package dev.ysknkd.mc.coordinates.event;

import java.util.function.Consumer;

import org.lwjgl.glfw.GLFW;

import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;

import dev.ysknkd.mc.coordinates.config.Config;
import dev.ysknkd.mc.coordinates.store.CoordinatesDataManager;
import dev.ysknkd.mc.coordinates.store.Coordinates;
import dev.ysknkd.mc.coordinates.util.IconTexture;
import dev.ysknkd.mc.coordinates.util.Util;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

/**
 * 位置情報保存用のキー入力処理（G キー）です。<br>
 */
public class CoordinatesSaveKeyBinding implements Consumer<MinecraftClient> {
    
    // 保存キー（G キー）
    private static final KeyBinding SAVE_COORDINATES_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.mc-coordinates.save_coordinates",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.mc-coordinates"
    ));
    
    public static void register() {
        // 位置保存キーおよびその他の Tick 処理の登録
        KeyBindingEventHandler.registerListener(SAVE_COORDINATES_KEY, new CoordinatesSaveKeyBinding());
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
        String icon = IconTexture.getIconName(client);

        // 位置情報エントリ生成。保存時のピン状態は設定値を利用
        Coordinates entry = new Coordinates(x, y, z, description, worldName, Config.getDefaultPinState(), icon);
        CoordinatesDataManager.addOrUpdateEntry(entry);
    }
}
