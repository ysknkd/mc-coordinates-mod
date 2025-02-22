package dev.ysknkd.mc.coordinates.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.RenderTickCounter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;

import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.store.Coordinates;
import dev.ysknkd.mc.coordinates.util.IconTexture;
import dev.ysknkd.mc.coordinates.util.Util;

import java.util.Optional;
import net.minecraft.client.render.RenderLayer;

/**
 * Renders a simplified indicator on the HUD.
 * Calculates the angle on the horizontal plane from the player's camera position to each pinned coordinate,
 * and displays a red rectangle at a fixed distance from the center of the screen.
 */
public final class IndicatorRenderer implements HudRenderCallback {

    private long frozenTime;

    public static void register() {
        HudRenderCallback.EVENT.register(new IndicatorRenderer());
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.world == null) return;

        if (!CoordinatesDataManager.hasPinnedEntriesByWorld(Util.getCurrentWorldName(client))) {
            return;
        }

        int screenWidth = context.getScaledWindowWidth();
        int screenHeight = context.getScaledWindowHeight();

        SimpleOption<Integer> fovOption = client.options.getFov();
        Matrix4f projectionMatrix = client.gameRenderer.getBasicProjectionMatrix(fovOption.getValue());
        Camera camera = client.gameRenderer.getCamera();
        Matrix4f viewMatrix = computeViewMatrix(camera);
        Matrix4f viewProjMatrix = projectionMatrix.mul(viewMatrix, new Matrix4f());

        // Pin image drawing dimensions (original size)
        final int pinWidth = 16;
        final int pinHeight = 16;

        // Common calculations within onHudRender()
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

        final float period = 1000.0F;  // 1-second cycle period
        float t = (time % (long) period) / period;
        float minAlpha = 0.3F;  // Minimum opacity (30%)
        float maxAlpha = 1.0F;  // Maximum opacity (100%)
        float ease;
        if (t < 0.5F) {
            // First half: normalize 0–0.5 range and apply quintic easing in
            float progress = t / 0.5F;
            ease = (float) Math.pow(progress, 5);
        } else {
            // Second half: normalize (1 - t) over the range 0.5–1 and apply quintic easing out
            float progress = (1.0F - t) / 0.5F;
            ease = (float) Math.pow(progress, 5);
        }
        float alphaValue = minAlpha + ease * (maxAlpha - minAlpha);
        int alphaInt = (int)(alphaValue * 255);
        int tintColor = (alphaInt << 24) | 0xFFFFFF;

        // Process each pinned coordinate entry
        for (Coordinates entry : CoordinatesDataManager.getPinnedEntriesByWorld(Util.getCurrentWorldName(client))) {
            // Compute the block center for world coordinates
            float worldX = (float) (Math.floor(entry.x) + 0.5);
            float worldY = (float) (Math.floor(entry.y) + 0.5);
            float worldZ = (float) (Math.floor(entry.z) + 0.5);

            // Calculate the distance from the camera
            Vec3d cameraPos = camera.getPos();
            double distance = cameraPos.distanceTo(new Vec3d(worldX, worldY, worldZ));

            // Determine scale based on distance (closer gives maximum scale; farther gives minimum)
            final double nearDistance = 10.0;
            final double farDistance = 100.0;
            final float minScale = 0.4f;
            final float maxScale = 1.0f;
            float scale;
            if (distance <= nearDistance) {
                scale = maxScale;
            } else if (distance >= farDistance) {
                scale = minScale;
            } else {
                scale = maxScale - (float)((distance - nearDistance) / (farDistance - nearDistance)) * (maxScale - minScale);
            }

            // Convert world coordinates to screen coordinates
            Optional<ScreenCoordinate> optionalCoord = calculateScreenCoordinate(entry, viewProjMatrix, screenWidth, screenHeight);
            if (!optionalCoord.isPresent()) continue;
            ScreenCoordinate coord = optionalCoord.get();

            // Scale and translate the pin image based on calculated coordinates
            context.getMatrices().push();
            context.getMatrices().translate(coord.x, coord.y, 0);
            context.getMatrices().scale(scale, scale, 1.0F);

            // Render the pin image texture (to be implemented according to texture rendering routines)
            context.drawTexture(
                RenderLayer::getGuiTextured,
                IconTexture.getIcon(entry.icon),
                -pinWidth / 2, -pinHeight / 2,
                0.0F, 0.0F,
                pinWidth, pinHeight,
                pinWidth, pinHeight,
                tintColor
            );
            context.getMatrices().pop();

            // Render distance text
            String distanceText = String.format("%.1f", distance);
            int textColor = 0xAAFFFFFF; // 半透明の白色
            int distanceTextWidth = client.textRenderer.getWidth(distanceText);
            context.drawText(client.textRenderer, distanceText, coord.x - distanceTextWidth / 2, coord.y + 8, textColor, false);

            // Render description text
            String descriptionText = entry.description;
            int descriptionTextWidth = client.textRenderer.getWidth(descriptionText);
            context.drawText(client.textRenderer, descriptionText, coord.x - descriptionTextWidth / 2, coord.y + 20, textColor, false);
        }
    }

    /**
     * Computes the view matrix from the camera's position and rotation.
     *
     * @param camera The camera containing position and rotation info.
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
     * Clamps the given value within the range [min, max].
     *
     * @param value The input value.
     * @param min The minimum allowed value.
     * @param max The maximum allowed value.
     * @return The clamped value.
     */
    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(value, max));
    }

    /**
     * A helper class to hold screen coordinate data.
     */
    private static class ScreenCoordinate {
        final int x, y;
        ScreenCoordinate(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * Converts a world coordinate from a pinned entry into screen coordinates.
     * Returns an empty Optional if the coordinate is not visible (e.g. behind the camera).
     *
     * @param entry The coordinate entry.
     * @param viewProjMatrix The combined view-projection matrix.
     * @param screenWidth Screen width.
     * @param screenHeight Screen height.
     * @return An Optional containing the screen coordinate if visible.
     */
    private static Optional<ScreenCoordinate> calculateScreenCoordinate(Coordinates entry, Matrix4f viewProjMatrix, int screenWidth, int screenHeight) {
        float worldX = (float)(Math.floor(entry.x) + 0.5);
        float worldY = (float)(Math.floor(entry.y) + 0.5);
        float worldZ = (float)(Math.floor(entry.z) + 0.5);

        org.joml.Vector4f pos = new org.joml.Vector4f(worldX, worldY, worldZ, 1.0f);
        viewProjMatrix.transform(pos);
        if (pos.w <= 0.0f) return Optional.empty();

        float ndcX = pos.x / pos.w;
        float ndcY = pos.y / pos.w;

        int indicatorX = clamp((int)((ndcX + 1.0f) * 0.5f * screenWidth), 0, screenWidth);
        int indicatorY = clamp((int)((1.0f - ndcY) * 0.5f * screenHeight), 0, screenHeight);

        return Optional.of(new ScreenCoordinate(indicatorX, indicatorY));
    }
}
