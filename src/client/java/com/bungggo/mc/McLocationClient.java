package com.bungggo.mc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.render.*;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.MinecraftClient;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

public class McLocationClient implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("mc-location");

    private static final KeyBinding saveLocationKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mc-location.save_location",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.mc-location"
    ));

    private String savedMessage = "";
    private long messageDisplayTick = 0;
    private static final float MESSAGE_DURATION_TICKS = 40.0f; // 40 tick = 2秒 (20 tick/sec)
    private boolean showListOnCommand = false;

    @Override
    public void onInitializeClient() {
        // コマンド登録: /mc-location list でリスト表示をトグル
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("ml")
                            .executes(context -> {
                                MinecraftClient client = MinecraftClient.getInstance();
                                client.execute(() -> {
                                    LOGGER.info("LocationListScreen requested via command");
                                    // コマンド実行時にチャットなどのスクリーンを閉じる
                                    client.setScreen(null);
                                    // 次の tick で LocationListScreen を表示するためのフラグを ON
                                    showListOnCommand = true;
                                });
                                return 1;
                            }));
        });

        // Tick イベント：コマンド実行後に次のティックでスクリーンを表示する
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (showListOnCommand && client.player != null && client.currentScreen == null) {
                showListOnCommand = false;
                LOGGER.info("Showing LocationListScreen via tick (command)");
                client.setScreen(new LocationListScreen());
            }
        });
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    private void onEndTick(MinecraftClient client) {
        while (saveLocationKey.wasPressed()) {
            if (client.player == null) {
                return;
            }
            double x = client.player.getX();
            double y = client.player.getY();
            double z = client.player.getZ();
            String locationText = String.format("X: %.1f, Y: %.1f, Z: %.1f", x, y, z);

            LOGGER.info("Location saved: " + locationText);
            LocationDataManager.addEntry(new LocationEntry(locationText));
            savedMessage = "Location saved!";
            if (client.world != null) {
                messageDisplayTick = client.world.getTime();
            } else {
                messageDisplayTick = System.currentTimeMillis() / 50;
            }
        }
    }

    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            double x = client.player.getX();
            double y = client.player.getY();
            double z = client.player.getZ();
            String locationText = String.format("X: %.1f, Y: %.1f, Z: %.1f", x, y, z);
            context.drawText(client.textRenderer, locationText, 1, 1, 0xFFFFFF, true);

            if (LocationDataManager.hasPinnedEntries()) {
                int yOffset = 20;
                for (LocationEntry pos : LocationDataManager.getPinnedEntries()) {
                    context.drawText(client.textRenderer, "📌" + pos.text, 1, yOffset, 0xFFFFFF, true);
                    yOffset += 10;
                }
            }

            // tickベースのフェードアウト処理
            if (!savedMessage.isEmpty() && client.world != null) {
                long currentTick = client.world.getTime();
                float elapsedTicks = ((float) (currentTick - messageDisplayTick)) + tickCounter.getTickDelta(true);
                if (elapsedTicks < MESSAGE_DURATION_TICKS) {
                    int alpha = (int) (255 * (1 - (elapsedTicks / MESSAGE_DURATION_TICKS)));
                    if (alpha > 10) {
                        int color = (alpha << 24) | 0xFFFFFF;
                        context.drawText(client.textRenderer, savedMessage, 1, 10, color, true);
                    }
                } else {
                    savedMessage = "";
                    messageDisplayTick = 0;
                }
            }
        }
    }
} 