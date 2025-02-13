package com.bungggo.mc.hud;

import com.bungggo.mc.store.LocationDataListener;
import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class LocationRenderer implements HudRenderCallback {
    // 色定数 (ARGB形式に変更)
    private static final int COLOR_WHITE = 0xDDFFFFFF;

    public static void register() {
        HudRenderCallback.EVENT.register(new LocationRenderer());
    }

    private LocationRenderer() {
        LocationDataManager.registerListener(new LocationDataListener() {
            @Override
            public void onEntryAdded(LocationEntry entry) {
                MinecraftClient client = MinecraftClient.getInstance();
                if (client == null || client.getToastManager() == null) {
                    return;
                }
                Notification.show("Location saved");
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
        renderCurrentLocation(context, client);
    }

    private void renderCurrentLocation(DrawContext context, MinecraftClient client) {
        String currentLocation = String.format("X: %.1f, Y: %.1f, Z: %.1f", 
                client.player.getX(), client.player.getY(), client.player.getZ());
        context.drawText(client.textRenderer, currentLocation, 1, 1, COLOR_WHITE, true);
    }

}
