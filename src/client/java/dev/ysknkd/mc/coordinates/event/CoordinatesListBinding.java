package dev.ysknkd.mc.coordinates.event;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import org.lwjgl.glfw.GLFW;

import dev.ysknkd.mc.coordinates.screen.CoordinatesListScreen;
import dev.ysknkd.mc.coordinates.CoordinatesApp;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class CoordinatesListBinding {

    // Key binding for displaying the coordinates list (B key)
    private static final KeyBinding SHOW_COORDINATES_LIST_KEY = KeyBindingHelper.registerKeyBinding(
            new KeyBinding("key." + CoordinatesApp.MOD_ID + ".show_coordinates_list",
                           InputUtil.Type.KEYSYM,
                           GLFW.GLFW_KEY_B,
                           "category." + CoordinatesApp.MOD_ID));
    
    private static boolean showListOnCommand = false;
    
    /**
     * Registers the "/ml" command to toggle the display of the CoordinatesListScreen.
     */
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("ml")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        client.execute(() -> {
                            // Close any open chat or overlays to allow screen transition
                            client.setScreen(null);
                            showListOnCommand = true;
                        });
                        return 1;
                    }));
        });

        // On the next tick, if showListOnCommand is true and the player exists with no open screen, show the CoordinatesListScreen.
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (showListOnCommand && client.player != null && client.currentScreen == null) {
                showListOnCommand = false;
                client.setScreen(new CoordinatesListScreen());
            }
        });

        // Register key binding listener to open the coordinates list screen
        KeyBindingEventHandler.registerListener(SHOW_COORDINATES_LIST_KEY, client -> {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new CoordinatesListScreen());
            }
        });
    }

}
