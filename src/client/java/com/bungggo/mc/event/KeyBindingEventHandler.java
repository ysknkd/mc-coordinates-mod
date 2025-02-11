package com.bungggo.mc.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Consumer;

public class KeyBindingEventHandler {

    // 個々のキーリスナ（key binding とそのコールバック）
    public static class KeyBindingListener {
        private final KeyBinding keyBinding;
        private final Consumer<MinecraftClient> callback;

        public KeyBindingListener(KeyBinding keyBinding, Consumer<MinecraftClient> callback) {
            if (keyBinding == null || callback == null) {
                throw new IllegalArgumentException("keyBinding と callback は null にできません");
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
     * 指定した KeyBinding が押下されたときに、指定のコールバックを呼び出すリスナーを登録します。
     *
     * @param keyBinding 監視するキー
     * @param callback   キーが押下された際に呼び出されるコールバック
     */
    public static void registerListener(KeyBinding keyBinding, Consumer<MinecraftClient> callback) {
        listeners.add(new KeyBindingListener(keyBinding, callback));
    }

    // 全ての登録済み KeyBinding をティック毎にチェックする
    static {
        ClientTickEvents.END_CLIENT_TICK.register((MinecraftClient client) -> {
            for (KeyBindingListener listener : listeners) {
                listener.tick(client);
            }
        });
    }
} 