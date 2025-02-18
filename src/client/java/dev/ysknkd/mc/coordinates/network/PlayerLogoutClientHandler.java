package dev.ysknkd.mc.coordinates.network;

import dev.ysknkd.mc.coordinates.store.PlayerCoordinatesCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.client.MinecraftClient;

/**
 * クライアント側でログアウトペイロードを受信した際に、
 * プレイヤーの位置情報キャッシュから該当プレイヤーのデータを削除します。
 */
public class PlayerLogoutClientHandler implements ClientPlayNetworking.PlayPayloadHandler<PlayerLogoutPayload> {

    public static void register() {
        // ログアウトペイロードの型登録（クライアント側）
        PayloadTypeRegistry.playS2C().register(PlayerLogoutPayload.ID, PlayerLogoutPayload.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(PlayerLogoutPayload.ID, new PlayerLogoutClientHandler());
    }

    @Override
    public void receive(PlayerLogoutPayload payload, Context context) {
        MinecraftClient client = context.client();
        client.execute(() -> {
            // キャッシュからログアウトしたプレイヤーのデータを削除
            PlayerCoordinatesCache.remove(payload.uuid());
        });
    }
} 