 package dev.ysknkd.mc.coordinates.hud;

import dev.ysknkd.mc.coordinates.store.PlayerCoordinatesCache;
import dev.ysknkd.mc.coordinates.store.PlayerCoordinates;
import dev.ysknkd.mc.coordinates.util.IconTexture;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * クライアント上で、自分以外のプレイヤーの位置情報から
 * アイコン（スキン）、カメラまでの距離、名前をHUD上に描画するクラスです。
 */
public final class PlayerIndicatorRenderer implements HudRenderCallback {

    public static void register() {
        HudRenderCallback.EVENT.register(new PlayerIndicatorRenderer());
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

        // アルファ値のフェードイン・アウト用のアニメーション（周期1秒）
        long time = System.currentTimeMillis();
        final float period = 1000.0F;
        float t = (time % (long) period) / period;
        float ease = (t < 0.5F)
                ? (float) Math.pow(t / 0.5F, 5)
                : (float) Math.pow((1.0F - t) / 0.5F, 5);
        float minAlpha = 0.5F, maxAlpha = 1.0F;
        float alphaValue = minAlpha + ease * (maxAlpha - minAlpha);
        int alphaInt = (int)(alphaValue * 255);
        int tintColor = (alphaInt << 24) | 0xFFFFFF;

        for (PlayerCoordinates playerEntity : PlayerCoordinatesCache.getCoordinatesList()) {
            // ワールド座標をブロック中央として扱う
            float worldX = (float)(Math.floor(playerEntity.x) + 0.5);
            float worldY = (float)(Math.floor(playerEntity.y) + 0.5);
            float worldZ = (float)(Math.floor(playerEntity.z) + 0.5);

            // カメラからの距離計算
            Vec3d camPos = camera.getPos();
            double distance = camPos.distanceTo(new Vec3d(worldX, worldY, worldZ));

            // 距離が10ブロック未満の場合はインジケーターを表示しない
            if (distance < 10.0) {
                continue;
            }

            // 距離に応じたスケール（近いほど大きく、遠いほど小さく）
            final double nearDistance = 10.0;
            final double farDistance = 100.0;
            final float minScale = 0.4f, maxScale = 1.0f;
            float scale;
            if (distance <= nearDistance) {
                scale = maxScale;
            } else if (distance >= farDistance) {
                scale = minScale;
            } else {
                scale = maxScale - (float)((distance - nearDistance) / (farDistance - nearDistance)) * (maxScale - minScale);
            }

            // ワールド座標からスクリーン座標へ変換
            Vector4f posVec = new Vector4f(worldX, worldY, worldZ, 1.0f);
            viewProjMatrix.transform(posVec);
            if (posVec.w <= 0.0f) continue;
            float ndcX = posVec.x / posVec.w;
            float ndcY = posVec.y / posVec.w;
            int screenX = clamp((int)((ndcX + 1.0f) * 0.5f * screenWidth), 0, screenWidth);
            int screenY = clamp((int)((1.0f - ndcY) * 0.5f * screenHeight), 0, screenHeight);

            context.getMatrices().push();
            context.getMatrices().translate(screenX, screenY, 0);
            context.getMatrices().scale(scale, scale, 1.0F);

            // プレイヤーの GameProfile からアイコン（スキンテクスチャ）を取得
            Identifier texture = IconTexture.getPlayerIcon(playerEntity.uuid);

            // アイコン描画（アイコンサイズは16×16ピクセル）
            final int iconSize = 16;
            int drawX = -iconSize / 2;
            int drawY = -iconSize; // アイコンの下部中央を原点に調整
            context.drawTexture(
                RenderLayer::getGuiTextured,
                texture,
                drawX, drawY,
                0.0F, 0.0F,
                iconSize, iconSize,
                iconSize, iconSize,
                tintColor
            );
            context.getMatrices().pop();

            // 距離テキスト描画
            String distanceText = String.format("%.1f", distance);
            int textColor = 0xAAFFFFFF; // 半透明の白
            int distanceTextWidth = client.textRenderer.getWidth(distanceText);
            context.drawText(client.textRenderer, distanceText, screenX - distanceTextWidth / 2, screenY + 8, textColor, false);

            // プレイヤー名前も描画
            String name = playerEntity.name;
            int nameWidth = client.textRenderer.getWidth(name);
            context.drawText(client.textRenderer, name, screenX - nameWidth / 2, screenY + 20, textColor, false);
        }
    }

    /**
     * カメラの位置と回転情報からビュー行列を生成します。
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
     * 値を指定範囲内にクランプします。
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}