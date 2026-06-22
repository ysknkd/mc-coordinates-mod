package dev.ysknkd.mc.coordinates.hud;

import dev.ysknkd.mc.coordinates.store.CoordinatesDataListener;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.CoordinatesApp;
import dev.ysknkd.mc.coordinates.store.Coordinates;

import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class CoordinatesRenderer implements HudElement {
    private static final int COLOR_WHITE = 0xDDFFFFFF;

    public static void register() {
        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "coordinates"),
                new CoordinatesRenderer());
    }

    private CoordinatesRenderer() {
        CoordinatesDataManager.registerListener(new CoordinatesDataListener() {
            @Override
            public void onEntryAdded(Coordinates entry) {
                Minecraft client = Minecraft.getInstance();
                if (client == null) {
                    return;
                }
                Notification.show(Component.translatable(CoordinatesApp.MOD_ID + ".coordinates_save.success").getString());
            }
        });
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null) {
            return;
        }
        renderCurrentCoordinates(context, client);
    }

    private void renderCurrentCoordinates(GuiGraphicsExtractor context, Minecraft client) {
        String currentCoordinates = String.format("X: %.1f, Y: %.1f, Z: %.1f",
                client.player.getX(), client.player.getY(), client.player.getZ());
        context.text(client.font, currentCoordinates, 1, 1, COLOR_WHITE, true);
    }

}
