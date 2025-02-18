package dev.ysknkd.mc.coordinates.network;

import dev.ysknkd.mc.coordinates.store.PlayerCoordinatesCache;
import dev.ysknkd.mc.coordinates.store.PlayerCoordinates;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public class PlayerCoordinatesHandler implements ClientPlayNetworking.PlayPayloadHandler<PlayerCoordinatesPayload> {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(PlayerCoordinatesPayload.ID, PlayerCoordinatesPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(PlayerCoordinatesPayload.ID, new PlayerCoordinatesHandler());
    }

    @Override
    public void receive(PlayerCoordinatesPayload payload, Context context) {
        context.client().execute(() -> {
            if (context.client().player != null && !context.client().player.getUuid().equals(payload.uuid())) {
                PlayerCoordinatesCache.update(new PlayerCoordinates(payload.uuid(), payload.x(), payload.y(), payload.z(), payload.name(), payload.world()));
            }
        });
    }

}
