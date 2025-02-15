package com.bungggo.mc.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import java.io.IOException;
import java.io.InputStream;

/**
 * エントリで指定された icon 名に対応するテクスチャ識別子を返すクラスです。
 */
public class IconTextureMap {
    private static final Map<String, Identifier> ICON_MAP = new HashMap<>();
    private static final Map<UUID, Identifier> FACE_CACHE = new HashMap<>();

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
     * 指定された位置のバイオームに応じたアイコン識別子を返します。<br>
     * 例: "minecraft:desert" を含む場合は "desert_icon"、含まれなければ "default_icon" を返します。
     *
     * @param client MinecraftClient インスタンス
     * @param pos 対象の BlockPos
     * @return アイコン識別子
     */
    public static String getIconName(MinecraftClient client) {
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
     * 指定された GameProfile に対応する顔部分のテクスチャ Identifier を返します。<br>
     * 未生成の場合は、スキンのテクスチャから顔の領域 (8,8)～(16,16) を切り出して動的テクスチャとして登録します。<br>
     * なお、テクスチャ取得には getSkinTextures を用い、取得できなかった場合はデフォルトの "steve" を返します。
     *
     * @param profile 取得対象の GameProfile
     * @return 顔部分のテクスチャ Identifier。取得できなかった場合はデフォルトの "steve" テクスチャ
     */
    public static Identifier getPlayerIcon(GameProfile profile) {
        if (profile == null) {
            return Identifier.of("minecraft", "textures/entity/steve.png");
        }
        UUID playerId = profile.getId();
        if (FACE_CACHE.containsKey(playerId)) {
            return FACE_CACHE.get(playerId);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerSkinProvider skinProvider = client.getSkinProvider();
        SkinTextures skin = skinProvider.getSkinTextures(profile);
        Identifier texture = skin.texture();
        if (texture == null) {
            return Identifier.of("minecraft", "textures/entity/steve.png");
        }

        // NativeImage の取得（TextureManager に登録済みか、フォールバックとしてリソースから読み込み）
        NativeImage fullImage = null;
        AbstractTexture abstractTexture = client.getTextureManager().getTexture(texture);
        if (abstractTexture instanceof NativeImageBackedTexture) {
            NativeImageBackedTexture skinTexture = (NativeImageBackedTexture) abstractTexture;
            fullImage = skinTexture.getImage();
        } else {
            // フォールバック処理：リソースマネージャーから直接読み込む
            ResourceManager resourceManager = client.getResourceManager();
            try (InputStream stream = resourceManager.open(texture)) {
                fullImage = NativeImage.read(stream);
            } catch (IOException e) {
                e.printStackTrace();
                return Identifier.of("minecraft", "textures/entity/steve.png");
            }
        }
        if (fullImage == null) {
            return Identifier.of("minecraft", "textures/entity/steve.png");
        }

        // 顔部分の領域 (通常は 8×8 ピクセル)：左上が (8,8)、右下が (16,16)
        final int faceX = 8;
        final int faceY = 8;
        final int faceWidth = 8;
        final int faceHeight = 8;
        
        // 新規 NativeImage を生成し、顔部分のピクセル（ARGB値）をコピー
        NativeImage faceImage = new NativeImage(faceWidth, faceHeight, false);
        for (int x = 0; x < faceWidth; x++) {
            for (int y = 0; y < faceHeight; y++) {
                int pixel = fullImage.getColorArgb(faceX + x, faceY + y);
                faceImage.setColorArgb(x, y, pixel);
            }
        }

        // 動的テクスチャとして登録
        NativeImageBackedTexture faceTexture = new NativeImageBackedTexture(faceImage);
        Identifier faceTextureId = Identifier.of("bungggo", "player_face/" + playerId);
        client.getTextureManager().registerTexture(faceTextureId, faceTexture);
        FACE_CACHE.put(playerId, faceTextureId);
        return faceTextureId;
    }
} 