package com.bungggo.mc.storage;

import com.bungggo.mc.LocationEntry;
import com.bungggo.mc.LocationDataManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ユーザーごとに管理するための LocationDataStorage インスタンス。
 * インスタンス生成時にユーザー識別子を渡し、その識別子のディレクトリ内でデータを読み書きします。
 * データは .minecraft/mc-location/<userId>/locations.json に保存されます。
 */
public class LocationDataStorage {
    private static final Logger LOGGER = LoggerFactory.getLogger(LocationDataStorage.class);
    private static final String BASE_DIRECTORY = "mc-location";
    private static final String FILE_NAME = "locations.json";
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private final String userId;

    public LocationDataStorage(String userId) {
        this.userId = userId;
    }

    private File getUserDirectory() throws IOException {
        File userDir = new File(BASE_DIRECTORY + File.separator + userId);
        if (!userDir.exists() && !userDir.mkdirs()) {
            throw new IOException("ユーザー用ディレクトリの作成に失敗しました: " + userDir.getAbsolutePath());
        }
        return userDir;
    }

    public void save() {
        try {
            File userDir = getUserDirectory();
            File file = new File(userDir, FILE_NAME);
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(LocationDataManager.entries, writer);
            }
        } catch (IOException e) {
            LOGGER.error("ファイル保存に失敗しました", e);
        }
    }

    public void load() {
        try {
            File userDir = new File(BASE_DIRECTORY + File.separator + userId);
            File file = new File(userDir, FILE_NAME);
            if (!file.exists()) {
                return;
            }
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<LocationEntry>>() {}.getType();
                List<LocationEntry> loadedEntries = gson.fromJson(reader, listType);
                if (loadedEntries != null) {
                    LocationDataManager.entries.clear();
                    LocationDataManager.entries.addAll(loadedEntries);
                }
            }
        } catch (IOException e) {
            LOGGER.error("ファイル読み込みに失敗しました", e);
        }
    }
} 