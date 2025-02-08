package com.bungggo.mc;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.util.math.MatrixStack;

import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BeaconRenderer:
 * ピン留め位置から上空（固定の 256.0F まで）へ一直線のラインを描画します。
 * 
 * Fabric Basic Rendering Concepts
 * の内容（https://docs.fabricmc.net/develop/rendering/basic-concepts）に基づいています。
 */
public final class BeaconRenderer {

    private static final Logger LOGGER = LoggerFactory.getLogger(BeaconRenderer.class);

    // インスタンス化防止
    private BeaconRenderer() {
    }

    /**
     * ワールドまたは HUD のレンダリングイベントに登録します。
     * ※ここでは HUD 描画イベントを利用していますが、必要に応じて world レンダリングに変更してください。
     */
    public static void register() {
        WorldRenderEvents.BEFORE_DEBUG_RENDER.register((context) -> {
            renderPinnedBeacons(context.matrixStack());
        });
    }

    /**
     * すべてのピン留め位置に対してビームを描画します。
     *
     * @param matrices 現在の MatrixStack
     */
    private static void renderPinnedBeacons(MatrixStack matrices) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null)
            return;

        LOGGER.debug("Pinned entries count: {}", LocationDataManager.getPinnedEntries().size());
        for (LocationEntry entry : LocationDataManager.getPinnedEntries()) {
            LOGGER.debug("Rendering beacon at: x={}, y={}, z={}", entry.x, entry.y, entry.z);
            renderBeaconBeam(matrices, entry);
        }
    }

    /**
     * 指定された位置から上空（skyHeight=256.0F）までの直方体（ビーム）を描画します。
     *
     * @param matrices MatrixStack
     * @param entry    描画する位置情報
     */
    private static void renderBeaconBeam(MatrixStack matrices, LocationEntry entry) {

        // ----- まずは四角形のポリゴンを描画（例示用） -----
        matrices.push();
        // 現在の変換行列を取得（HUD 座標系）
        Matrix4f transform = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        // QUADS モードで頂点バッファを初期化
        BufferBuilder buffer = tessellator.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // ここでは座標 (10,10) ～ (110,110) の四角形を描画（色は薄緑）
        buffer.vertex(transform, 10, 10, 0).color(0xFF88FF88);
        buffer.vertex(transform, 110, 10, 0).color(0xFF88FF88);
        buffer.vertex(transform, 110, 110, 0).color(0xFF88FF88);
        buffer.vertex(transform, 10, 110, 0).color(0xFF88FF88);

        // 正しいシェーダー設定で描画
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(buffer.end());
        matrices.pop();

        // ----- 次に直方体（ビーム）を描画 -----
        matrices.push();
        // カメラ位置に合わせた相対座標に変換

        MinecraftClient client = MinecraftClient.getInstance();
        double camX = client.gameRenderer.getCamera().getPos().x;
        double camY = client.gameRenderer.getCamera().getPos().y;
        double camZ = client.gameRenderer.getCamera().getPos().z;

        /*
         * entry の座標は小数点以下を含むため、ブロックの中心に合わせるには
         * 各軸の整数部分を Math.floor() で取得し、+0.5F を行います。
         * さらに、ビームはブロック上面から始まるよう Y 軸に +0.5F します。
         */
        float alignedX = (float) (Math.floor(entry.x) + 0.5);
        float alignedY = (float) (Math.floor(entry.y) + 0.5);
        float alignedZ = (float) (Math.floor(entry.z) + 0.5);
        matrices.translate(alignedX - (float) camX, (alignedY + 0.5F) - (float) camY, alignedZ - (float) camZ);

        /*
         * 直方体（ビーム）のサイズ:
         * 横幅・奥行は 1 ブロック（X, Z は -0.5 ～ +0.5）
         * 高さは 254（Y 軸: 下端 0 ～ 上端 254、合計 254）
         */
        float half = 0.5F;
        float bottomY = 0.0F;
        float topY = 254.0F;

        Matrix4f m = matrices.peek().getPositionMatrix();
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder cubeBuffer = tess.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        // 前面 (Z = +half)【青】
        cubeBuffer.vertex(m, -half, bottomY, half).color(0, 0, 255, 255);
        cubeBuffer.vertex(m, half, bottomY, half).color(0, 0, 255, 255);
        cubeBuffer.vertex(m, half, topY,    half).color(0, 0, 255, 255);
        cubeBuffer.vertex(m, -half, topY,   half).color(0, 0, 255, 255);

        // 背面 (Z = -half)【赤】
        cubeBuffer.vertex(m, half, bottomY, -half).color(255, 0, 0, 255);
        cubeBuffer.vertex(m, -half, bottomY, -half).color(255, 0, 0, 255);
        cubeBuffer.vertex(m, -half, topY,   -half).color(255, 0, 0, 255);
        cubeBuffer.vertex(m, half, topY,    -half).color(255, 0, 0, 255);

        // 左側面 (X = -half)【緑】
        cubeBuffer.vertex(m, -half, bottomY, -half).color(0, 255, 0, 255);
        cubeBuffer.vertex(m, -half, bottomY, half).color(0, 255, 0, 255);
        cubeBuffer.vertex(m, -half, topY,    half).color(0, 255, 0, 255);
        cubeBuffer.vertex(m, -half, topY,   -half).color(0, 255, 0, 255);

        // 右側面 (X = +half)【黄】
        cubeBuffer.vertex(m, half, bottomY, half).color(255, 255, 0, 255);
        cubeBuffer.vertex(m, half, bottomY, -half).color(255, 255, 0, 255);
        cubeBuffer.vertex(m, half, topY,   -half).color(255, 255, 0, 255);
        cubeBuffer.vertex(m, half, topY,    half).color(255, 255, 0, 255);

        // 上面 (Y = topY)【シアン】
        cubeBuffer.vertex(m, -half, topY,   half).color(0, 255, 255, 255);
        cubeBuffer.vertex(m, half, topY,    half).color(0, 255, 255, 255);
        cubeBuffer.vertex(m, half, topY,   -half).color(0, 255, 255, 255);
        cubeBuffer.vertex(m, -half, topY,  -half).color(0, 255, 255, 255);

        // 底面 (Y = bottomY)【マゼンタ】
        cubeBuffer.vertex(m, -half, bottomY, -half).color(255, 0, 255, 255);
        cubeBuffer.vertex(m, half, bottomY, -half).color(255, 0, 255, 255);
        cubeBuffer.vertex(m, half, bottomY, half).color(255, 0, 255, 255);
        cubeBuffer.vertex(m, -half, bottomY, half).color(255, 0, 255, 255);

        // シェーダー設定後、バッファ描画
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        BufferRenderer.drawWithGlobalProgram(cubeBuffer.end());
        matrices.pop();
    }
}