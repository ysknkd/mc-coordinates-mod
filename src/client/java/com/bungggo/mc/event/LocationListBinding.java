package com.bungggo.mc.event;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import org.lwjgl.glfw.GLFW;

import com.bungggo.mc.screen.LocationListScreen;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class LocationListBinding {

    // 保存キー（V キー）
    private static final KeyBinding SHOW_LOCATION_LIST_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mc-location.show_location_list",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.mc-location"
    ));
    
    private static boolean showListOnCommand = false;
    
    /**
     * コマンド登録： "/ml" コマンドで LocationListScreen の表示をトグル
     */
    public static void register() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("ml")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        client.execute(() -> {
                            // チャット等を閉じて画面遷移可能にする
                            client.setScreen(null);
                            showListOnCommand = true;
                        });
                        return 1;
                    }));
        });

        // コマンド実行後に次の tick で LocationListScreen 表示
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (showListOnCommand && client.player != null && client.currentScreen == null) {
                showListOnCommand = false;
                client.setScreen(new LocationListScreen());
            }
        });

        // キー押下時の処理
        KeyBindingEventHandler.registerListener(SHOW_LOCATION_LIST_KEY, client -> {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new LocationListScreen());
            }
        });
    }

}
