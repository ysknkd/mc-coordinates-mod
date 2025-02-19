package dev.ysknkd.mc.coordinates.util;

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
public class IconTexture {
    private static final Map<String, Identifier> ICON_MAP = new HashMap<>();
    private static final Map<UUID, Identifier> FACE_CACHE = new HashMap<>();

    // 各テクスチャの Identifier を定義
    public static final Identifier DEFAULT_TEXTURE = Identifier.of("mc-coordinates", "textures/indicator/pin.png");
    public static final Identifier DESERT_TEXTURE  = Identifier.of("mc-coordinates", "textures/indicator/desert.png");
    public static final Identifier ICE_TEXTURE     = Identifier.of("mc-coordinates", "textures/indicator/ice.png");
    public static final Identifier FOREST_TEXTURE  = Identifier.of("mc-coordinates", "textures/indicator/forest.png");
    public static final Identifier MOUNTAIN_TEXTURE = Identifier.of("mc-coordinates", "textures/indicator/mountain.png");
    public static final Identifier PALE_TEXTURE    = Identifier.of("mc-coordinates", "textures/indicator/pale.png");
    public static final Identifier PLAINS_TEXTURE  = Identifier.of("mc-coordinates", "textures/indicator/plains.png");
    public static final Identifier RIVER_TEXTURE = Identifier.of("mc-coordinates", "textures/indicator/river.png");

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
     * 例: "minecraft:desert" を含む場合は "desert_icon"、含まれなければ "default_icon" を返します。
     *
     * @param client MinecraftClient インスタンス
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
            return Identifier.of("minecraft", "textures/entity/steve.png");
        }
        // 既にキャッシュにある場合はそれを返す
        if (FACE_CACHE.containsKey(playerId)) {
            return FACE_CACHE.get(playerId);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerSkinProvider skinProvider = client.getSkinProvider();

        // UUID からダミーの GameProfile を作成してスキンを取得
        GameProfile profile = new GameProfile(playerId, playerName);
        SkinTextures skin = skinProvider.getSkinTextures(profile);
        Identifier texture = skin.texture();
        if (texture == null) {
            return Identifier.of("minecraft", "textures/entity/steve.png");
        }

        // TextureManager から取得済みの場合、NativeImage を利用
        NativeImage fullImage = null;
        AbstractTexture abstractTexture = client.getTextureManager().getTexture(texture);
        if (abstractTexture instanceof NativeImageBackedTexture) {
            NativeImageBackedTexture skinTexture = (NativeImageBackedTexture) abstractTexture;
            fullImage = skinTexture.getImage();
        } else {
            // フォールバック：ResourceManager から直接読み込む
            ResourceManager resourceManager = client.getResourceManager();
            try (InputStream stream = resourceManager.open(texture)) {
                fullImage = NativeImage.read(stream);
            } catch (IOException e) {
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