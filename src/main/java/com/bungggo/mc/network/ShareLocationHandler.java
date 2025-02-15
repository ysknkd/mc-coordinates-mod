package com.bungggo.mc.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.PlayPayloadHandler;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShareLocationHandler implements PlayPayloadHandler<ShareLocationPayload> {
    
    public static void register() {
        PayloadTypeRegistry.playC2S().register(ShareLocationPayload.ID, ShareLocationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShareLocationPayload.ID, ShareLocationPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ShareLocationPayload.ID, new ShareLocationHandler());
    }

    @Override
    public void receive(ShareLocationPayload payload, Context context) {
        ServerPlayerEntity senderPlayer = context.player();

        // サーバースレッド上での処理
        context.server().execute(() -> {
            // 発信元の UUID を上書きして、ブロードキャスト用の LocationPayload を作成
            ShareLocationPayload outgoing = new ShareLocationPayload(senderPlayer.getUuid(), payload.uuid(), payload.x(),
                    payload.y(), payload.z(), payload.description(), payload.world(), payload.pinned(), payload.icon());

            // 送信元以外の全プレイヤーに対して、同じ内容を送信
            for (ServerPlayerEntity target : senderPlayer.getServer().getPlayerManager().getPlayerList()) {
                if (!target.getUuid().equals(senderPlayer.getUuid())) {
                    ServerPlayNetworking.send(target, outgoing);
                }
            }
        });
    }
}
