package dev.ysknkd.mc.coordinates.config;

import com.google.gson.Gson;
import dev.ysknkd.mc.coordinates.CoordinatesApp;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration for saving coordinate settings.
 * Allows toggling the default pin state for new entries.
 */
public class Config {
    // Determines whether the pin state is enabled by default when saving (default is false)
    private static final boolean DEFAULT_PIN_STATE = false;
    private static boolean defaultPinState = false;
    
    // Default minimum distance to show player indicators (default is 10.0)
    private static final double DEFAULT_MIN_INDICATOR_DISTANCE = 10.0;
    private static double minIndicatorDistance = DEFAULT_MIN_INDICATOR_DISTANCE;
    
    // Stores the current worldId (initially "unknown")
    private static String currentWorldId = "unknown";

    // Internal structure for JSON serialization of configuration data
    private static class ConfigData {
        boolean defaultPinState;
        double minIndicatorDistance;
    }
    
    /**
     * Returns the configuration file path for the specified worldId.
     *
     * @param worldId The target world ID
     * @return The configuration file path
     */
    private static Path getConfigFilePath(String worldId) {
        return Paths.get("config", CoordinatesApp.MOD_ID, worldId, "config.json");
    }
    
    /**
     * Loads configuration from the file associated with the specified worldId.
     * If the file does not exist, the default value (defaultPinState=false) is retained.
     * Also stores the current worldId internally.
     *
     * @param worldId The world ID for which to load the configuration
     */
    public static void load(String worldId) {
        currentWorldId = worldId; // Store the worldId internally
        Path configFile = getConfigFilePath(worldId);
        if (!Files.exists(configFile)) {
            return;
        }
        Gson gson = new Gson();
        try (Reader reader = Files.newBufferedReader(configFile, StandardCharsets.UTF_8)) {
            ConfigData data = gson.fromJson(reader, ConfigData.class);
            if (data != null) {
                defaultPinState = data.defaultPinState;
                // Handle the case where the config file is from an older version without this property
                if (data.minIndicatorDistance > 0) {
                    minIndicatorDistance = data.minIndicatorDistance;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Saves the current configuration to the file corresponding to the stored worldId.
     * Automatically creates the target directory if it does not exist.
     */
    public static void save() {
        Path configFile = getConfigFilePath(Config.currentWorldId);
        try {
            Files.createDirectories(configFile.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        Gson gson = new Gson();
        ConfigData data = new ConfigData();
        data.defaultPinState = defaultPinState;
        data.minIndicatorDistance = minIndicatorDistance;
        try (Writer writer = Files.newBufferedWriter(configFile, StandardCharsets.UTF_8)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Sets the default pin state for saving coordinates.
     *
     * @param state true to enable, false to disable
     */
    public static void setDefaultPinState(boolean state) {
        defaultPinState = state;
    }
    
    /**
     * Returns the current default pin state.
     *
     * @return true if enabled, false otherwise
     */
    public static boolean getDefaultPinState() {
        return defaultPinState;
    }
    
    /**
     * Toggles the default pin state (enables it if disabled, disables it if enabled).
     */
    public static void toggleDefaultPinState() {
        defaultPinState = !defaultPinState;
    }

    /**
     * Returns the minimum distance for showing player indicators.
     *
     * @return the minimum distance in blocks
     */
    public static double getMinIndicatorDistance() {
        return minIndicatorDistance;
    }
    
    /**
     * Sets the minimum distance for showing player indicators.
     *
     * @param distance the minimum distance in blocks
     */
    public static void setMinIndicatorDistance(double distance) {
        minIndicatorDistance = distance;
    }
    
    /**
     * Increases the minimum indicator distance by 5 blocks up to a maximum of 50.
     */
    public static void increaseMinIndicatorDistance() {
        minIndicatorDistance = Math.min(50.0, minIndicatorDistance + 5.0);
    }
    
    /**
     * Decreases the minimum indicator distance by 5 blocks down to a minimum of 0.
     */
    public static void decreaseMinIndicatorDistance() {
        minIndicatorDistance = Math.max(0.0, minIndicatorDistance - 5.0);
    }
    
    /**
     * Resets the configuration to its default values.
     */
    public static void reset() {
        defaultPinState = DEFAULT_PIN_STATE;
        minIndicatorDistance = DEFAULT_MIN_INDICATOR_DISTANCE;
    }
} 