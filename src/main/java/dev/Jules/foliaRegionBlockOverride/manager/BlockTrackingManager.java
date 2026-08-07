package dev.Jules.foliaRegionBlockOverride.manager;

import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import dev.Jules.foliaRegionBlockOverride.model.BlockKey;
import dev.Jules.foliaRegionBlockOverride.model.BlockRule;
import dev.Jules.foliaRegionBlockOverride.model.TrackedBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class BlockTrackingManager {

    private final FoliaRegionBlockOverride plugin;
    private final Map<BlockKey, TrackedBlock> tracked = new ConcurrentHashMap<>();

    public BlockTrackingManager(FoliaRegionBlockOverride plugin) {
        this.plugin = plugin;
    }

    public void track(Location location, String region, BlockRule rule) {
        BlockKey key = BlockKey.of(location);
        TrackedBlock previous = tracked.remove(key);
        if (previous != null) {
            previous.cancelTask();
        }

        TrackedBlock trackedBlock = new TrackedBlock(rule.material(), rule.allowBreak());
        tracked.put(key, trackedBlock);

        long delay = Math.max(1L, rule.despawnTicks());
        Location despawnLocation = location.toBlockLocation();
        trackedBlock.task(Bukkit.getRegionScheduler().runDelayed(plugin, despawnLocation, scheduledTask -> despawn(key, despawnLocation, rule.material()), delay));

        if (plugin.configManager().debug()) {
            plugin.getLogger().info("Tracking " + rule.material() + " at " + key + " for " + delay + " ticks (region " + region + ").");
        }
    }

    public TrackedBlock get(Location location) {
        return tracked.get(BlockKey.of(location));
    }

    public void untrack(Location location) {
        TrackedBlock trackedBlock = tracked.remove(BlockKey.of(location));
        if (trackedBlock != null) {
            trackedBlock.cancelTask();
        }
    }

    private void despawn(BlockKey key, Location location, Material expected) {
        TrackedBlock trackedBlock = tracked.remove(key);
        if (trackedBlock == null) {
            return;
        }
        plugin.placedBlockManager().removePlaced(location);
        Block block = location.getBlock();
        if (block.getType() == expected) {
            block.setType(Material.AIR, needsPhysics(expected));
            if (plugin.configManager().debug()) {
                plugin.getLogger().info("Despawned " + expected + " at " + key + ".");
            }
        }
    }

    private static boolean needsPhysics(Material material) {
        return material == Material.WATER || material == Material.LAVA;
    }

    public void shutdown() {
        for (Map.Entry<BlockKey, TrackedBlock> entry : tracked.entrySet()) {
            TrackedBlock trackedBlock = entry.getValue();
            trackedBlock.cancelTask();
            BlockKey key = entry.getKey();
            Material expected = trackedBlock.material();
            Location location = new Location(Bukkit.getWorld(key.world()), key.x(), key.y(), key.z());
            if (location.getWorld() == null) {
                continue;
            }
            Bukkit.getRegionScheduler().execute(plugin, location, () -> {
                Block block = location.getBlock();
                if (block.getType() == expected) {
                    block.setType(Material.AIR, needsPhysics(expected));
                }
            });
        }
        tracked.clear();
    }

    public int size() {
        return tracked.size();
    }
}
