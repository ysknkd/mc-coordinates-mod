package dev.ysknkd.mc.coordinates.network;

import dev.ysknkd.mc.coordinates.store.PlayerCoordinatesCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;

/**
 * Client-side handler that removes the corresponding player's data from the cache
 * when a logout payload is received.
 */
public class PlayerLogoutClientHandler implements ClientPlayNetworking.PlayPayloadHandler<PlayerLogoutPayload> {

    public static void register() {
        // Register the payload type for logout (for server-to-client communication)
        PayloadTypeRegistry.playS2C().register(PlayerLogoutPayload.ID, PlayerLogoutPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(PlayerLogoutPayload.ID, new PlayerLogoutClientHandler());
    }

    @Override
    public void receive(PlayerLogoutPayload payload, Context context) {
        MinecraftClient client = context.client();
        client.execute(() -> {
            // Remove the logged-out player's data from the cache
            PlayerCoordinatesCache.remove(payload.uuid());
        });
    }
} 