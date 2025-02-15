package com.bungggo.mc.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bungggo.mc.store.PlayerLocationCache;
import com.bungggo.mc.store.PlayerLocationEntity;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PlayerLocationHandler implements ClientPlayNetworking.PlayPayloadHandler<PlayerLocationPayload> {

    private static final Logger LOGGER = LoggerFactory.getLogger("mc-location");

    public static void register() {
        PayloadTypeRegistry.playS2C().register(PlayerLocationPayload.ID, PlayerLocationPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(PlayerLocationPayload.ID, new PlayerLocationHandler());
    }

    @Override
    public void receive(PlayerLocationPayload payload, Context context) {
        context.client().execute(() -> {
            LOGGER.info("receive: {}", payload.uuid());
            LOGGER.info("x = {}, y = {}, z = {}", payload.x(), payload.y(), payload.z());
            if (context.client().player != null && !context.client().player.getUuid().equals(payload.uuid())) {
                LOGGER.info("update: {}", payload.uuid());
                PlayerLocationCache.update(new PlayerLocationEntity(payload.uuid(), payload.x(), payload.y(), payload.z(), payload.world()));
            }
        });
    }

}
