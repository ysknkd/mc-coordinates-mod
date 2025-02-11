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
 *   <li>登録済みエントリの取得（全件／ピン留めのみ）</li>
 *   <li>ファイルへの JSON 形式での保存・ファイルからの読み込み</li>
 *   <li>リスナーへの通知</li>
 * </ul>
 * このクラスはインスタンス化できないユーティリティクラスです。
 */
public final class LocationDataManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDataManager.class);

    // 内部の位置情報リスト（メモリ上での保持）
    private static final List<LocationEntry> entries = new ArrayList<>();

    // 保存先ファイルのパス（例：config フォルダ直下の JSON ファイル）
    private static final Path DATA_FILE = Paths.get("config", "mc_location_data.json");

    private static final Gson gson = new Gson();

    // リスナーの保持リスト
    private static final List<LocationDataListener> listeners = new ArrayList<>();

    // インスタンス化防止
    private LocationDataManager() {}

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
     * 指定されたワールドにピン留めされたエントリが存在するかどうかを返します。
     *
     * @param world 対象のワールド名（例: "overworld", "the_nether"）
     * @return 対象ワールドにピン留めエントリが存在すれば {@code true}、存在しなければ {@code false}
     */
    public static boolean hasPinnedEntriesByWorld(String world) {
        return entries.stream()
                      .anyMatch(entry -> entry.isPinned() && entry.world != null && entry.world.equals(world));
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
     * @param world 対象のワールド名（例: "overworld", "the_nether"）
     * @return 指定ワールドのピン留めされたエントリの不変リスト
     */
    public static List<LocationEntry> getPinnedEntriesByWorld(String world) {
        return Collections.unmodifiableList(
            entries.stream()
                   .filter(LocationEntry::isPinned)
                   .filter(entry -> entry.world != null && entry.world.equals(world))
                   .collect(Collectors.toList())
        );
    }

    /**
     * 永続化されたファイルから位置情報のデータを読み込み、内部リストに反映します。<br>
     * ファイルが存在しない場合は何もしません。
     */
    public static void load() {
        if (!Files.exists(DATA_FILE)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(DATA_FILE, StandardCharsets.UTF_8)) {
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
        try {
            Files.createDirectories(DATA_FILE.getParent());
            try (Writer writer = Files.newBufferedWriter(DATA_FILE, StandardCharsets.UTF_8)) {
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
}