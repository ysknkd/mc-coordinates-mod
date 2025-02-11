package com.bungggo.mc.store;

import com.bungggo.mc.model.LocationEntry;
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

    // インスタンス化防止
    private LocationDataManager() {}

    /**
     * 新しい位置情報エントリを追加します。<br>
     * 追加後、直ちにストレージへ保存します。
     *
     * @param entry 追加する位置情報エントリ
     */
    public static void addEntry(LocationEntry entry) {
        entries.add(entry);
        save();
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
}