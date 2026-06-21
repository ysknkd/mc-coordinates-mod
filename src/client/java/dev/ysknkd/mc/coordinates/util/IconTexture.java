package dev.ysknkd.mc.coordinates.util;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerSkin;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

/**
 * エントリで指定された icon 名に対応するテクスチャ識別子を返すクラスです。
 */
public class IconTexture {
    private static final Map<String, Identifier> ICON_MAP = new HashMap<>();
    private static final Map<UUID, Identifier> FACE_CACHE = new HashMap<>();

    // 各テクスチャの Identifier を定義
    public static final Identifier DEFAULT_TEXTURE = Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "textures/indicator/pin.png");
    public static final Identifier DESERT_TEXTURE  = Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "textures/indicator/desert.png");
    public static final Identifier ICE_TEXTURE     = Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "textures/indicator/ice.png");
    public static final Identifier FOREST_TEXTURE  = Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "textures/indicator/forest.png");
    public static final Identifier MOUNTAIN_TEXTURE = Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "textures/indicator/mountain.png");
    public static final Identifier PALE_TEXTURE    = Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "textures/indicator/pale.png");
    public static final Identifier PLAINS_TEXTURE  = Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "textures/indicator/plains.png");
    public static final Identifier RIVER_TEXTURE = Identifier.fromNamespaceAndPath(CoordinatesApp.MOD_ID, "textures/indicator/river.png");

    static {
        ICON_MAP.put("default", DEFAULT_TEXTURE);
        ICON_MAP.put("desert", DESERT_TEXTURE);
        ICON_MAP.put("ice", ICE_TEXTURE);
        ICON_MAP.put("forest", FOREST_TEXTURE);
        ICON_MAP.put("mountain", MOUNTAIN_TEXTURE);
        ICON_MAP.put("pale", PALE_TEXTURE);
        ICON_MAP.put("plains", PLAINS_TEXTURE);
        ICON_MAP.put("river", RIVER_TEXTURE);
    }

    /**
     * 指定された位置のバイオームに応じたアイコン識別子を返します。<br>
     * 例: "minecraft:desert" を含む場合は "desert"、含まれなければ "default" を返します。
     *
     * @param client Minecraft インスタンス
     * @return アイコン識別子
     */
    public static String getIconName(Minecraft client) {
        String biome = Util.getBiome(client);

        if (biome.contains("desert")) {
            return "desert";
        } else if (biome.contains("snowy") || biome.contains("frozen") || biome.contains("ice")) {
            return "ice";
        } else if (biome.contains("pale")) {
            return "pale";
        } else if (biome.contains("forest")) {
            return "forest";
        } else if (biome.contains("plains")) {
            return "plains";
        } else if (biome.contains("mountain") || biome.contains("badlands")) {
            return "mountain";
        } else if (biome.contains("river")) {
            return "river";
        }
        return "default";
    }

    /**
     * 指定された icon 名に対応するテクスチャ識別子を返します。<br>
     * マッピングが存在しない場合は、デフォルトのテクスチャを返します。
     *
     * @param icon エントリで指定された icon 名
     * @return 対応するテクスチャ識別子
     */
    public static Identifier getIcon(String icon) {
        return ICON_MAP.getOrDefault(icon, DEFAULT_TEXTURE);
    }

    /**
     * Returns the face portion texture Identifier for the given player UUID.
     * If the texture is not generated yet, it extracts the face area from the player's skin
     * and registers it as a dynamic texture.
     * In case of a failure, it returns the default "steve" texture.
     *
     * @param playerId The UUID of the player.
     * @param playerName The name of the player.
     * @return The Identifier for the face texture or the default "steve" texture if retrieval fails.
     */
    public static Identifier getPlayerIcon(UUID playerId, String playerName) {
        if (playerId == null) {
            return DefaultPlayerSkin.getDefaultTexture();
        }
        // 既にキャッシュにある場合はそれを返す
        if (FACE_CACHE.containsKey(playerId)) {
            return FACE_CACHE.get(playerId);
        }

        Minecraft client = Minecraft.getInstance();
        GameProfile profile = new GameProfile(playerId, playerName);
        PlayerSkin skin = client.getSkinManager().createLookup(profile, false).get();
        Identifier texture = skin.body().texturePath();
        FACE_CACHE.put(playerId, texture);
        return texture;
    }
}
