package com.bungggo.mc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
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
import java.util.HashMap;
import java.util.Map;

/**
 * マルチプレイヤー位置管理クライアントクラス
 * <p>
 * ・位置の保存<br>
 * ・HUD 表示（現在位置、ピン留めエントリ、保存メッセージ）<br>
 * ・プレイヤーの動きとの比較により、ピン留めエントリの各軸の色を変化させる  
 *   （前回との差分が縮まれば青、広がれば灰色、変化なければ前回の色を維持）
 * </p>
 */
@Environment(EnvType.CLIENT)
public class McLocationClient implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("mc-location");

    // 色定数
    private static final int COLOR_BLUE = 0x3399FF;
    private static final int COLOR_GRAY = 0xFF9999;
    private static final int COLOR_WHITE = 0xFFFFFF;
    // 前回座標更新間隔（tick 単位）
    private static final int TICK_UPDATE_INTERVAL = 5;
    // メッセージ表示時間（tick 単位）
    private static final float MESSAGE_DURATION_TICKS = 40.0f; // 40 tick = 2秒 (20 tick/sec)

    // 保存キー（G キー）
    private static final KeyBinding SAVE_LOCATION_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mc-location.save_location",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.mc-location"
    ));

    private String savedMessage = "";
    private long messageDisplayTick = 0;
    private boolean showListOnCommand = false;

    // プレイヤーの前回座標を保持
    private static Double prevPlayerX = 0.0;
    private static Double prevPlayerY = 0.0;
    private static Double prevPlayerZ = 0.0;
    // 前回更新を行った tick
    private long lastPrevUpdateTick = 0;

    // 各ピン留めエントリごとの前回の色を保持するマップ
    private final Map<LocationEntry, AxisColor> previousColors = new HashMap<>();

    /**
     * 各軸の色を管理する内部クラス
     */
    private static class AxisColor {
        public int colorX;
        public int colorY;
        public int colorZ;

        public AxisColor(int colorX, int colorY, int colorZ) {
            this.colorX = colorX;
            this.colorY = colorY;
            this.colorZ = colorZ;
        }
    }

    @Override
    public void onInitializeClient() {
        // 永続化されたデータをロードする
        LocationDataManager.load();

        registerCommand();
        registerTickEvents();

        LocationIndicatorRenderer.register();
        HudRenderCallback.EVENT.register(this::onHudRender);
    }

    /**
     * コマンド登録： "/ml" コマンドで LocationListScreen の表示をトグル
     */
    private void registerCommand() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(literal("ml")
                    .executes(context -> {
                        MinecraftClient client = MinecraftClient.getInstance();
                        client.execute(() -> {
                            LOGGER.info("LocationListScreen requested via command");
                            // チャット等を閉じて画面遷移可能にする
                            client.setScreen(null);
                            showListOnCommand = true;
                        });
                        return 1;
                    }));
        });
    }

    /**
     * Tick イベント処理の登録
     */
    private void registerTickEvents() {
        // コマンド実行後に次の tick で LocationListScreen 表示
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (showListOnCommand && client.player != null && client.currentScreen == null) {
                showListOnCommand = false;
                LOGGER.info("Showing LocationListScreen via tick (command)");
                client.setScreen(new LocationListScreen());
            }
        });
        // 位置保存キーおよびその他の Tick 処理
        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);

        // ログイン時：必要に応じて個別のストレージ設定があれば実施
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            if (client.player != null) {
                String userId = client.player.getUuidAsString();
                LOGGER.info("ワールドにログインしたのでデータをロードします。userId: {}", userId);
            }
        });

        // ログアウト時：ストレージにデータを保存
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.info("ワールドからログアウトしたのでデータを保存します。");
            LocationDataManager.save();
        });

        // クライアント終了時にデータ保存
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LOGGER.info("クライアント終了時にデータを保存します。");
            LocationDataManager.save();
        });
    }

    /**
     * Tick 時の処理を行う。
     */
    private void onEndTick(MinecraftClient client) {
        processSaveLocationKey(client);
    }

    /**
     * 保存キー (G キー) が押された場合、現在位置を保存する。
     */
    private void processSaveLocationKey(MinecraftClient client) {
        while (SAVE_LOCATION_KEY.wasPressed()) {
            if (client.player == null) {
                return;
            }
            double x = client.player.getX();
            double y = client.player.getY();
            double z = client.player.getZ();

            LOGGER.info("Location saved: " + String.format("X: %.1f, Y: %.1f, Z: %.1f", x, y, z));
            // LocationDataManager にエントリ追加（内部で保存処理が実施される）
            LocationDataManager.addEntry(new LocationEntry(x, y, z));
            savedMessage = "Location saved!";
            if (client.world != null) {
                messageDisplayTick = client.world.getTime();
            } else {
                messageDisplayTick = System.currentTimeMillis() / 50;
            }
        }
    }

    /**
     * HUD 描画時の処理を統括する。
     */
    private void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        updatePrevPlayerCoordinates(client, client.player.getX(), client.player.getY(), client.player.getZ());

        // 描画処理：現在座標、ピン留めエントリ、保存メッセージ
        renderCurrentLocation(context, client);
        renderPinnedEntries(context, client);
        renderSavedMessage(context, client, tickCounter);
    }

    /**
     * HUD 上に現在のプレイヤー座標を表示する。
     */
    private void renderCurrentLocation(DrawContext context, MinecraftClient client) {
        String currentLocation = String.format("X: %.1f, Y: %.1f, Z: %.1f", 
                client.player.getX(), client.player.getY(), client.player.getZ());
        context.drawText(client.textRenderer, currentLocation, 1, 1, COLOR_WHITE, true);
    }

    /**
     * HUD 上にピン留めされた各エントリの座標を描画する。
     */
    private void renderPinnedEntries(DrawContext context, MinecraftClient client) {
        if (!LocationDataManager.hasPinnedEntries()) {
            return;
        }
        int yOffset = 20;
        for (LocationEntry pos : LocationDataManager.getPinnedEntries()) {
            int xPosition = 1;
            String prefix = "📌";
            context.drawText(client.textRenderer, prefix, xPosition, yOffset, COLOR_WHITE, true);
            xPosition += client.textRenderer.getWidth(prefix);

            int computedColorX = getAxisColor(pos.x, prevPlayerX, client.player.getX());
            int computedColorY = getAxisColor(pos.y, prevPlayerY, client.player.getY());
            int computedColorZ = getAxisColor(pos.z, prevPlayerZ, client.player.getZ());

            // 前回保持していた色があり、今回の計算結果が白の場合は保持色を利用
            AxisColor storedColor = previousColors.get(pos);
            if (storedColor != null) {
                if (computedColorX == COLOR_WHITE) {
                    computedColorX = storedColor.colorX;
                }
                if (computedColorY == COLOR_WHITE) {
                    computedColorY = storedColor.colorY;
                }
                if (computedColorZ == COLOR_WHITE) {
                    computedColorZ = storedColor.colorZ;
                }
            }

            String xStr = String.format("X: %.1f", pos.x);
            context.drawText(client.textRenderer, xStr, xPosition, yOffset, computedColorX, true);
            xPosition += client.textRenderer.getWidth(xStr);

            String yStr = String.format(", Y: %.1f", pos.y);
            context.drawText(client.textRenderer, yStr, xPosition, yOffset, computedColorY, true);
            xPosition += client.textRenderer.getWidth(yStr);

            String zStr = String.format(", Z: %.1f", pos.z);
            context.drawText(client.textRenderer, zStr, xPosition, yOffset, computedColorZ, true);

            // 現在の色を保存
            previousColors.put(pos, new AxisColor(computedColorX, computedColorY, computedColorZ));
            yOffset += client.textRenderer.fontHeight;
        }
    }

    /**
     * HUD 上に保存メッセージをフェードアウトさせて描画する。
     */
    private void renderSavedMessage(DrawContext context, MinecraftClient client, RenderTickCounter tickCounter) {
        if (savedMessage.isEmpty() || client.world == null) {
            return;
        }
        long currentTick = client.world.getTime();
        float elapsedTicks = (currentTick - messageDisplayTick) + tickCounter.getTickDelta(true);
        if (elapsedTicks < MESSAGE_DURATION_TICKS) {
            int alpha = (int) (255 * (1 - (elapsedTicks / MESSAGE_DURATION_TICKS)));
            if (alpha > 10) {
                int color = (alpha << 24) | COLOR_WHITE;
                context.drawText(client.textRenderer, savedMessage, 1, 10, color, true);
            }
        } else {
            savedMessage = "";
            messageDisplayTick = 0;
        }
    }

    /**
     * 前回のプレイヤー座標を一定間隔ごとに更新する。
     */
    private void updatePrevPlayerCoordinates(MinecraftClient client, double currentX, double currentY, double currentZ) {
        if (client.world != null) {
            long currentTick = client.world.getTime();
            if (currentTick - lastPrevUpdateTick >= TICK_UPDATE_INTERVAL) {
                prevPlayerX = currentX;
                prevPlayerY = currentY;
                prevPlayerZ = currentZ;
                lastPrevUpdateTick = currentTick;
            }
        } else {
            prevPlayerX = currentX;
            prevPlayerY = currentY;
            prevPlayerZ = currentZ;
        }
    }

    /**
     * 指定されたピン留め値と、前回・現在のプレイヤー座標との差から適用する色を決定する。
     *
     * @param pinned        ピン留めされた値
     * @param prevPlayer    前回のプレイヤー座標
     * @param currentPlayer 現在のプレイヤー座標
     * @return 選択された色コード
     */
    private int getAxisColor(double pinned, double prevPlayer, double currentPlayer) {
        double prevDiff = Math.abs(pinned - prevPlayer);
        double currentDiff = Math.abs(pinned - currentPlayer);
        if (currentDiff < prevDiff) {
            return COLOR_BLUE;
        } else if (currentDiff > prevDiff) {
            return COLOR_GRAY;
        }
        return COLOR_WHITE;
    }
} 