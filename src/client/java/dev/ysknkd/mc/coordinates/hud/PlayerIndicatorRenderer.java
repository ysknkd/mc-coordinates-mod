package dev.ysknkd.mc.coordinates.hud;

import dev.ysknkd.mc.coordinates.config.Config;
import dev.ysknkd.mc.coordinates.store.PlayerCoordinatesCache;
import dev.ysknkd.mc.coordinates.store.PlayerCoordinates;
import dev.ysknkd.mc.coordinates.util.IconTexture;
import dev.ysknkd.mc.coordinates.util.Util;
import dev.ysknkd.mc.coordinates.CoordinatesApp;
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
 * Renders each player's icon, distance from the camera, and their name on the HUD based on their position.
 */
public final class PlayerIndicatorRenderer implements HudElement {

    private long frozenTime;

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "player_indicators"),
                new PlayerIndicatorRenderer());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) return;

        int screenWidth = context.guiWidth();
        int screenHeight = context.guiHeight();
        Camera camera = client.gameRenderer.mainCamera();

        // Animation for alpha fade in/out (1-second cycle)
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
            Vec3 worldPos = new Vec3(worldX, worldY, worldZ);
            Vec3 camPos = camera.position();
            double distance = camPos.distanceTo(worldPos);

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

            Optional<ScreenProjection.Coordinate> optionalCoord = ScreenProjection.projectWorldToGui(client, camera, worldPos, screenWidth, screenHeight);
            if (!optionalCoord.isPresent()) continue;
            ScreenProjection.Coordinate coord = optionalCoord.get();
            int screenX = coord.x;
            int screenY = coord.y;

            // Retrieve the player's icon (skin) from their GameProfile
            Identifier texture = IconTexture.getPlayerIcon(playerEntity.uuid, playerEntity.name);

            // Draw the face and hat layers from the 64x64 player skin.
            final int iconSize = 16;
            final int faceSize = 8;
            final int skinSize = 64;
            int scaledIconSize = Math.max(1, Math.round(iconSize * scale));
            int drawX = screenX - scaledIconSize / 2;
            int drawY = screenY - scaledIconSize; // Adjust to align the bottom center of the icon with the origin
            context.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                drawX, drawY,
                8.0F, 8.0F,
                scaledIconSize, scaledIconSize,
                faceSize, faceSize,
                skinSize, skinSize,
                tintColor
            );
            context.blit(
                RenderPipelines.GUI_TEXTURED,
                texture,
                drawX, drawY,
                40.0F, 8.0F,
                scaledIconSize, scaledIconSize,
                faceSize, faceSize,
                skinSize, skinSize,
                tintColor
            );

            // Render distance text
            String distanceText = String.format("%.1f", distance);
            int textColor = 0xAAFFFFFF; // Semi-transparent white
            int distanceTextWidth = client.font.width(distanceText);
            context.text(client.font, distanceText, screenX - distanceTextWidth / 2, screenY + 8, textColor, false);

            // Render the player's name
            String name = playerEntity.name;
            int nameWidth = client.font.width(name);
            context.text(client.font, name, screenX - nameWidth / 2, screenY + 20, textColor, false);
        }
    }

}
