package dev.ysknkd.mc.coordinates.hud;

import dev.ysknkd.mc.coordinates.store.CoordinatesDataListener;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.CoordinatesApp;
import dev.ysknkd.mc.coordinates.store.Coordinates;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class CoordinatesRenderer implements HudRenderCallback {
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
                Notification.show(Text.translatable(CoordinatesApp.MOD_ID + ".coordinates_save.success").getString());
            }
        });
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        renderCurrentCoordinates(context, client);
    }

    private void renderCurrentCoordinates(DrawContext context, MinecraftClient client) {
        String currentCoordinates = String.format("X: %.1f, Y: %.1f, Z: %.1f",
                client.player.getX(), client.player.getY(), client.player.getZ());
        context.drawText(client.textRenderer, currentCoordinates, 1, 1, COLOR_WHITE, true);
    }

}
