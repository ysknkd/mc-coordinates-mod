package dev.ysknkd.mc.coordinates.hud;

import dev.ysknkd.mc.coordinates.store.CoordinatesDataListener;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.CoordinatesApp;
import dev.ysknkd.mc.coordinates.store.Coordinates;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class CoordinatesRenderer implements HudRenderCallback {
    private static final int COLOR_WHITE = 0xDDFFFFFF;
    private static boolean hideCoordinates = false;
    
    // Keybinding per togglare le coordinate con il tasto J
    private static final KeyBinding TOGGLE_COORDINATES_KEY = KeyBindingHelper.registerKeyBinding(
        new KeyBinding(
            "key.coordinates.toggle", // Translation key
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_J,
            "category.coordinates" // Category
        )
    );

    public static void register() {
        HudRenderCallback.EVENT.register(new CoordinatesRenderer());
        
        // Registra il listener per il client tick per gestire i tasti
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (TOGGLE_COORDINATES_KEY.wasPressed()) {
                toggleCoordinateVisibility();
            }
        });
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

    /**
     * Toggle coordinate visibility on HUD.
     */
    public static void toggleCoordinateVisibility() {
        hideCoordinates = !hideCoordinates;
    }

    /**
     * Set coordinate visibility on HUD.
     * @param hidden true to hide coordinates, false to show them
     */
    public static void setCoordinateVisibility(boolean hidden) {
        hideCoordinates = hidden;
    }

    /**
     * Check if coordinates are currently hidden.
     * @return true if coordinates are hidden, false otherwise
     */
    public static boolean areCoordinatesHidden() {
        return hideCoordinates;
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
        // Se le coordinate sono nascoste, non disegnare nulla
        if (hideCoordinates) {
            return;
        }
        
        String currentCoordinates = String.format("X: %.1f, Y: %.1f, Z: %.1f",
                client.player.getX(), client.player.getY(), client.player.getZ());
        
        context.drawText(client.textRenderer, currentCoordinates, 1, 1, COLOR_WHITE, true);
    }
}
