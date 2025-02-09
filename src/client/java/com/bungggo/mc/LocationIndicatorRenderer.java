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
import net.minecraft.util.Identifier;
import net.minecraft.client.render.RenderLayer;

/**
 * 簡易版のインジケーターを HUD に描画します。
 * プレイヤーのカメラ位置と各ピンの位置から水平面上の角度を計算し、
 * 画面中心から一定距離の位置に四角形（赤色）として表示します。
 */
public final class LocationIndicatorRenderer implements HudRenderCallback {

    private static final Identifier PIN_TEXTURE = Identifier.of("mc-location", "textures/indicator/pin.png");

    public static void register() {
        HudRenderCallback.EVENT.register(new LocationIndicatorRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        SimpleOption<Integer> fovOption = client.options.getFov();
        Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(fovOption.getValue());
        Camera camera = client.gameRenderer.getCamera();
        Matrix4f viewMatrix = computeViewMatrix(camera);
        Matrix4f viewProjMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f());

        // ピン画像の描画サイズ
        final int pinWidth = 16;
        final int pinHeight = 16;

        // onHudRender() 内で共通計算を行います
        long time = System.currentTimeMillis();
        final float period = 1000.0F;  // 2秒周期
        float t = (time % (long) period) / period;  // 0.0～1.0 に正規化

        float minAlpha = 0.5F;  // 最小透明度（50%）
        float maxAlpha = 1.0F;  // 最大透明度（100%）
        float ease;
        if (t < 0.5F) {
            // 前半：0～0.5 の範囲を正規化、easeInQuintで上昇
            float progress = t / 0.5F;
            ease = (float) Math.pow(progress, 5);
        } else {
            // 後半：0.5～1 の範囲は (1 - t) を正規化、easeInQuintで下降
            float progress = (1.0F - t) / 0.5F;
            ease = (float) Math.pow(progress, 5);
        }
        float alphaValue = minAlpha + ease * (maxAlpha - minAlpha);
        int alphaInt = (int)(alphaValue * 255);
        int tintColor = (alphaInt << 24) | 0xFFFFFF;

        for (LocationEntry entry : LocationDataManager.getPinnedEntries()) {
            Optional<ScreenCoordinate> optionalCoord = calculateScreenCoordinate(entry, viewProjMatrix, screenWidth, screenHeight);
            if (!optionalCoord.isPresent()) continue;
            ScreenCoordinate coord = optionalCoord.get();
            
            // ピン画像のオフセット（例えば、画像の下部の先端を指すように）
            int x = coord.x - pinWidth / 2;
            int y = coord.y - pinHeight;
            
            context.drawTexture(
                RenderLayer::getGuiTextured,
                PIN_TEXTURE,
                x, y,
                0.0F, 0.0F,
                pinWidth, pinHeight,
                pinWidth, pinHeight,
                tintColor
            );
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
} 
