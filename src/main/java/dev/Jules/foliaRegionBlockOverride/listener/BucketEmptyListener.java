package dev.Jules.foliaRegionBlockOverride.listener;

import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import dev.Jules.foliaRegionBlockOverride.manager.BlockOverrideManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBucketEmptyEvent;

import java.util.Map;

/**
 * Fluids such as water, lava and powder snow are placed with a bucket, which fires a
 * {@link PlayerBucketEmptyEvent} instead of a BlockPlaceEvent. This listener mirrors
 * {@link BlockPlaceListener} so those materials can be overridden and tracked too.
 */
public final class BucketEmptyListener implements Listener {

    private final FoliaRegionBlockOverride plugin;

    public BucketEmptyListener(FoliaRegionBlockOverride plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEmpty(PlayerBucketEmptyEvent event) {
        if (event.getPlayer().hasPermission("regionblock.bypass")) {
            return;
        }

        Material material = placedMaterial(event.getBucket());
        if (material == null) {
            return;
        }

        Block block = event.getBlock();
        Location location = block.getLocation();

        if (plugin.blockOverrideManager().onlyBreakPlayerPlacedRegion(location) != null) {
            plugin.placedBlockManager().markPlaced(location);
        }

        if (plugin.blockOverrideManager().blacklistedRegion(location, material) != null) {
            event.setCancelled(true);
            plugin.messageManager().send(event.getPlayer(), "place-blacklisted",
                    Map.of("block", material.name()));
            return;
        }

        BlockOverrideManager.Match match = plugin.blockOverrideManager().resolve(location, material);
        if (match == null) {
            return;
        }

        if (event.isCancelled()) {
            event.setCancelled(false);
        }

        plugin.blockTrackingManager().track(location, match.region(), match.rule());
    }

    private static Material placedMaterial(Material bucket) {
        return switch (bucket) {
            case WATER_BUCKET, COD_BUCKET, SALMON_BUCKET, PUFFERFISH_BUCKET,
                 TROPICAL_FISH_BUCKET, AXOLOTL_BUCKET, TADPOLE_BUCKET -> Material.WATER;
            case LAVA_BUCKET -> Material.LAVA;
            case POWDER_SNOW_BUCKET -> Material.POWDER_SNOW;
            default -> null;
        };
    }
}
