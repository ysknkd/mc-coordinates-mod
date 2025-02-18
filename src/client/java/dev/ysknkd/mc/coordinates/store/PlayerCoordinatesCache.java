package dev.ysknkd.mc.coordinates.store;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerCoordinatesCache {
    private final static ConcurrentHashMap<UUID, PlayerCoordinates> coordinatesMap = new ConcurrentHashMap<>();
    
    private PlayerCoordinatesCache() {}
    
    public static void update(PlayerCoordinates entity) {
        coordinatesMap.compute(entity.uuid, (key, existing) -> {
            if (existing != null) {
                existing.x = entity.x;
                existing.y = entity.y;
                existing.z = entity.z;
                existing.world = entity.world;
                existing.name = entity.name;
                return existing;
            }
            return entity;
        });
    }

    public static void clear() {
        coordinatesMap.clear();
    }
    
    public static void cleanOfflinePlayers(Set<UUID> onlinePlayers) {
         coordinatesMap.keySet().removeIf(uuid -> !onlinePlayers.contains(uuid));
    }
    
    public static void remove(UUID uuid) {
         coordinatesMap.remove(uuid);
    }
    
    public static List<PlayerCoordinates> getCoordinatesList() {
        return coordinatesMap.values().stream().collect(Collectors.toList());
    }
}