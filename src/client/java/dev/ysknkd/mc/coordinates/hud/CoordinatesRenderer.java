package dev.ysknkd.mc.coordinates.hud;

import dev.ysknkd.mc.coordinates.store.CoordinatesDataListener;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.store.Coordinates;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class CoordinatesRenderer implements HudRenderCallback {
    // 色定数 (ARGB形式に変更)
    private static final int COLOR_WHITE = 0xDDFFFFFF;

    public static void register() {
        HudRenderCallback.EVENT.register(new CoordinatesRenderer());
    }

    private CoordinatesRenderer() {
        CoordinatesDataManager.registerListener(new CoordinatesDataListener() {
            @Override
            public void onEntryAdded(Coordinates entry) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null || client.getToastManager() == null) {
                    return;
                }
                Notification.show(Text.translatable("modid.coordinates_save.success").getString());
            }
        });
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        // 現在座標の描画
        renderCurrentCoordinates(context, client);
    }

    private void renderCurrentCoordinates(DrawContext context, MinecraftClient client) {
        String currentCoordinates = String.format("X: %.1f, Y: %.1f, Z: %.1f",
                client.player.getX(), client.player.getY(), client.player.getZ());
        context.drawText(client.textRenderer, currentCoordinates, 1, 1, COLOR_WHITE, true);
    }

}
