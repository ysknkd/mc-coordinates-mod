package dev.ysknkd.mc.coordinates.hud;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.store.Coordinates;
import dev.ysknkd.mc.coordinates.util.IconTexture;
import dev.ysknkd.mc.coordinates.util.Util;

import java.util.Optional;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/**
 * Renders a simplified indicator on the HUD.
 * Calculates the angle on the horizontal plane from the player's camera position to each pinned coordinate,
 * and displays a red rectangle at a fixed distance from the center of the screen.
 */
public final class IndicatorRenderer implements HudElement {

    private long frozenTime;

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "indicators"),
                new IndicatorRenderer());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        if (!CoordinatesDataManager.hasPinnedEntriesByWorld(Util.getCurrentWorldName(client))) {
            return;
        }

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();

        Camera camera = client.gameRenderer.mainCamera();

        // Pin image drawing dimensions (original size)
        final int pinWidth = 16;
        final int pinHeight = 16;

        // Common calculations within onHudRender()
        long time = System.currentTimeMillis();
        if (client.gui.screen() != null) {
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
        int tintColor = (alphaInt << 24) | 0xFFFFFF;  // Note: Final result will have correct alpha

        // Process each pinned coordinate entry
        for (Coordinates entry : CoordinatesDataManager.getPinnedEntriesByWorld(Util.getCurrentWorldName(client))) {
            // Compute the block center for world coordinates
            float worldX = (float) (Math.floor(entry.x) + 0.5);
            float worldY = (float) (Math.floor(entry.y) + 0.5);
            float worldZ = (float) (Math.floor(entry.z) + 0.5);

            // Calculate the distance from the camera
            Vec3 cameraPos = camera.position();
            Vec3 worldPos = new Vec3(worldX, worldY, worldZ);
            double distance = cameraPos.distanceTo(worldPos);

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
            Optional<ScreenCoordinate> optionalCoord = calculateScreenCoordinate(worldPos, client, screenWidth, screenHeight);
            if (!optionalCoord.isPresent()) continue;
            ScreenCoordinate coord = optionalCoord.get();

            // Render the pin image texture (to be implemented according to texture rendering routines)
            int scaledPinWidth = Math.max(1, Math.round(pinWidth * scale));
            int scaledPinHeight = Math.max(1, Math.round(pinHeight * scale));
            context.blit(
                RenderPipelines.GUI_TEXTURED,
                IconTexture.getIcon(entry.icon),
                coord.x - scaledPinWidth / 2, coord.y - scaledPinHeight / 2,
                0.0F, 0.0F,
                scaledPinWidth, scaledPinHeight,
                pinWidth, pinHeight,
                tintColor
            );

            // Render distance text
            String distanceText = String.format("%.1f", distance);
            int textColor = 0xAAFFFFFF; // 半透明の白色
            int distanceTextWidth = client.font.width(distanceText);
            context.text(client.font, distanceText, coord.x - distanceTextWidth / 2, coord.y + 8, textColor, false);

            // Render description text
            String descriptionText = entry.description;
            int descriptionTextWidth = client.font.width(descriptionText);
            context.text(client.font, descriptionText, coord.x - descriptionTextWidth / 2, coord.y + 20, textColor, false);
        }
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
    private static Optional<ScreenCoordinate> calculateScreenCoordinate(Vec3 worldPos, Minecraft client, int screenWidth, int screenHeight) {
        Vec3 screenPos = client.gameRenderer.projectPointToScreen(worldPos);
        if (screenPos == null || !screenPos.isFinite()) return Optional.empty();

        int indicatorX = clamp((int) screenPos.x, 0, screenWidth);
        int indicatorY = clamp((int) screenPos.y, 0, screenHeight);

        return Optional.of(new ScreenCoordinate(indicatorX, indicatorY));
    }
}
