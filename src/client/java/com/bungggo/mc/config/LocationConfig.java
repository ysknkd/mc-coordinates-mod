package com.bungggo.mc.config;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 位置情報保存に関する各種設定を保持するクラスです。<br>
 * このクラスは設定の JSON 保存とロードをサポートしています。<br>
 * 保存先: config/mc-location/{worldId}/config.json
 */
public class LocationConfig {
    // 保存時にピン状態を初期状態として有効にするか（デフォルトは無効）
    private static final boolean DEFAULT_PIN_STATE = false;
    private static boolean defaultPinState = false;
    // 現在の worldId を保持するフィールド（初期値 "unknown"）
    private static String currentWorldId = "unknown";

    // 内部用構造体：設定データを JSON として保存する際の構造体
    private static class ConfigData {
        boolean defaultPinState;
    }
    
    /**
     * worldId に応じた設定ファイルのパスを返します。<br>
     * 例: config/mc-location/{worldId}/config.json
     *
     * @param worldId 対象のワールドID
     * @return 設定ファイルのパス
     */
    private static Path getConfigFilePath(String worldId) {
        return Paths.get("config", "mc-location", worldId, "config.json");
    }
    
    /**
     * 指定されたワールドIDの設定ファイルから設定を読み込みます。<br>
     * ファイルが存在しない場合は既定値（defaultPinState=false）を維持します。<br>
     * また、内部で現在の worldId を保持します。
     *
     * @param worldId 読み込み対象のワールドID
     */
    public static void load(String worldId) {
        currentWorldId = worldId; // worldId を内部に保持
        Path configFile = getConfigFilePath(worldId);
        if (!Files.exists(configFile)) {
            return;
        }
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            ConfigData data = gson.fromJson(reader, ConfigData.class);
            if (data != null) {
                defaultPinState = data.defaultPinState;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 現在の設定を、指定されたワールドIDの設定ファイルへ保存します。<br>
     * 保存先ディレクトリが存在しない場合は自動的に作成されます。<br>
     * また、引数で渡された worldId を内部に保持します。
     *
     * @param worldId 保存対象のワールドID
     */
    public static void save() {
        Path configFile = getConfigFilePath(LocationConfig.currentWorldId);
        try {
            Files.createDirectories(configFile.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        ConfigData data = new ConfigData();
        data.defaultPinState = defaultPinState;
        try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * ピン状態の初期設定を変更します。
     *
     * @param state true なら有効、false なら無効
     */
    public static void setDefaultPinState(boolean state) {
        defaultPinState = state;
    }
    
    /**
     * 現在のピン状態設定を返します。
     *
     * @return trueなら有効、falseなら無効
     */
    public static boolean getDefaultPinState() {
        return defaultPinState;
    }
    
    /**
     * ピン状態を反転させます（有効なら無効、無効なら有効に）。
     */
    public static void toggleDefaultPinState() {
        defaultPinState = !defaultPinState;
    }

    public static void reset() {
        defaultPinState = DEFAULT_PIN_STATE;
    }
} 