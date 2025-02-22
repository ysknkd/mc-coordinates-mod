package dev.ysknkd.mc.coordinates.store;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import dev.ysknkd.mc.coordinates.CoordinatesApp;

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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.LinkedHashMap;
import java.util.Comparator;

/**
 * Utility class for managing and persisting coordinate data.
 */
public final class CoordinatesDataManager {

    // ----------------------------------------------------------------
    // Fields
    // ----------------------------------------------------------------
    private static final Logger LOGGER = LogManager.getLogger(CoordinatesApp.MOD_ID);

    // Map to manage entries keyed by UUID
    private static final Map<UUID, Coordinates> entries = new LinkedHashMap<>();

    // Gson instance
    private static final Gson gson = new Gson();
    private static final List<CoordinatesDataListener> listeners = new ArrayList<>();

    // Current worldId (updated during load)
    private static String currentWorldId = "unknown";

    // ----------------------------------------------------------------
    // Constructor (Prevent instantiation)
    // ----------------------------------------------------------------
    private CoordinatesDataManager() {}

    // ----------------------------------------------------------------
    // File I/O related
    // ----------------------------------------------------------------

    /**
     * Gets the path to the data file based on the given worldId.
     * Example: "config/mc-coordinates/{worldId}/data.json"
     *
     * @param worldId Target world ID
     * @return Path to the data file
     */
    private static Path getDataFilePath(String worldId) {
        return Paths.get("config", "mc-coordinates", worldId, "data.json");
    }

    /**
     * Loads coordinate data from a persisted file and reflects it in the internal map.
     * If the file does not exist, it does nothing.
     *
     * @param worldId Target world ID to load
     */
    public static void load(String worldId) {
        currentWorldId = worldId;
        Path dataFile = getDataFilePath(worldId);
        if (!Files.exists(dataFile)) {
            return;
        }
        try (Reader reader = Files.newBufferedReader(dataFile, StandardCharsets.UTF_8)) {
            Type listType = new TypeToken<List<Coordinates>>() {}.getType();
            List<Coordinates> loadedEntries = gson.fromJson(reader, listType);
            if (loadedEntries != null) {
                entries.clear();
                entries.putAll(
                    loadedEntries.stream()
                    .collect(Collectors.toMap(entry -> entry.uuid, Function.identity()))
                );
            }
        } catch (IOException e) {
            LOGGER.error("CoordinatesDataManager#load error", e);
        }
    }

    /**
     * Saves the content of the internal map to a JSON file on disk.
     * If the save destination directory does not exist, it creates it automatically.
     */
    public static void save() {
        Path dataFile = getDataFilePath(currentWorldId);
        try {
            Files.createDirectories(dataFile.getParent());
            try (Writer writer = Files.newBufferedWriter(dataFile, StandardCharsets.UTF_8)) {
                gson.toJson(entries.values(), writer);
            }
        } catch (IOException e) {
            LOGGER.error("CoordinatesDataManager#save error", e);
        }
    }

    // ----------------------------------------------------------------
    // Entry management (Add, Update, Remove, Get)
    // ----------------------------------------------------------------

    /**
     * Adds a new entry or updates an existing entry (same UUID).<br>
     * If an entry with the same UUID already exists, its fields are overwritten and updated,
     * or a new entry is registered if it does not exist.
     *
     * @param newEntry Entry to add or update
     */
    public static void addOrUpdateEntry(Coordinates newEntry) {
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
     * Removes an entry from memory.
     *
     * @param entry Entry to remove
     */
    public static void removeEntry(Coordinates entry) {
        entries.remove(entry.uuid);
    }

    /**
     * Returns the entry corresponding to the specified UUID.
     *
     * @param uuid UUID of the entry to search for
     * @return The corresponding entry if it exists, null if it does not
     */
    public static Coordinates getEntry(UUID uuid) {
        return entries.get(uuid);
    }

    /**
     * Returns a list of entries sorted by their saved time in descending order.
     *
     * @return List of Coordinates sorted by saved time descending
     */
    public static List<Coordinates> getEntries() {
        return entries.values().stream()
                .sorted(Comparator.comparingLong((Coordinates e) -> e.savedTime).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Clears all coordinate entries from memory.
     */
    public static void clear() {
        entries.clear();
    }

    // Pinned entry related processing

    /**
     * Returns whether there are pinned entries.
     *
     * @return true if pinned entries exist, false if they do not
     */
    public static boolean hasPinnedEntries() {
        return entries.values().stream().anyMatch(Coordinates::isPinned);
    }

    /**
     * Returns pinned entries only as an immutable list.
     *
     * @return Immutable list of pinned entries
     */
    public static List<Coordinates> getPinnedEntries() {
        return Collections.unmodifiableList(
                entries.values().stream()
                        .filter(Coordinates::isPinned)
                        .collect(Collectors.toList())
        );
    }

    /**
     * Returns whether there are pinned entries in the specified world.
     *
     * @param world Target world name
     * @return true if pinned entries exist, false if they do not
     */
    public static boolean hasPinnedEntriesByWorld(String world) {
        return entries.values().stream()
                .anyMatch(entry -> entry.isPinned() && entry.world.equals(world));
    }

    /**
     * Returns pinned entries only in the specified world as an immutable list.
     *
     * @param world Target world name (e.g., "overworld")
     * @return Immutable list of pinned entries in the specified world
     */
    public static List<Coordinates> getPinnedEntriesByWorld(String world) {
        return Collections.unmodifiableList(
            entries.values().stream()
                   .filter(Coordinates::isPinned)
                   .filter(entry -> entry.world.equals(world))
                   .collect(Collectors.toList())
        );
    }

    // ----------------------------------------------------------------
    // Listener management
    // ----------------------------------------------------------------

    /**
     * Notifies all registered listeners when a new entry is added.
     *
     * @param entry The added coordinate entry.
     */
    private static void notifyEntryAdded(Coordinates entry) {
        synchronized (listeners) {
            for (CoordinatesDataListener listener : listeners) {
                try {
                    listener.onEntryAdded(entry);
                } catch (Exception e) {
                    LOGGER.error("Error in notifyEntryAdded", e);
                }
            }
        }
    }

    /**
     * Registers a listener for coordinate data updates.
     *
     * @param listener The listener to register.
     */
    public static void registerListener(CoordinatesDataListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a previously registered listener.
     *
     * @param listener The listener to unregister.
     */
    public static void unregisterListener(CoordinatesDataListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }
}
 