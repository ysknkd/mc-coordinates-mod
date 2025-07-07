package dev.ysknkd.mc.coordinates.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Broadcasts a player's logout information to other clients when the player logs out.
 */
public class PlayerLogoutBroadcaster {

    public static void register() {
        // Register the logout event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity disconnectedPlayer = handler.getPlayer();
            broadcastLogout(disconnectedPlayer);
        });

        PayloadTypeRegistry.playS2C().register(PlayerLogoutPayload.ID, PlayerLogoutPayload.CODEC);
    }

    /**
     * Notifies all connected players (except the disconnected player) of the logout by sending their UUID.
     *
     * @param disconnectedPlayer The player who has logged out.
     */
    private static void broadcastLogout(ServerPlayerEntity disconnectedPlayer) {
        PlayerLogoutPayload payload = new PlayerLogoutPayload(disconnectedPlayer.getUuid());
        disconnectedPlayer.getServer().getPlayerManager().getPlayerList().forEach(player -> {
            if (!player.getUuid().equals(disconnectedPlayer.getUuid())) {
                ServerPlayNetworking.send(player, payload);
            }
        });
    }
} 