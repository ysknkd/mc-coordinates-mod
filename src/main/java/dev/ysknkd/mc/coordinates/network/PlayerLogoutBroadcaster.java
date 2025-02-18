package dev.ysknkd.mc.coordinates.network;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * プレイヤーがログアウトした際、その情報を他のクライアントへブロードキャストするクラスです。
 */
public class PlayerLogoutBroadcaster {

    public static void register() {
        // ログアウトイベントを登録
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ServerPlayerEntity disconnectedPlayer = handler.getPlayer();
            broadcastLogout(disconnectedPlayer);
        });

        // ログアウトペイロードの型登録（サーバーからクライアントへの送信用）
        PayloadTypeRegistry.playS2C().register(PlayerLogoutPayload.ID, PlayerLogoutPayload.CODEC);
    }

    /**
     * 接続中の各プレイヤーへ、ログアウトしたプレイヤーのUUIDを通知します。
     *
     * @param disconnectedPlayer ログアウトしたプレイヤー
     */
    private static void broadcastLogout(ServerPlayerEntity disconnectedPlayer) {
        PlayerLogoutPayload payload = new PlayerLogoutPayload(disconnectedPlayer.getUuid());
        // 接続中の各プレイヤー（ログアウトしたプレイヤー自身を除く）に送信
        disconnectedPlayer.getServer().getPlayerManager().getPlayerList().forEach(player -> {
            if (!player.getUuid().equals(disconnectedPlayer.getUuid())) {
                ServerPlayNetworking.send(player, payload);
            }
        });
    }
} 