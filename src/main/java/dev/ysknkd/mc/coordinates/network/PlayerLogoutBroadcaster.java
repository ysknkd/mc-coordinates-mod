package dev.ysknkd.mc.coordinates.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

/**
 * Broadcasts a player's logout information to other clients when the player logs out.
 */
public class PlayerLogoutBroadcaster {

    public static void register() {
        // Register the logout event
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayer disconnectedPlayer = handler.getPlayer();
            broadcastLogout(disconnectedPlayer, server);
        });

        PayloadTypeRegistry.clientboundPlay().register(PlayerLogoutPayload.ID, PlayerLogoutPayload.CODEC);
    }

    /**
     * Notifies all connected players (except the disconnected player) of the logout by sending their UUID.
     *
     * @param disconnectedPlayer The player who has logged out.
     */
    private static void broadcastLogout(ServerPlayer disconnectedPlayer, net.minecraft.server.MinecraftServer server) {
        PlayerLogoutPayload payload = new PlayerLogoutPayload(disconnectedPlayer.getUUID());
        server.getPlayerList().getPlayers().forEach(player -> {
            if (!player.getUUID().equals(disconnectedPlayer.getUUID())
                    && ServerPlayNetworking.canSend(player, PlayerLogoutPayload.ID)) {
                ServerPlayNetworking.send(player, payload);
            }
        });
    }
}
