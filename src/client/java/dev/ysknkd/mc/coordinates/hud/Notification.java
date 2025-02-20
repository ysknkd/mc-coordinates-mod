package dev.ysknkd.mc.coordinates.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * Displays temporary notification messages on the HUD.
 */
public class Notification implements HudRenderCallback {
    // The currently displayed message
    private static String currentMessage = null;
    // The timestamp when the message display started (in milliseconds)
    private static long messageStartTime = 0;
    // The timestamp when the message should end (in milliseconds)
    private static long messageEndTime = 0;

    public static void register() {
        HudRenderCallback.EVENT.register(new Notification());
    }

    public static void show(String message) {
        show(message, 3000);
    }

    public static void show(String message, long durationMillis) {
        currentMessage = message;
        messageStartTime = System.currentTimeMillis();
        messageEndTime = messageStartTime + durationMillis;
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        if (currentMessage == null) {
            return;
        }
        
        long now = System.currentTimeMillis();
        if (now >= messageEndTime) {
            // Clear the message if the display duration has ended
            currentMessage = null;
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        
        // Fade-out effect: gradually decrease opacity during the last 500 milliseconds
        float alphaFactor = 1.0f;
        long fadeDuration = 500; // milliseconds
        long remaining = messageEndTime - now;
        if (remaining < fadeDuration) {
            alphaFactor = remaining / (float) fadeDuration;
        }
        
        // Text dimensions
        int textWidth = textRenderer.getWidth(currentMessage);
        int textHeight = textRenderer.fontHeight;
        
        int x = 10;
        int y = 20;
        
        // Background rectangle with padding
        int padding = 4;
        int rectX = x - padding;
        int rectY = y - padding;
        int rectWidth = textWidth + padding * 2;
        int rectHeight = textHeight + padding * 2;
        
        // Background color: black with applied alpha (max value 120)
        int bgAlpha = (int) (alphaFactor * 120);
        int backgroundColor = (bgAlpha << 24);
        context.fill(rectX, rectY, rectX + rectWidth, rectY + rectHeight, backgroundColor);
        
        // Text color: white with applied alpha (max value 224)
        int textAlpha = (int) (alphaFactor * 224);
        int textColor = (textAlpha << 24) | 0xFFFFFF;
        context.drawText(client.textRenderer, currentMessage, x, y, textColor, false);
    }
}
