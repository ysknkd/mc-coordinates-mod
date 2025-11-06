package dev.ysknkd.mc.coordinates.network;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerCoordinatesBroadcaster implements ServerTickEvents.EndTick {

    public static void register() {
        PayloadTypeRegistry.playS2C().register(PlayerCoordinatesPayload.ID, PlayerCoordinatesPayload.CODEC);
        ServerTickEvents.END_SERVER_TICK.register(new PlayerCoordinatesBroadcaster());
    }

    @Override
    public void onEndTick(MinecraftServer server) {
        // Broadcast coordinate data every 20 ticks (approximately 1 second interval)
        if (server.getTicks() % 20 == 0) {
            send(server);
        }
    }

    private void send(MinecraftServer server) {
        // For each recipient, send individual coordinate data of every other player
        for (ServerPlayerEntity recipient : server.getPlayerManager().getPlayerList()) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                if (!player.getUuid().equals(recipient.getUuid())) {
                    String world = player.getEntityWorld().getRegistryKey().getValue().toString();
                    PlayerCoordinatesPayload payload = new PlayerCoordinatesPayload(
                        player.getUuid(),
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        player.getName().getString(),
                        world
                    );
                    ServerPlayNetworking.send(recipient, payload);
                }
            }
        }
    }
} 