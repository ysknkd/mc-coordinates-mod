package com.bungggo.mc.hud;

import java.util.HashMap;
import java.util.Map;

import com.bungggo.mc.store.LocationDataListener;
import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;
import com.bungggo.mc.util.Util;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class LocationRenderer implements HudRenderCallback {
    // 色定数
    private static final int COLOR_BLUE = 0x3399FF;
    private static final int COLOR_GRAY = 0xFF9999;
    private static final int COLOR_WHITE = 0xFFFFFF;

    // 前回座標更新間隔（tick 単位）
    private static final int TICK_UPDATE_INTERVAL = 5;
    // メッセージ表示時間（tick 単位）
    private static final float MESSAGE_DURATION_TICKS = 40.0f; // 40 tick = 2秒 (20 tick/sec)
    // 前回更新を行った tick
    private long lastPrevUpdateTick = 0;

    // プレイヤーの前回座標を保持
    private static Double prevPlayerX = 0.0;
    private static Double prevPlayerY = 0.0;
    private static Double prevPlayerZ = 0.0;

    private String savedMessage = "";
    private long messageDisplayTick = 0;

    // 各ピン留めエントリごとの前回の色を保持するマップ
    private final Map<LocationEntry, AxisColor> previousColors = new HashMap<>();
    
    public static void register() {
        HudRenderCallback.EVENT.register(new LocationRenderer());
    }

    private LocationRenderer() {
        LocationDataManager.registerListener(new LocationDataListener() {
            @Override
            public void onEntryAdded(LocationEntry entry) {
                MinecraftClient client = MinecraftClient.getInstance();

                savedMessage = "Location saved!";
                if (client.world != null) {
                    messageDisplayTick = client.world.getTime();
                } else {
                    messageDisplayTick = System.currentTimeMillis() / 50;
                }
            }
        });
    }

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) {
            return;
        }

        updatePrevPlayerCoordinates(client, client.player.getX(), client.player.getY(), client.player.getZ());

        renderCurrentLocation(context, client);
        renderPinnedEntries(context, client);
        renderSavedMessage(context, client, tickCounter);
    }

    private void renderCurrentLocation(DrawContext context, MinecraftClient client) {
        String currentLocation = String.format("X: %.1f, Y: %.1f, Z: %.1f", 
                client.player.getX(), client.player.getY(), client.player.getZ());
        context.drawText(client.textRenderer, currentLocation, 1, 1, COLOR_WHITE, true);
    }

    /**
     * HUD 上にピン留めされた各エントリの座標を描画する。
     */
    private void renderPinnedEntries(DrawContext context, MinecraftClient client) {
        if (!LocationDataManager.hasPinnedEntriesByWorld(Util.getCurrentWorldName(client))) {
            return;
        }
        int yOffset = 20;
        for (LocationEntry pos : LocationDataManager.getPinnedEntriesByWorld(Util.getCurrentWorldName(client))) {
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
}
