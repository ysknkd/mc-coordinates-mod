package com.bungggo.mc.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;

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

   /**
     * 指定された MinecraftClient からデフォルトの説明として使う文字列（構造物名またはバイオーム名）を返します。<br>
     * 現在はバイオーム情報を取得して利用しています。
     *
     * @param client MinecraftClient のインスタンス
     * @return デフォルトの説明文字列（例: "plains"）
     */
    public static String getDefaultDescription(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return "unknown";
        }
        BlockPos pos = client.player.getBlockPos();
        RegistryEntry<Biome> biome = client.world.getBiome(pos);
        return biome.getIdAsString().replace("minecraft:", "");
    }
}
