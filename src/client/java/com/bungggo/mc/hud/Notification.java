package com.bungggo.mc.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

/**
 * 一時的な通知メッセージを HUD に表示するクラスです。
 */
public class Notification implements HudRenderCallback {
    // 現在表示中のメッセージ
    private static String currentMessage = null;
    // メッセージが表示開始した時刻（ミリ秒）
    private static long messageStartTime = 0;
    // メッセージの終了時刻（ミリ秒）
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
            // 表示時間が終了したので、メッセージをクリア
            currentMessage = null;
            return;
        }
        
        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;
        
        // フェードアウト効果：終了500ミリ秒前から徐々に透明にする
        float alphaFactor = 1.0f;
        long fadeDuration = 500; // ミリ秒
        long remaining = messageEndTime - now;
        if (remaining < fadeDuration) {
            alphaFactor = remaining / (float) fadeDuration;
        }
        
        // テキストの幅と高さ
        int textWidth = textRenderer.getWidth(currentMessage);
        int textHeight = textRenderer.fontHeight;
        
        int x = 10;
        int y = 20;
        
        // 背景となる矩形（パディング付き）
        int padding = 4;
        int rectX = x - padding;
        int rectY = y - padding;
        int rectWidth = textWidth + padding * 2;
        int rectHeight = textHeight + padding * 2;
        
        // 背景色：黒色に alpha を乗せたもの（最大値 120）
        int bgAlpha = (int) (alphaFactor * 120);
        int backgroundColor = (bgAlpha << 24);
        context.fill(rectX, rectY, rectX + rectWidth, rectY + rectHeight, backgroundColor);
        
        // テキストの色：白色に alpha を乗せた値
        int textAlpha = (int) (alphaFactor * 224);
        int textColor = (textAlpha << 24) | 0xFFFFFF;
        context.drawText(client.textRenderer, currentMessage, x, y, textColor, false);
    }
}
