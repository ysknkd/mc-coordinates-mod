package com.bungggo.mc;

import net.fabricmc.api.DedicatedServerModInitializer;

import com.bungggo.mc.network.PlayerLocationBroadcaster;
import com.bungggo.mc.network.ShareLocationHandler;

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

    @Override
    public void onInitializeServer() {
        ShareLocationHandler.register();
        PlayerLocationBroadcaster.register();
    }

} 