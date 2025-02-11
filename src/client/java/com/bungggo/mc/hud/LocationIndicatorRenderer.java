package com.bungggo.mc.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;
import com.bungggo.mc.util.Util;

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

        if (!LocationDataManager.hasPinnedEntriesByWorld(Util.getCurrentWorldName(client))) {
            return;
        }

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        SimpleOption<Integer> fovOption = client.options.getFov();
        Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(fovOption.getValue());
        Camera camera = client.gameRenderer.getCamera();
        Matrix4f viewMatrix = computeViewMatrix(camera);
        Matrix4f viewProjMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f());

        // ピン画像の描画サイズ（元のサイズ）
        final int pinWidth = 16;
        final int pinHeight = 16;

        // onHudRender() 内で共通計算を行います
        long time = System.currentTimeMillis();
        final float period = 1000.0F;  // 2秒周期
        float t = (time % (long) period) / period;  // 0.0～1.0 に正規化

        float minAlpha = 0.3F;  // 最小透明度（30%）
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

        // 各ピンごとに処理（サイズをカメラとの距離で調整）
        for (LocationEntry entry : LocationDataManager.getPinnedEntriesByWorld(Util.getCurrentWorldName(client))) {
            Optional<ScreenCoordinate> optionalCoord = calculateScreenCoordinate(entry, viewProjMatrix, screenWidth, screenHeight);
            if (!optionalCoord.isPresent()) continue;
            ScreenCoordinate coord = optionalCoord.get();

            // ピン位置のワールド座標（ブロック中央）を計算
            float worldX = (float) (Math.floor(entry.x) + 0.5);
            float worldY = (float) (Math.floor(entry.y) + 0.5);
            float worldZ = (float) (Math.floor(entry.z) + 0.5);

            // カメラからの距離を計算
            Vec3d cameraPos = camera.getPos();
            double distance = cameraPos.distanceTo(new Vec3d(worldX, worldY, worldZ));

            // 距離に応じたスケールを計算（近いと最大、遠いと最小）
            final double nearDistance = 10.0;   // この距離以下なら最大サイズ（scale = 1.0）
            final double farDistance = 100.0;     // この距離以上なら最小サイズ
            final float minScale = 0.4f;          // 最小スケール（40%）
            final float maxScale = 1.0f;          // 最大スケール（100%）

            float scale;
            if (distance <= nearDistance) {
                scale = maxScale;
            } else if (distance >= farDistance) {
                scale = minScale;
            } else {
                // near～far の間は線形補間で決定
                scale = maxScale - (float)((distance - nearDistance) / (farDistance - nearDistance)) * (maxScale - minScale);
            }

            // マトリクス変換を利用して、テクスチャ全体をスケーリング描画する
            context.getMatrices().push();
            // 画面座標に合わせて平行移動
            context.getMatrices().translate(coord.x, coord.y, 0);
            // 距離に基づいたスケール倍率を適用
            context.getMatrices().scale(scale, scale, 1.0F);
            // テクスチャの下部中央が原点にくるようオフセット
            int drawX = - pinWidth / 2;
            int drawY = - pinHeight;
            context.drawTexture(
                RenderLayer::getGuiTextured,
                PIN_TEXTURE,
                drawX, drawY,
                0.0F, 0.0F,
                pinWidth, pinHeight, // 常に元のテクスチャ全体を描画
                pinWidth, pinHeight,
                tintColor
            );
            context.getMatrices().pop();

            // インジケーター下に距離テキストを表示する
            // 距離（ブロック数）を少数第1位まで表示
            String distanceText = String.format("%.1f", distance);
            int textColor = 0xAAFFFFFF; // 半透明の白色
            int textWidth = client.textRenderer.getWidth(distanceText);
            // 画面上の座標 coord はインジケーターの下端なので、そこからもう少し下に描画
            context.drawText(client.textRenderer, distanceText, coord.x - textWidth / 2, coord.y + 8, textColor, false);
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
