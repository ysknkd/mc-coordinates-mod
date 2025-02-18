package dev.ysknkd.mc.coordinates.event;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

import org.lwjgl.glfw.GLFW;

import dev.ysknkd.mc.coordinates.screen.CoordinatesListScreen;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class CoordinatesListBinding {

    // 保存キー（V キー）
    private static final KeyBinding SHOW_COORDINATES_LIST_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mc-coordinates.show_coordinates_list",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.mc-coordinates"
    ));
    
    private static boolean showListOnCommand = false;
    
    /**
     * コマンド登録： "/ml" コマンドで CoordinatesListScreen の表示をトグル
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

        // コマンド実行後に次の tick で CoordinatesListScreen 表示
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (showListOnCommand && client.player != null && client.currentScreen == null) {
                showListOnCommand = false;
                client.setScreen(new CoordinatesListScreen());
            }
        });

        // キー押下時の処理
        KeyBindingEventHandler.registerListener(SHOW_COORDINATES_LIST_KEY, client -> {
            if (client.player != null && client.currentScreen == null) {
                client.setScreen(new CoordinatesListScreen());
            }
        });
    }

}
