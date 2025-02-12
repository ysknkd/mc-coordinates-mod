package com.bungggo.mc.event;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.bungggo.mc.config.LocationConfig;
import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;
import com.bungggo.mc.util.Util;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * 位置情報保存用のキー入力処理（G キー）です。<br>
 * 保存時に LocationConfig.defaultPinState の値を用いて、エントリの初期ピン状態を設定します。
 */
public class LocationSaveKeyBinding implements Consumer<MinecraftClient> {
    
    // 保存キー（G キー）
    private static final KeyBinding SAVE_LOCATION_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mc-location.save_location",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.mc-location"
    ));
    
    public static void register() {
        // 位置保存キーおよびその他の Tick 処理の登録
        KeyBindingEventHandler.registerListener(SAVE_LOCATION_KEY, new LocationSaveKeyBinding());
    }

    /**
     * 保存キー (G キー) が押された場合、現在位置とワールド情報、設定のピン状態で位置情報エントリを保存します。
     */
    @Override
    public void accept(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();
        String description = Util.getDefaultDescription(client);
        String worldName = Util.getCurrentWorldName(client);
        // エントリ生成（保存時のピン状態は設定値 LocationConfig.defaultPinState を利用）
        LocationEntry entry = new LocationEntry(x, y, z, description, worldName, LocationConfig.getDefaultPinState());
        // LocationDataManager にエントリを追加（内部で保存処理を実施）
        LocationDataManager.addEntry(entry);
    }
}
