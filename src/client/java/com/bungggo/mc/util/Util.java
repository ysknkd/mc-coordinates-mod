package com.bungggo.mc.util;

import net.minecraft.client.MinecraftClient;

public class Util {

    /**
     * 指定された MinecraftClient から現在のワールド名を取得し、"minecraft:" の接頭辞を取り除いて返します。
     *
     * @param client MinecraftClient のインスタンス
     * @return 現在のワールド名、もしくはワールド情報が取得できない場合は "unknown"
     */
    public static String getCurrentWorldName(MinecraftClient client) {
        if (client == null || client.world == null) {
            return "unknown";
        }
        return client.world.getRegistryKey().getValue().toString();
    }
}
