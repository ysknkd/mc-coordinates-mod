package dev.ysknkd.mc.coordinates.store;

import java.util.UUID;

public class PlayerCoordinates {
    public UUID uuid;
    public double x;
    public double y;
    public double z;
    public String name;
    public String world;

    public PlayerCoordinates(UUID uuid, double x, double y, double z, String name, String world) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.name = name;
        this.world = world;
    }
}
