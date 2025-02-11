package com.bungggo.mc.event;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import com.bungggo.mc.screen.LocationListScreen;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;

public class LocationListCommand {

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
    }

}
