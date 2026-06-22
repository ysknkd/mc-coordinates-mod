package dev.ysknkd.mc.coordinates.util;

import dev.ysknkd.mc.coordinates.CoordinatesApp;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

public class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoordinatesApp.MOD_ID);

    /**
     * 指定された Minecraft から現在のワールド名を取得し、"minecraft:" の接頭辞を取り除いて返します。
     *
     * @param client Minecraft のインスタンス
     * @return 現在のワールド名、もしくはワールド情報が取得できない場合は "unknown"
     */
    public static String getCurrentWorldName(Minecraft client) {
        if (client == null || client.level == null) {
            return "unknown";
        }
        return client.level.dimension().identifier().toString();
    }

   /**
     * 指定された Minecraft からバイオーム名を返します。<br>
     *
     * @param client Minecraft のインスタンス
     * @return バイオーム名（例: "plains"）
     */
    public static String getBiome(Minecraft client) {
        if (client == null || client.player == null || client.level == null) {
            return "unknown";
        }
        BlockPos pos = client.player.blockPosition();
        Holder<Biome> biome = client.level.getBiome(pos);
        return biome.getRegisteredName().replace("minecraft:", "");
    }

    public static GameProfile getGameProfileByUuid(Minecraft client, UUID uuid) {
        if (client == null || client.player == null || client.level == null) {
            return null;
        }
        return client.level.players().stream()
            .filter(player -> player.getUUID().equals(uuid))
            .findFirst()
            .map(player -> player.getGameProfile())
            .orElse(null);
    }

    // ワールド内に保存するファイル名
    private static final String UNIQUE_ID_FILE_NAME = CoordinatesApp.MOD_ID + "/unique_id.dat";

    /**
     * 統合サーバー(シングルプレイ)から、ワールド固有のUUIDを取得し、
     * なければ新規生成して保存する。
     */
    public static UUID getOrCreateWorldUniqueId(IntegratedServer server) {
        Path worldFolder = server.getWorldPath(LevelResource.ROOT);
        LOGGER.info(worldFolder.toString());
        // => シングルプレイのワールドフォルダ
        Path modIdFile = worldFolder.resolve(UNIQUE_ID_FILE_NAME);

        // ディレクトリがなければ作る
        try {
            Files.createDirectories(modIdFile.getParent());
        } catch (IOException e) {
            e.printStackTrace();
            // 必要に応じて例外処理
        }

        // ファイルがあるなら読み込む
        if (Files.exists(modIdFile)) {
            try (DataInputStream in = new DataInputStream(Files.newInputStream(modIdFile))) {
                long mostSig = in.readLong();
                long leastSig = in.readLong();
                return new UUID(mostSig, leastSig);
            } catch (IOException e) {
                e.printStackTrace();
                // 例外発生時は再生成
            }
        }

        // ここに来たらファイルが存在しない or 読み込み失敗 → 新規UUID生成
        UUID newId = UUID.randomUUID();
        // 書き込み
        try (DataOutputStream out = new DataOutputStream(Files.newOutputStream(modIdFile))) {
            out.writeLong(newId.getMostSignificantBits());
            out.writeLong(newId.getLeastSignificantBits());
        } catch (IOException e) {
            e.printStackTrace();
            // 書き込み失敗したらどうする？ → fallbackなど
        }

        return newId;
    }

    /**
     * 現在のクライアントが存在するワールドに基づき、
     * 「ワールド＋ディメンション」を一意に区別できるID文字列を生成して返す。
     * @param client    Minecraft
     * @return 生成されたワールド識別子（例：MD5ハッシュ文字列など）
     */
    public static String createWorldIdentifier(Minecraft client) {
        if (client == null) {
            return "unknown";
        }

        String baseName;

        // シングルプレイかどうかで分岐
        if (client.hasSingleplayerServer()) {
            // シングル -> 統合サーバー(IntegratedServer)からレベル名を取得
            IntegratedServer integratedServer = client.getSingleplayerServer();
            if (integratedServer != null) {
                baseName = Util.getOrCreateWorldUniqueId(integratedServer).toString();
            } else {
                baseName = "SinglePlayerUnknown";
            }
        } else {
            // マルチ -> サーバーリストの情報からアドレスを取得
            ServerData serverInfo = client.getCurrentServer();
            if (serverInfo != null) {
                baseName = serverInfo.ip; // 例: "example.com:25565"
            } else {
                // サーバーリストにない場合など
                baseName = "MultiPlayerUnknown";
            }
        }

        // baseName をハッシュ or sanitize して返す（ここではMD5に例示）
        return md5Hash(baseName);
    }

    /**
     * 文字列のMD5ハッシュ(16進数)を返すサンプルメソッド。
     */
    private static String md5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            // MD5結果を16進文字列に変換
            BigInteger number = new BigInteger(1, digest);
            return number.toString(16);
        } catch (NoSuchAlgorithmException e) {
            // 万が一MD5が使えない環境なら適当にフォールバック
            return Integer.toHexString(input.hashCode());
        }
    }
}
