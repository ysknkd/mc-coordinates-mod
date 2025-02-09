package com.bungggo.mc;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import java.util.Optional;

/**
 * 簡易版のインジケーターを HUD に描画します。
 * プレイヤーのカメラ位置と各ピンの位置から水平面上の角度を計算し、
 * 画面中心から一定距離の位置に四角形（赤色）として表示します。
 */
public final class LocationIndicatorRenderer implements HudRenderCallback {

    public static void register() {
        HudRenderCallback.EVENT.register(new LocationIndicatorRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();
        MatrixStack matrices = context.getMatrices();

        SimpleOption<Integer> fovOption = client.options.getFov();
        Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(fovOption.getValue());
        Camera camera = client.gameRenderer.getCamera();
        Matrix4f viewMatrix = computeViewMatrix(camera);
        Matrix4f viewProjMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f());

        final int size = 10;
        final int color = 0xFFFF0000; // ARGB: 不透明な赤

        // 各ピンのワールド座標からスクリーン座標へ変換して描画
        for (LocationEntry entry : LocationDataManager.getPinnedEntries()) {
            Optional<ScreenCoordinate> optionalCoord = calculateScreenCoordinate(entry, viewProjMatrix, screenWidth, screenHeight);
            if (!optionalCoord.isPresent()) continue;
            ScreenCoordinate coord = optionalCoord.get();
            fill(matrices,
                 coord.x - size / 2,
                 coord.y - size / 2,
                 coord.x + size / 2,
                 coord.y + size / 2,
                 color);
        }
    }

    /**
     * カメラの位置と回転情報からビュー行列を生成する
     */
    private static Matrix4f computeViewMatrix(Camera camera) {
        Vec3d camPos = camera.getPos();
        Vector3f eye = new Vector3f((float) camPos.x, (float) camPos.y, (float) camPos.z);
        Vector3f forward = new Vector3f(0, 0, -1);
        camera.getRotation().transform(forward);
        Vector3f up = new Vector3f(0, 1, 0);
        camera.getRotation().transform(up);
        return new Matrix4f().lookAt(eye, new Vector3f(eye).add(forward), up);
    }

    /**
     * LocationEntry からスクリーン座標を計算する
     * カメラの背後にある場合は空を返す
     */
    private static Optional<ScreenCoordinate> calculateScreenCoordinate(LocationEntry entry, Matrix4f viewProjMatrix, int screenWidth, int screenHeight) {
        float worldX = (float) (Math.floor(entry.x) + 0.5);
        float worldY = (float) (Math.floor(entry.y) + 0.5);
        float worldZ = (float) (Math.floor(entry.z) + 0.5);

        org.joml.Vector4f pos = new org.joml.Vector4f(worldX, worldY, worldZ, 1.0f);
        viewProjMatrix.transform(pos);
        if (pos.w <= 0.0f) return Optional.empty();

        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;

        int indicatorX = clamp((int) ((ndcX + 1.0f) * 0.5f * screenWidth), 0, screenWidth);
        int indicatorY = clamp((int) ((1.0f - ndcY) * 0.5f * screenHeight), 0, screenHeight);

        return Optional.of(new ScreenCoordinate(indicatorX, indicatorY));
    }

    /**
     * 値を指定の範囲内にクランプする
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * スクリーン座標を保持する簡易クラス
     */
    private static class ScreenCoordinate {
        final int x, y;

        ScreenCoordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * 指定した範囲に四角形を塗りつぶす
     * ※ fill の内容は変更していません
     */
    private static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = (color >> 24 & 255) / 255F;
        float r = (color >> 16 & 255) / 255F;
        float g = (color >> 8  & 255) / 255F;
        float b = (color       & 255) / 255F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, (float) x2, (float) y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, (float) x1, (float) y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, (float) x1, (float) y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, (float) x2, (float) y2, 0).color(r, g, b, a);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
} 
