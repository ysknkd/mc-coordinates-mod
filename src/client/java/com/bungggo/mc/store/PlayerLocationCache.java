package com.bungggo.mc.store;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerLocationCache {
    private final static ConcurrentHashMap<UUID, PlayerLocationEntity> locations = new ConcurrentHashMap<>();
    
    private PlayerLocationCache() {}
    
    public static void update(PlayerLocationEntity entity) {
        locations.compute(entity.uuid, (key, existing) -> {
            if (existing != null) {
                existing.x = entity.x;
                existing.y = entity.y;
                existing.z = entity.z;
                existing.world = entity.world;
                return existing;
            }
            return entity;
        });
    }

    public static void clear() {
        locations.clear();
    }
    
    public static void cleanOfflinePlayers(Set<UUID> onlinePlayers) {
         locations.keySet().removeIf(uuid -> !onlinePlayers.contains(uuid));
    }
    
    public static void remove(UUID uuid) {
         locations.remove(uuid);
    }
    
    public static List<PlayerLocationEntity> getLocations() {
        return locations.values().stream().collect(Collectors.toList());
    }
}