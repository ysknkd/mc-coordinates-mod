package com.bungggo.mc.store;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 位置情報のデータ管理と永続化を行うユーティリティクラスです。<br>
 * 以下の責務を持ちます。
 * <ul>
 *   <li>位置情報エントリの追加、削除</li>
 *   <li>登録済みエントリの取得（全件／ピン留めのみ／ワールド毎）</li>
 *   <li>ファイルへの JSON 形式での保存・ファイルからの読み込み</li>
 *   <li>リスナーへの通知</li>
 * </ul>
 * このクラスはインスタンス化できないユーティリティクラスです。
 */
public final class LocationDataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDataManager.class);

    // 内部の位置情報リスト（メモリ上での保持）
    private static final List<LocationEntry> entries = new ArrayList<>();

    // gson インスタンス
    private static final Gson gson = new Gson();

    // リスナーの保持リスト
    private static final List<LocationDataListener> listeners = new ArrayList<>();

    // 現在の worldId を保持するためのフィールド（load 時に設定）
    private static String currentWorldId = "unknown";

    // インスタンス化防止
    private LocationDataManager() {}

    /**
     * worldId に応じたファイルパスを返します。<br>
     * 例: "config/mc-location/{worldId}/data.json"
     *
     * @param worldId 対象のワールドID
     * @return データファイルのパス
     */
    private static Path getDataFilePath(String worldId) {
        return Paths.get("config", "mc-location", worldId, "data.json");
    }

    /**
     * 新しい位置情報エントリを追加します。<br>
     * 追加後、直ちにストレージへ保存し、リスナーに通知します。
     *
     * @param entry 追加する位置情報エントリ
     */
    public static void addEntry(LocationEntry entry) {
        entries.add(entry);
        save();
        notifyEntryAdded(entry);
    }

    /**
     * 指定された位置情報エントリを削除します。<br>
     * 削除後、直ちにストレージへ保存します。
     *
     * @param entry 削除する位置情報エントリ
     */
    public static void removeEntry(LocationEntry entry) {
        entries.remove(entry);
        save();
    }

    /**
     * 登録されている全位置情報エントリの不変リストを返します。
     *
     * @return 全エントリの不変リスト
     */
    public static List<LocationEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * ピン留めされているエントリが存在するかどうかを返します。
     *
     * @return ピン留めエントリが存在すれば {@code true}、なければ {@code false}
     */
    public static boolean hasPinnedEntries() {
        return entries.stream().anyMatch(LocationEntry::isPinned);
    }

    /**
     * ピン留めされたエントリのみを抽出し、不変リストとして返します。
     *
     * @return ピン留めエントリの不変リスト
     */
    public static List<LocationEntry> getPinnedEntries() {
        return Collections.unmodifiableList(
                entries.stream()
                        .filter(LocationEntry::isPinned)
                        .collect(Collectors.toList())
        );
    }

    /**
     * 指定されたワールドのピン留めされたエントリのみを抽出し、不変リストとして返します。
     *
     * @param world 対象のワールド名（例: "overworld"）
     * @return 指定ワールドのピン留めされたエントリの不変リスト
     */
    public static List<LocationEntry> getPinnedEntriesByWorld(String world) {
        return Collections.unmodifiableList(
            entries.stream()
                   .filter(LocationEntry::isPinned)
                   .filter(entry -> entry.world.equals(world))
                   .collect(Collectors.toList())
        );
    }

    /**
     * 指定されたワールドにピン留めされたエントリが存在するかどうかを返します。
     *
     * @param world 対象のワールド名（例: "overworld"）
     * @return 対象ワールドにピン留めエントリが存在すれば {@code true}、存在しなければ {@code false}
     */
    public static boolean hasPinnedEntriesByWorld(String world) {
        return entries.stream()
                      .anyMatch(entry -> entry.isPinned() && entry.world.equals(world));
    }

    /**
     * 永続化されたファイルから位置情報のデータを読み込み、内部リストに反映します。<br>
     * ファイルが存在しない場合は何もしません。
     *
     * @param worldId 読み込み対象のワールドID
     */
    public static void load(String worldId) {
        // ワールドID を設定
        currentWorldId = worldId;
        Path dataFile = getDataFilePath(worldId);
        if (!Files.exists(dataFile)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<LocationEntry>>() {}.getType();
            List<LocationEntry> loadedEntries = gson.fromJson(reader, listType);
            if (loadedEntries != null) {
                entries.clear();
                entries.addAll(loadedEntries);
            }
        } catch (IOException e) {
            LOGGER.error("LocationDataManager#load エラー", e);
        }
    }

    /**
     * 内部リストの内容をディスクに保存します。<br>
     * 保存先ディレクトリが存在しない場合、自動的に作成します。
     */
    public static void save() {
        // currentWorldId に基づいたファイルパスを使用
        Path dataFile = getDataFilePath(currentWorldId);
        try {
            Files.createDirectories(dataFile.getParent());
            try (Writer writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
                gson.toJson(entries, writer);
            }
        } catch (IOException e) {
            LOGGER.error("LocationDataManager#save エラー", e);
        }
    }

    /**
     * リスナーを登録します。
     *
     * @param listener 登録するリスナー
     */
    public static void registerListener(LocationDataListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * リスナーの登録を解除します。
     *
     * @param listener 登録解除するリスナー
     */
    public static void unregisterListener(LocationDataListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    /**
     * エントリが追加された際に、全ての登録済みリスナーへ通知します。
     *
     * @param entry 追加された位置情報エントリ
     */
    private static void notifyEntryAdded(LocationEntry entry) {
        synchronized (listeners) {
            for (LocationDataListener listener : listeners) {
                try {
                    listener.onEntryAdded(entry);
                } catch (Exception e) {
                    LOGGER.error("LocationDataManager#notifyEntryAdded エラー", e);
                }
            }
        }
    }

    /**
     * メモリ上の位置情報をクリアします。
     */
    public static void clear() {
        // メモリ上のデータをクリア
        entries.clear();
    }
}