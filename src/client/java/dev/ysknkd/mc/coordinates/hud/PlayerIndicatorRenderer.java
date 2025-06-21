package dev.ysknkd.mc.coordinates.hud;

import dev.ysknkd.mc.coordinates.config.Config;
import dev.ysknkd.mc.coordinates.store.PlayerCoordinatesCache;
import dev.ysknkd.mc.coordinates.store.PlayerCoordinates;
import dev.ysknkd.mc.coordinates.util.IconTexture;
import dev.ysknkd.mc.coordinates.util.Util;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Renders each player's icon, distance from the camera, and their name on the HUD based on their position.
 */
public final class PlayerIndicatorRenderer implements HudRenderCallback {

    private long frozenTime;

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

        // Animation for alpha fade in/out (1-second cycle)
        long time = System.currentTimeMillis();
        if (client.currentScreen != null) {
            if (frozenTime == -1) {
                frozenTime = System.currentTimeMillis();
            }
            time = frozenTime;
        } else {
            frozenTime = -1;
            time = System.currentTimeMillis();
        }
        
        final float period = 1000.0F;
        float t = (time % (long) period) / period;
        float ease = (t < 0.5F)
                ? (float) Math.pow(t / 0.5F, 5)
                : (float) Math.pow((1.0F - t) / 0.5F, 5);
        float minAlpha = 0.5F, maxAlpha = 1.0F;
        float alphaValue = minAlpha + ease * (maxAlpha - minAlpha);
        int alphaInt = (int)(alphaValue * 255);
        int tintColor = (alphaInt << 24) | 0xFFFFFF;

        String currentWorld = Util.getCurrentWorldName(client);

        for (PlayerCoordinates playerEntity : PlayerCoordinatesCache.getCoordinatesList()) {
            if (playerEntity.world == null || !playerEntity.world.equals(currentWorld)) continue;

            // Treat world coordinates as the center of the block
            float worldX = (float)(Math.floor(playerEntity.x) + 0.5);
            float worldY = (float)(Math.floor(playerEntity.y) + 0.5);
            float worldZ = (float)(Math.floor(playerEntity.z) + 0.5);

            // Calculate distance from the camera
            Vec3d camPos = camera.getPos();
            double distance = camPos.distanceTo(new Vec3d(worldX, worldY, worldZ));

            // Do not display the indicator if the distance is less than the configured minimum distance
            if (distance < Config.getPlayerIndicatorMinDistance()) {
                continue;
            }

            // Determine scale based on distance (closer -> larger, farther -> smaller)
            final double nearDistance = Config.getPlayerIndicatorMinDistance();
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

            // Convert world coordinates to screen coordinates
            Vector4f posVec = new Vector4f(worldX, worldY, worldZ, 1.0f);
            viewProjMatrix.transform(posVec);
            if (posVec.w <= 0.0f) continue;
            float ndcX = posVec.x / posVec.w;
            float ndcY = posVec.y / posVec.w;
            int screenX = clamp((int)((ndcX + 1.0f) * 0.5f * screenWidth), 0, screenWidth);
            int screenY = clamp((int)((1.0f - ndcY) * 0.5f * screenHeight), 0, screenHeight);

            // Render the player's icon (skin texture)
            context.getMatrices().pushMatrix();
            context.getMatrices().translate(screenX, screenY);
            context.getMatrices().scale(scale, scale);

            // Retrieve the player's icon (skin) from their GameProfile
            Identifier texture = IconTexture.getPlayerIcon(playerEntity.uuid, playerEntity.name);

            // Draw the icon (icon size is 16x16 pixels)
            final int iconSize = 16;
            int drawX = -iconSize / 2;
            int drawY = -iconSize; // Adjust to align the bottom center of the icon with the origin
            context.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                texture,
                drawX, drawY,
                0.0F, 0.0F,
                iconSize, iconSize,
                iconSize, iconSize,
                tintColor
            );
            context.getMatrices().popMatrix();

            // Render distance text
            String distanceText = String.format("%.1f", distance);
            int textColor = 0xAAFFFFFF; // Semi-transparent white
            int distanceTextWidth = client.textRenderer.getWidth(distanceText);
            context.drawText(client.textRenderer, distanceText, screenX - distanceTextWidth / 2, screenY + 8, textColor, false);

            // Render the player's name
            String name = playerEntity.name;
            int nameWidth = client.textRenderer.getWidth(name);
            context.drawText(client.textRenderer, name, screenX - nameWidth / 2, screenY + 20, textColor, false);
        }
    }

    /**
     * Computes the view matrix from the camera's position and rotation.
     *
     * @param camera The camera object containing position and rotation.
     * @return The computed view matrix.
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
     * Clamps the given value within the specified range.
     *
     * @param value The value to clamp.
     * @param min The minimum allowed value.
     * @param max The maximum allowed value.
     * @return The clamped value.
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }
}