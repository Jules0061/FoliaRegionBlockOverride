package dev.Jules.foliaRegionBlockOverride.manager;

import dev.Jules.foliaRegionBlockOverride.model.BlockKey;
import org.bukkit.Location;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Remembers locations of blocks placed by players in regions that use the
 * {@code only-break-player-placed} option, so natural terrain can be protected
 * while player-built blocks stay breakable.
 *
 * <p>State is in-memory only; it is reset when the server restarts.</p>
 */
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

    public void clear() {
        placed.clear();
    }

    public int size() {
        return placed.size();
    }
}
