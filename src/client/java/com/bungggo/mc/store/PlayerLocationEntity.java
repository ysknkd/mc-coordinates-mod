package com.bungggo.mc.store;

import java.util.UUID;

public class PlayerLocationEntity {
    public UUID uuid;
    public double x;
    public double y;
    public double z;
    public String world;
    
    public PlayerLocationEntity(UUID uuid, double x, double y, double z, String world) {
        this.uuid = uuid;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
}
