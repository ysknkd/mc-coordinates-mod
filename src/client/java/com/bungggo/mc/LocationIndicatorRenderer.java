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

        SimpleOption<Integer> fov = client.options.getFov();
        Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(fov.getValue());
        Camera camera = client.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        Vector3f eye = new Vector3f((float) camPos.x, (float) camPos.y, (float) camPos.z);
        Vector3f forward = new Vector3f(0, 0, -1);
        camera.getRotation().transform(forward);
        Vector3f up = new Vector3f(0, 1, 0);
        camera.getRotation().transform(up);
        Matrix4f viewMatrix = new Matrix4f().lookAt(eye, new Vector3f(eye).add(forward), up);
        Matrix4f viewProjMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f());

        for (LocationEntry entry : LocationDataManager.getPinnedEntries()) {
            double worldX = Math.floor(entry.x) + 0.5;
            double worldY = Math.floor(entry.y) + 0.5;
            double worldZ = Math.floor(entry.z) + 0.5;

            org.joml.Vector4f pos = new org.joml.Vector4f((float) worldX, (float) worldY, (float) worldZ, 1.0f);
            viewProjMatrix.transform(pos);
            if (pos.w <= 0.0f) continue;

            float ndcX = pos.x / pos.w;
            float ndcY = pos.y / pos.w;

            int indicatorX = (int) ((ndcX + 1.0f) * 0.5f * screenWidth);
            int indicatorY = (int) ((1.0f - ndcY) * 0.5f * screenHeight);

            indicatorX = Math.max(0, Math.min(indicatorX, screenWidth));
            indicatorY = Math.max(0, Math.min(indicatorY, screenHeight));

            int size = 10;
            int color = 0xFFFF0000; // ARGB: 不透明な赤
            fill(matrices,
                 indicatorX - size / 2,
                 indicatorY - size / 2,
                 indicatorX + size / 2,
                 indicatorY + size / 2,
                 color);
        }
    }

    private static void fill(MatrixStack matrices, int x1, int y1, int x2, int y2, int color) {
        Matrix4f matrix = matrices.peek().getPositionMatrix();
        float a = (color >> 24 & 255) / 255F;
        float r = (color >> 16 & 255) / 255F;
        float g = (color >> 8  & 255) / 255F;
        float b = (color       & 255) / 255F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        BufferBuilder buffer = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, (float)x2, (float)y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, (float)x1, (float)y1, 0).color(r, g, b, a);
        buffer.vertex(matrix, (float)x1, (float)y2, 0).color(r, g, b, a);
        buffer.vertex(matrix, (float)x2, (float)y2, 0).color(r, g, b, a);

        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        RenderSystem.setShaderColor(r, g, b, a);

        BufferRenderer.drawWithGlobalProgram(buffer.end());

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
} 
