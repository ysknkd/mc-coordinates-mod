package com.bungggo.mc;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bungggo.mc.event.LocationListCommand;
import com.bungggo.mc.event.LocationSaveKeyBinding;
import com.bungggo.mc.hud.LocationRenderer;
import com.bungggo.mc.hud.LocationIndicatorRenderer;
import com.bungggo.mc.network.LocationPayload;
import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;

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

    private static final Logger LOGGER = LoggerFactory.getLogger("mc-location");

    @Override
    public void onInitializeClient() {
        LocationSaveKeyBinding.register();
        LocationListCommand.register();

        LocationRenderer.register();
        LocationIndicatorRenderer.register();

        // クライアント側の CLIENTBOUND 用ペイロード型を登録
        PayloadTypeRegistry.playC2S().register(LocationPayload.ID, LocationPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(LocationPayload.ID, LocationPayload.CODEC);

        // ログイン時：必要に応じて個別のストレージ設定があれば実施
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            LOGGER.info("ログインしたのでデータを読み込みます。");
            LocationDataManager.load();
        });

        // ログアウト時：ストレージにデータを保存
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            LOGGER.info("ワールドからログアウトしたのでデータを保存します。");
            LocationDataManager.save();
        });

        // クライアント終了時にデータ保存
        net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            LOGGER.info("クライアント終了時にデータを保存します。");
            LocationDataManager.save();
        });
        
        // グローバルレシーバーを登録してサーバーからブロードキャストされた LocationPayload を受信
        ClientPlayNetworking.registerGlobalReceiver(LocationPayload.ID, (payload, context) -> {
            // ネットワークスレッドからクライアントメインスレッドに切り替え
            context.client().execute(() -> {
                try {
                    // 送られてきたバッファを LocationPayload の CODEC を使ってデコード
                    LOGGER.info("ブロードキャストされた位置情報を受信: {} {} {} {}",
                            payload.sender(), payload.x(), payload.y(), payload.z());
                    
                    // クライアント側で位置情報として取り込む処理（例：LocationDataManagerに追加）
                    LocationDataManager.addEntry(new LocationEntry(payload.x(), payload.y(), payload.z(), "", payload.world()));
                } catch (Exception e) {
                    LOGGER.error("LocationPayload の受信・デコードに失敗しました", e);
                }
            });
        });
    }
} 