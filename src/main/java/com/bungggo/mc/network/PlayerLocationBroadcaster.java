package com.bungggo.mc.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerLocationBroadcaster implements ServerTickEvents.EndTick {

    // サーバー起動時にこのメソッドで登録します
    public static void register() {
        PayloadTypeRegistry.playS2C().register(PlayerLocationPayload.ID, PlayerLocationPayload.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(new PlayerLocationBroadcaster());
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        // 20 tick 毎（約1秒間隔）に位置情報を配信
        if (server.getTicks() % 20 == 0) {
            send(server);
        }
    }

    private void send(MinecraftServer server) {
        // 各受信者 (recipient) ごとに、recipient 以外の各プレイヤーの位置情報を個別送信
        for (ServerPlayerEntity recipient : server.getPlayerManager().getPlayerList()) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!player.getUuid().equals(recipient.getUuid())) {
                    String world = player.getWorld().getRegistryKey().getValue().toString();
                    PlayerLocationPayload payload = new PlayerLocationPayload(
                        player.getUuid(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        world
                    );
                    ServerPlayNetworking.send(recipient, payload);
                }
            }
        }
    }
} 