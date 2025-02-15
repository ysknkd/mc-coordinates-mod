package com.bungggo.mc.event;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import com.bungggo.mc.config.LocationConfig;
import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;
import com.bungggo.mc.util.IconTextureMap;
import com.bungggo.mc.util.Util;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * 位置情報保存用のキー入力処理（G キー）です。<br>
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
     * 保存キー (G キー) が押された場合、現在位置とワールド情報、設定のピン状態で位置情報エントリを保存します。<br>
     */
    @Override
    public void accept(MinecraftClient client) {
        if (client.player == null || client.world == null) {
            return;
        }

        // レイキャストして画面中央の"+"位置を取得
        HitResult hitResult = client.player.raycast(3, 1.0F, false);
        Vec3d hitPos = hitResult.getPos();
        double x = hitPos.x;
        double y = hitPos.y;
        double z = hitPos.z;
        
        String worldName = Util.getCurrentWorldName(client);
        String description = Util.getBiome(client);
        String icon = IconTextureMap.getIconName(client);

        // 位置情報エントリ生成。保存時のピン状態は設定値を利用
        LocationEntry entry = new LocationEntry(x, y, z, description, worldName, LocationConfig.getDefaultPinState(), icon);
        LocationDataManager.addOrUpdateEntry(entry);
    }
}
