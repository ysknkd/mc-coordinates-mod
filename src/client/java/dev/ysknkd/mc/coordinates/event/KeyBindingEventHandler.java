package dev.ysknkd.mc.coordinates.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class KeyBindingEventHandler {

    // Represents a key binding listener with its associated callback.
    public static class KeyBindingListener {
        private final KeyBinding keyBinding;
        private final Consumer<MinecraftClient> callback;

        public KeyBindingListener(KeyBinding keyBinding, Consumer<MinecraftClient> callback) {
            if (keyBinding == null || callback == null) {
                throw new IllegalArgumentException("keyBinding and callback must not be null");
            }
            this.keyBinding = keyBinding;
            this.callback = callback;
        }

        public void tick(MinecraftClient client) {
            while (keyBinding.wasPressed()) {
                callback.accept(client);
            }
        }
    }

    private static final List<KeyBindingListener> listeners = new ArrayList<>();

    /**
     * Registers a listener that executes the specified callback when the given key is pressed.
     *
     * @param keyBinding The key to monitor
     * @param callback   The callback to execute upon key press
     */
    public static void registerListener(KeyBinding keyBinding, Consumer<MinecraftClient> callback) {
        listeners.add(new KeyBindingListener(keyBinding, callback));
    }

    // Check all registered key bindings on each client tick.
    static {
        ClientTickEvents.END_CLIENT_TICK.register((MinecraftClient client) -> {
            for (KeyBindingListener listener : listeners) {
                listener.tick(client);
            }
        });
    }
} 