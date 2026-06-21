package dev.ysknkd.mc.coordinates.event;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import dev.ysknkd.mc.coordinates.config.Config;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.store.Coordinates;
import dev.ysknkd.mc.coordinates.util.IconTexture;
import dev.ysknkd.mc.coordinates.util.Util;
import dev.ysknkd.mc.coordinates.CoordinatesApp;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

/**
 * Handles the key input for saving coordinate data (G key).
 */
public class CoordinatesSaveKeyBinding implements Consumer<Minecraft> {

    // Key binding category - shared with other key bindings
    public static final KeyMapping.Category COORDINATES_CATEGORY =
        KeyMapping.Category.register(Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "main"));

    // Key binding for saving coordinates (G key)
    private static final KeyMapping SAVE_COORDINATES_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key." + CoordinatesApp.MOD_ID + ".save_coordinates",
            InputConstants.Type.KEYSYM,
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
    public void accept(Minecraft client) {
        if (client.player == null || client.level == null) {
            return;
        }

        // Raycast to determine the position from the center of the screen.
        HitResult hitResult = client.player.pick(3, 1.0F, false);
        Vec3 hitPos = hitResult.getLocation();
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
