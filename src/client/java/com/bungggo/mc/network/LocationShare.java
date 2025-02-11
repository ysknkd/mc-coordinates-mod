package com.bungggo.mc.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;

public class LocationShare {

    private static final Logger LOGGER = LoggerFactory.getLogger("mc-location");

    public static void register() {
        // クライアント側の CLIENTBOUND 用ペイロード型を登録
        PayloadTypeRegistry.playC2S().register(LocationPayload.ID, LocationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LocationPayload.ID, LocationPayload.CODEC);

        // グローバルレシーバーを登録してサーバーからブロードキャストされた LocationPayload を受信
        ClientPlayNetworking.registerGlobalReceiver(LocationPayload.ID, (payload, context) -> {
            // ネットワークスレッドからクライアントメインスレッドに切り替え
            context.client().execute(() -> {
                try {
                    // クライアント側で位置情報として取り込む処理（例：LocationDataManagerに追加）
                    LocationDataManager.addEntry(new LocationEntry(payload.x(), payload.y(), payload.z(), payload.description(), payload.world()));
                } catch (Exception e) {
                    LOGGER.error("LocationPayload の受信・デコードに失敗しました", e);
                }
            });
        });
    }
    
    public static void send(LocationEntry entry) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // 送信者は現在のプレイヤーの UUID を利用
            LocationPayload payload = new LocationPayload(
                    client.player.getUuid(),
                    entry.x,
                    entry.y,
                    entry.z,
                    entry.description,
                    entry.world,
                    entry.pinned);
            ClientPlayNetworking.send(payload);
        }
    }
}
