package dev.Jules.foliaRegionBlockOverride.manager;

import dev.Jules.foliaRegionBlockOverride.model.BlockKey;
import org.bukkit.Location;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public final class PlacedBlockManager {

    private final Set<BlockKey> placed = ConcurrentHashMap.newKeySet();

    public void markPlaced(Location location) {
        placed.add(BlockKey.of(location));
    }

    public boolean isPlaced(Location location) {
        return placed.contains(BlockKey.of(location));
    }

    public void removePlaced(Location location) {
        placed.remove(BlockKey.of(location));
    }

    public int size() {
        return placed.size();
    }
}
