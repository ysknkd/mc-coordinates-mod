package dev.ysknkd.mc.coordinates.event;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import dev.ysknkd.mc.coordinates.config.Config;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.store.Coordinates;
import dev.ysknkd.mc.coordinates.util.IconTexture;
import dev.ysknkd.mc.coordinates.util.Util;
import dev.ysknkd.mc.coordinates.CoordinatesApp;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * Handles the key input for saving coordinate data (G key).
 */
public class CoordinatesSaveKeyBinding implements Consumer<MinecraftClient> {

    // Key binding category
    private static final KeyBinding.Category COORDINATES_CATEGORY =
        KeyBinding.Category.create(net.minecraft.util.Identifier.of(CoordinatesApp.MOD_ID, "main"));

    // Key binding for saving coordinates (G key)
    private static final KeyBinding SAVE_COORDINATES_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key." + CoordinatesApp.MOD_ID + ".save_coordinates",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            COORDINATES_CATEGORY
    ));
    
    public static void register() {
        // Register the key binding listener and tick handler for saving coordinate data
        KeyBindingEventHandler.registerListener(SAVE_COORDINATES_KEY, new CoordinatesSaveKeyBinding());
    }

    /**
     * Called when the save key (G) is pressed.
     * Saves the current position, world information, and configuration-based pin state.
     */
    @Override
    public void accept(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        // Raycast to determine the position from the center of the screen.
        HitResult hitResult = client.player.raycast(3, 1.0F, false);
        Vec3d hitPos = hitResult.getPos();
        double x = hitPos.x;
        double y = hitPos.y;
        double z = hitPos.z;
        
        String worldName = Util.getCurrentWorldName(client);
        String description = Util.getBiome(client);
        String icon = IconTexture.getIconName(client);

        // Create a new Coordinates entry using the current configuration's pin state.
        Coordinates entry = new Coordinates(x, y, z, description, worldName, Config.getDefaultPinState(), icon);
        CoordinatesDataManager.addOrUpdateEntry(entry);
    }
}
