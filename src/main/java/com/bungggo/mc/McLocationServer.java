package com.bungggo.mc;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.server.network.ServerPlayerEntity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bungggo.mc.network.LocationPayload;

/**
 * サーバー用の位置情報共有モッド
 *
 * Minecraft 1.21.4 の新しいネットワーキング API（1.20.5 以降の仕様）に沿って、
 * クライアントから送信された位置情報パケット（LocationPayload）を受信し、
 * サーバースレッド上で解析後、送信元の UUID を正しいものに上書きして発信者以外のプレイヤーへブロードキャストします。
 *
 * ※ この例では、CustomPayload を用いた実装例となっています。
 */
public class McLocationServer implements DedicatedServerModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("mc-location-server");

    @Override
    public void onInitializeServer() {
        LOGGER.info("mc-location サーバーモッドを初期化します (Minecraft 1.21.4 用新版ネットワーキング API)");
        // クライアントからサーバへのペイロード用に Codec を登録
        PayloadTypeRegistry.playC2S().register(LocationPayload.ID, LocationPayload.CODEC);
        // サーバからクライアントへ送出するペイロード用の Codec 登録（必要に応じて）
        PayloadTypeRegistry.playS2C().register(LocationPayload.ID, LocationPayload.CODEC);
        registerNetworking();
    }

    /**
     * クライアントから送信された LocationPayload を受信し、
     * サーバースレッド上で処理後、送信元以外の全プレイヤーへブロードキャストします。
     */
    private void registerNetworking() {
        ServerPlayNetworking.registerGlobalReceiver(LocationPayload.ID, (payload, context) -> {
            // ここで受信された payload は、クライアントが送信した位置情報（x, y, z）ですが、
            // サーバー側では信頼できる送信者（context.getPlayer()）の UUID を使用します。
            ServerPlayerEntity senderPlayer = context.player();

            LOGGER.info("プレイヤー {} から位置情報を受信: X: {}, Y: {}, Z: {}",
                    senderPlayer.getName().getString(), payload.x(), payload.y(), payload.z());

            // サーバースレッド上での処理
            context.server().execute(() -> {
                // 発信元の UUID を上書きして、ブロードキャスト用の LocationPayload を作成
                LocationPayload outgoing = new LocationPayload(senderPlayer.getUuid(), payload.x(), payload.y(), payload.z(), payload.description(), payload.world(), payload.pinned());

                // 送信元以外の全プレイヤーに対して、同じ内容を送信
                for (ServerPlayerEntity target : senderPlayer.getServer().getPlayerManager().getPlayerList()) {
                    if (!target.getUuid().equals(senderPlayer.getUuid())) {
                        ServerPlayNetworking.send(target, outgoing);
                    }
                }
            });
        });
    }
} 