package com.bungggo.mc.event;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import com.bungggo.mc.store.LocationDataManager;
import com.bungggo.mc.store.LocationEntry;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

public class LocationSaveKeyBinding implements Consumer<MinecraftClient> {
    
    // 保存キー（G キー）
    private static final KeyBinding SAVE_LOCATION_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mc-location.save_location",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.mc-location"
    ));

    
    public static void register() {
        // 位置保存キーおよびその他の Tick 処理
        KeyBindingEventHandler.registerListener(SAVE_LOCATION_KEY, new LocationSaveKeyBinding());
    }

    @Override
    /**
     * 保存キー (G キー) が押された場合、現在位置を保存する。
     */
    public void accept(MinecraftClient client) {
        if (client.player == null) {
            return;
        }
        double x = client.player.getX();
        double y = client.player.getY();
        double z = client.player.getZ();

        // LocationDataManager にエントリ追加（内部で保存処理が実施される）
        LocationDataManager.addEntry(new LocationEntry(x, y, z));
    }
}
