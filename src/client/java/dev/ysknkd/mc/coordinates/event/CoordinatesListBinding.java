package dev.ysknkd.mc.coordinates.event;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommands.literal;

import org.lwjgl.glfw.GLFW;

import dev.ysknkd.mc.coordinates.screen.CoordinatesListScreen;
import dev.ysknkd.mc.coordinates.CoordinatesApp;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public class CoordinatesListBinding {

    // Key binding for displaying the coordinates list (B key)
    private static final KeyMapping SHOW_COORDINATES_LIST_KEY = KeyMappingHelper.registerKeyMapping(
            new KeyMapping("key." + CoordinatesApp.MOD_ID + ".show_coordinates_list",
                           InputConstants.Type.KEYSYM,
                           GLFW.GLFW_KEY_B,
                           CoordinatesSaveKeyBinding.COORDINATES_CATEGORY));
    
    private static boolean showListOnCommand = false;
    
    /**
     * Registers the "/ml" command to toggle the display of the CoordinatesListScreen.
     */
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("ml")
                    .executes(context -> {
                        Minecraft client = Minecraft.getInstance();
                        client.execute(() -> {
                            // Close any open chat or overlays to allow screen transition
                            client.gui.setScreen(null);
                            showListOnCommand = true;
                        });
                        return 1;
                    }));
        });

        // On the next tick, if showListOnCommand is true and the player exists with no open screen, show the CoordinatesListScreen.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (showListOnCommand && client.player != null && client.gui.screen() == null) {
                showListOnCommand = false;
                client.gui.setScreen(new CoordinatesListScreen());
            }
        });

        // Register key binding listener to open the coordinates list screen
        KeyBindingEventHandler.registerListener(SHOW_COORDINATES_LIST_KEY, client -> {
            if (client.player != null && client.gui.screen() == null) {
                client.gui.setScreen(new CoordinatesListScreen());
            }
        });
    }

}
