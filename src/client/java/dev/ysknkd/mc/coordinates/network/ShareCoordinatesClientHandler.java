package dev.ysknkd.mc.coordinates.network;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.store.Coordinates;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;

public class ShareCoordinatesClientHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatesApp.MOD_ID);

    public static void register() {
        PayloadTypeRegistry.playC2S().register(ShareCoordinatesPayload.ID, ShareCoordinatesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ShareCoordinatesPayload.ID, ShareCoordinatesPayload.CODEC);

        ClientPlayNetworking.registerGlobalReceiver(ShareCoordinatesPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                try {
                    CoordinatesDataManager.addOrUpdateEntry(new Coordinates(payload.uuid(), payload.x(), payload.y(), payload.z(), payload.description(), payload.world(), payload.pinned(), payload.icon()));
                } catch (Exception e) {
                    LOGGER.error("Failed to receive/decode payload", e);
                }
            });
        });
    }
    
    public static void send(Coordinates entry) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            // Use the current player's UUID for the sender
            ShareCoordinatesPayload payload = new ShareCoordinatesPayload(
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
