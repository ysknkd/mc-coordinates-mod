package dev.ysknkd.mc.coordinates.store;

import java.util.UUID;

/**
 * Coordinate entry.
 */
public class Coordinates {
    public UUID uuid;
    public double x;
    public double y;
    public double z;

    public String description;
    public boolean favorite;
    public boolean pinned;
    public long savedTime;
    
    public String world;

    public String icon;
    public boolean share;

    /**
     * Constructor for new entries, automatically generating a new UUID.
     *
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param z The z-coordinate.
     * @param description Description for the coordinate.
     * @param world The associated world (e.g., "minecraft:overworld", "minecraft:the_nether").
     */
    public Coordinates(double x, double y, double z, String description, String world, boolean pinned, String icon) {
        this.uuid = UUID.randomUUID();
        this.x = x;
        this.y = y;
        this.z = z;
        this.description = description;
        this.world = world;
        this.favorite = false;
        this.pinned = pinned;
        this.icon = icon;
        this.savedTime = System.currentTimeMillis();
    }
    
    public Coordinates(UUID uuid, double x, double y, double z, String description, String world, boolean pinned, String icon) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.description = description;
        this.world = world;
        this.favorite = false;
        this.pinned = pinned;
        this.icon = icon;
        this.savedTime = System.currentTimeMillis();
    }

    /**
     * Formats the coordinate data into a string for display.
     *
     * @return The formatted coordinate string.
     */
    public String getCoordinatesText() {
        String worldName = world.replace("minecraft:", "");
        return String.format("X: %.1f, Y: %.1f, Z: %.1f [%s]", x, y, z, worldName);
    }

    public boolean isPinned() {
        return pinned;
    }
} 