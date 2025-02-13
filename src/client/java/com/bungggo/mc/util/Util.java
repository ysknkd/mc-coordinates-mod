package com.bungggo.mc.util;

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

import net.minecraft.util.WorldSavePath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.math.BlockPos;

public class Util {
    private static final Logger LOGGER = LoggerFactory.getLogger("mc-location");

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
     * 指定された MinecraftClient からバイオーム名を返します。<br>
     *
     * @param client MinecraftClient のインスタンス
     * @return バイオーム名（例: "plains"）
     */
    public static String getBiome(MinecraftClient client) {
        if (client == null || client.player == null || client.world == null) {
            return "unknown";
        }
        BlockPos pos = client.player.getBlockPos();
        RegistryEntry<Biome> biome = client.world.getBiome(pos);
        return biome.getIdAsString().replace("minecraft:", "");
    }

    // ワールド内に保存するファイル名
    private static final String UNIQUE_ID_FILE_NAME = "mc-location/unique_id.dat";

    /**
     * 統合サーバー(シングルプレイ)から、ワールド固有のUUIDを取得し、
     * なければ新規生成して保存する。
     */
    public static UUID getOrCreateWorldUniqueId(IntegratedServer server) {
        Path worldFolder = server.getSavePath(WorldSavePath.ROOT);
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
     * @param client    MinecraftClient
     * @return 生成されたワールド識別子（例：MD5ハッシュ文字列など）
     */
    public static String createWorldIdentifier(MinecraftClient client) {
        if (client == null) {
            return "unknown";
        }

        String baseName;

        // シングルプレイかどうかで分岐
        if (client.isInSingleplayer()) {
            // シングル -> 統合サーバー(IntegratedServer)からレベル名を取得
            IntegratedServer integratedServer = client.getServer();
            if (integratedServer != null) {
                baseName = Util.getOrCreateWorldUniqueId(integratedServer).toString();
            } else {
                baseName = "SinglePlayerUnknown";
            }
        } else {
            // マルチ -> サーバーリストの情報からアドレスを取得
            ServerInfo serverInfo = client.getCurrentServerEntry();
            if (serverInfo != null) {
                baseName = serverInfo.address; // 例: "example.com:25565"
            } else {
                // サーバーリストにない場合など
                baseName = "MultiPlayerUnknown";
            }
        }
        LOGGER.info("ワールド識別子: {}", baseName);

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
