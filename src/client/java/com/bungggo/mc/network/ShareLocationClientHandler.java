package com.bungggo.mc.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;

public class ShareLocationClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger("mc-location");

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ShareLocationPayload.ID, ShareLocationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShareLocationPayload.ID, ShareLocationPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ShareLocationPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    LocationDataManager.addOrUpdateEntry(new LocationEntry(payload.uuid(), payload.x(), payload.y(), payload.z(), payload.description(), payload.world(), payload.pinned(), payload.icon()));
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
            ShareLocationPayload payload = new ShareLocationPayload(
                    client.player.getUuid(),
                    entry.uuid,
                    entry.x,
                    entry.y,
                    entry.z,
                    entry.description,
                    entry.world,
                    entry.pinned,
                    entry.icon);
            ClientPlayNetworking.send(payload);
        }
    }
}
