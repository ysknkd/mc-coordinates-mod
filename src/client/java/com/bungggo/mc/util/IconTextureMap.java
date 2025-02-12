package com.bungggo.mc.util;

import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;

/**
 * エントリで指定された icon 名に対応するテクスチャ識別子を返すクラスです。
 */
public class IconTextureMap {
    private static final Map<String, Identifier> ICON_MAP = new HashMap<>();

    // 各テクスチャの Identifier を定義
    public static final Identifier DEFAULT_TEXTURE = Identifier.of("mc-location", "textures/indicator/pin.png");
    public static final Identifier PALE_TEXTURE    = Identifier.of("mc-location", "textures/indicator/pale.png");
    public static final Identifier ICE_TEXTURE     = Identifier.of("mc-location", "textures/indicator/ice.png");
    public static final Identifier FOREST_TEXTURE  = Identifier.of("mc-location", "textures/indicator/forest.png");
    public static final Identifier MOUNTAIN_TEXTURE = Identifier.of("mc-location", "textures/indicator/mountain.png");
    public static final Identifier DESERT_TEXTURE  = Identifier.of("mc-location", "textures/indicator/desert.png");

    static {
        ICON_MAP.put("default", DEFAULT_TEXTURE);
        ICON_MAP.put("pale", PALE_TEXTURE);
        ICON_MAP.put("ice", ICE_TEXTURE);
        ICON_MAP.put("forest", FOREST_TEXTURE);
        ICON_MAP.put("mountain", MOUNTAIN_TEXTURE);
        ICON_MAP.put("desert", DESERT_TEXTURE);
    }

    /**
     * 指定された icon 名に対応するテクスチャ識別子を返します。<br>
     * マッピングが存在しない場合は、デフォルトのテクスチャを返します。
     *
     * @param icon エントリで指定された icon 名
     * @return 対応するテクスチャ識別子
     */
    public static Identifier getTexture(String icon) {
        return ICON_MAP.getOrDefault(icon, DEFAULT_TEXTURE);
    }
} 