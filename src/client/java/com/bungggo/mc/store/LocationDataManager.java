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
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 位置情報のデータ管理と永続化を行うユーティリティクラスです。<br>
 * 以下の責務を持ちます。
 * <ul>
 *   <li>位置情報エントリの追加、削除・更新</li>
 *   <li>登録済みエントリの取得（全件／ピン留めのみ／ワールド毎）</li>
 *   <li>ファイルへの JSON 形式での保存・ファイルからの読み込み</li>
 *   <li>エントリ追加時のリスナー通知</li>
 * </ul>
 * このクラスはインスタンス化できないユーティリティクラスです。
 */
public final class LocationDataManager {

    // ----------------------------------------------------------------
    // フィールド
    // ----------------------------------------------------------------
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDataManager.class);
    private static final Gson gson = new Gson();
    private static final Map<UUID, LocationEntry> entries = new HashMap<>();
    private static final List<LocationDataListener> listeners = new ArrayList<>();

    // 現在の worldId（load 時に更新される）
    private static String currentWorldId = "unknown";

    // ----------------------------------------------------------------
    // コンストラクタ（インスタンス化防止）
    // ----------------------------------------------------------------
    private LocationDataManager() {}

    // ----------------------------------------------------------------
    // ファイル入出力関連
    // ----------------------------------------------------------------

    /**
     * 指定された worldId に基づいたデータファイルのパスを取得します。<br>
     * 例: "config/mc-location/{worldId}/data.json"
     *
     * @param worldId 対象のワールドID
     * @return データファイルのパス
     */
    private static Path getDataFilePath(String worldId) {
        return Paths.get("config", "mc-location", worldId, "data.json");
    }

    /**
     * 永続化されたファイルから位置情報データを読み込み、内部マップに反映します。<br>
     * ファイルが存在しない場合は何も行いません。
     *
     * @param worldId 読み込み対象のワールドID
     */
    public static void load(String worldId) {
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
                entries.putAll(
                    loadedEntries.stream()
                    .collect(Collectors.toMap(entry -> entry.uuid, Function.identity()))
                );
            }
        } catch (IOException e) {
            LOGGER.error("LocationDataManager#load エラー", e);
        }
    }

    /**
     * 内部マップの内容をJSON形式でディスクに保存します。<br>
     * 保存先ディレクトリが存在しない場合は自動で作成します。
     */
    public static void save() {
        Path dataFile = getDataFilePath(currentWorldId);
        try {
            Files.createDirectories(dataFile.getParent());
            try (Writer writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
                gson.toJson(entries.values(), writer);
            }
        } catch (IOException e) {
            LOGGER.error("LocationDataManager#save エラー", e);
        }
    }

    // ----------------------------------------------------------------
    // エントリ管理（追加・更新・削除・取得）
    // ----------------------------------------------------------------

    /**
     * 新規追加または、既存のエントリ（同一 UUID）の状態を更新します。<br>
     * 同じ UUID のエントリが存在する場合はそのフィールドを上書き更新し、存在しなければ新規登録します。
     *
     * @param newEntry 追加または更新対象の LocationEntry
     */
    public static void addOrUpdateEntry(LocationEntry newEntry) {
        entries.compute(newEntry.uuid, (key, existing) -> {
            if (existing != null) {
                existing.x = newEntry.x;
                existing.y = newEntry.y;
                existing.z = newEntry.z;
                existing.description = newEntry.description;
                existing.world = newEntry.world;
                existing.pinned = newEntry.pinned;
                existing.icon = newEntry.icon;
                return existing;
            }
            notifyEntryAdded(newEntry);
            return newEntry;
        });
    }

    /**
     * 指定された UUID に一致するエントリを取得します。
     *
     * @param uuid 対象エントリの UUID
     * @return 該当するエントリがあれば返します。なければ null
     */
    public static LocationEntry getEntry(UUID uuid) {
        return entries.get(uuid);
    }

    /**
     * 現在管理されている全エントリを返します。
     *
     * @return 管理中のエントリコレクション
     */
    public static Collection<LocationEntry> getEntries() {
        return entries.values();
    }

    /**
     * 与えられたエントリの UUID をキーとしてエントリを削除します。
     *
     * @param entry 削除対象の LocationEntry
     */
    public static void removeEntry(LocationEntry entry) {
        entries.remove(entry.uuid);
    }

    /**
     * メモリ上の位置情報エントリを全てクリアします。
     */
    public static void clear() {
        entries.clear();
    }

    // ピン留めエントリに関する処理

    /**
     * ピン留めされたエントリが存在するかどうかを返します。
     *
     * @return ピン留めエントリが存在すれば true、なければ false
     */
    public static boolean hasPinnedEntries() {
        return entries.values().stream().anyMatch(LocationEntry::isPinned);
    }

    /**
     * ピン留めされたエントリのみを抽出し、不変リストとして返します。
     *
     * @return ピン留めエントリの不変リスト
     */
    public static List<LocationEntry> getPinnedEntries() {
        return Collections.unmodifiableList(
                entries.values().stream()
                        .filter(LocationEntry::isPinned)
                        .collect(Collectors.toList())
        );
    }

    /**
     * 指定されたワールドでピン留めされたエントリが存在するかどうかを返します。
     *
     * @param world 対象のワールド名
     * @return ピン留めエントリがあれば true、なければ false
     */
    public static boolean hasPinnedEntriesByWorld(String world) {
        return entries.values().stream()
                .anyMatch(entry -> entry.isPinned() && entry.world.equals(world));
    }

    /**
     * 指定されたワールドのピン留めされたエントリのみを抽出し、不変リストとして返します。
     *
     * @param world 対象のワールド名（例: "overworld"）
     * @return 指定ワールドのピン留めエントリの不変リスト
     */
    public static List<LocationEntry> getPinnedEntriesByWorld(String world) {
        return Collections.unmodifiableList(
            entries.values().stream()
                   .filter(LocationEntry::isPinned)
                   .filter(entry -> entry.world.equals(world))
                   .collect(Collectors.toList())
        );
    }

    // ----------------------------------------------------------------
    // リスナー管理
    // ----------------------------------------------------------------

    /**
     * エントリが追加された際に、全ての登録済みリスナーへ通知します。
     *
     * @param entry 追加されたエントリ
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
     * リスナーを登録します。
     *
     * @param listener 登録対象の LocationDataListener
     */
    public static void registerListener(LocationDataListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * 登録済みのリスナーを解除します。
     *
     * @param listener 解除対象の LocationDataListener
     */
    public static void unregisterListener(LocationDataListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
}