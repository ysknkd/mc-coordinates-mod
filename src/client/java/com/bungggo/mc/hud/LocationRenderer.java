package com.bungggo.mc.hud;

import java.util.HashMap;
import java.util.Map;

import com.bungggo.mc.store.LocationDataListener;
import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;
import com.bungggo.mc.util.IconTextureMap;
import com.bungggo.mc.util.Util;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;

public class LocationRenderer implements HudRenderCallback {
    // 色定数 (ARGB形式に変更)
    private static final int COLOR_BLUE = 0xDD3399FF;
    private static final int COLOR_RED = 0xDDFF9999;
    private static final int COLOR_WHITE = 0xDDFFFFFF;

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

    // 保存メッセージ表示用
    private String savedMessage = "";
    private long messageDisplayTick = 0;

    // 各エントリごとに最後の表示色（青または赤）を保持するマップ
    private final Map<LocationEntry, Integer> lastDistanceColors = new HashMap<>();
    
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
     * HUD 上にピン留めされた各エントリのアイコンと説明文を描画します。<br>
     * プレイヤーとの距離変化に応じ、近づいていれば青、遠ざかっていれば赤の色で表示し、<br>
     * 色が変化した後は直前の色を保持します。
     */
    private void renderPinnedEntries(DrawContext context, MinecraftClient client) {
        String currentWorldName = Util.getCurrentWorldName(client);
        if (!LocationDataManager.hasPinnedEntriesByWorld(currentWorldName)) {
            return;
        }
        int yOffset = 20;
        for (LocationEntry entry : LocationDataManager.getPinnedEntriesByWorld(currentWorldName)) {
            int xPosition = 1;
            // 距離に応じて色を切り替える
            double currDist = Math.sqrt(
                    Math.pow(client.player.getX() - entry.x, 2) +
                    Math.pow(client.player.getY() - entry.y, 2) +
                    Math.pow(client.player.getZ() - entry.z, 2)
            );
            double prevDist = Math.sqrt(
                    Math.pow(prevPlayerX - entry.x, 2) +
                    Math.pow(prevPlayerY - entry.y, 2) +
                    Math.pow(prevPlayerZ - entry.z, 2)
            );
            int computedColor = COLOR_WHITE;
            if (currDist < prevDist) {
                computedColor = COLOR_BLUE;
            } else if (currDist > prevDist) {
                computedColor = COLOR_RED;
            }
            // computedColor が白であれば、直前の色（青または赤）を保持
            if (computedColor != COLOR_WHITE) {
                lastDistanceColors.put(entry, computedColor);
            }
            int entryColor = lastDistanceColors.getOrDefault(entry, COLOR_WHITE);

            int iconSize = 16;  // アイコンサイズ
            // tint 付きでアイコンを描画（DrawContext.drawTexture のオーバーロードを使用）
            context.drawTexture(
                RenderLayer::getGuiTextured, 
                IconTextureMap.getTexture(entry.icon), 
                xPosition, yOffset, 
                0.0F, 0.0F, 
                iconSize, iconSize, 
                iconSize, iconSize, 
                entryColor
            );
            xPosition += iconSize + 3;
            // 説明文も同じ色で描画
            context.drawText(
                client.textRenderer,
                Text.literal(entry.description),
                xPosition,
                yOffset + (iconSize - client.textRenderer.fontHeight) / 2,
                entryColor,
                true
            );

            yOffset += iconSize + 4;
        }
    }

    /**
     * HUD 上に保存メッセージをフェードアウトさせて描画します。
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
     * 前回のプレイヤー座標を一定間隔ごとに更新します。
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
