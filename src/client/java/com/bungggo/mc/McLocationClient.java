package com.bungggo.mc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.bungggo.mc.config.LocationConfig;
import com.bungggo.mc.event.LocationListBinding;
import com.bungggo.mc.event.LocationSaveKeyBinding;
import com.bungggo.mc.hud.LocationRenderer;
import com.bungggo.mc.hud.Notification;
import com.bungggo.mc.hud.PlayerIndicatorRenderer;
import com.bungggo.mc.network.PlayerLocationHandler;
import com.bungggo.mc.network.ShareLocationClientHandler;
import com.bungggo.mc.hud.LocationIndicatorRenderer;
import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.util.Util;

/**
 * マルチプレイヤー位置管理クライアントクラス
 * <p>
 * ・位置の保存<br>
 * ・HUD 表示（現在位置、ピン留めエントリ、保存メッセージ）<br>
 * ・プレイヤーの動きとの比較により、ピン留めエントリの各軸の色を変化させる  
 *   （前回との差分が縮まれば青、広がれば灰色、変化なければ前回の色を維持）
 * </p>
 */
@Environment(EnvType.CLIENT)
public class McLocationClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LocationSaveKeyBinding.register();
        LocationListBinding.register();

        Notification.register();
        LocationRenderer.register();
        LocationIndicatorRenderer.register();
        PlayerIndicatorRenderer.register();

        ShareLocationClientHandler.register();
        PlayerLocationHandler.register();

        // ログイン時：必要に応じて個別のストレージ設定があれば実施
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LocationDataManager.clear();
            LocationDataManager.load(Util.createWorldIdentifier(client));
            LocationConfig.reset();
            LocationConfig.load(Util.createWorldIdentifier(client));
        });

        // ログアウト時：ストレージにデータを保存
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LocationDataManager.save();
            LocationConfig.save();
        });

        // クライアント終了時にデータ保存
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LocationDataManager.save();
            LocationConfig.save();
        });    
    }
} 