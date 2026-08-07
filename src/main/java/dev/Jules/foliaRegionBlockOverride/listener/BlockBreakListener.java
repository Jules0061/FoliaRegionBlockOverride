package dev.Jules.foliaRegionBlockOverride.listener;

import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import dev.Jules.foliaRegionBlockOverride.model.TrackedBlock;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public final class BlockBreakListener implements Listener {

    private final FoliaRegionBlockOverride plugin;

    public BlockBreakListener(FoliaRegionBlockOverride plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onBreak(BlockBreakEvent event) {
        Location location = event.getBlock().getLocation();
        TrackedBlock trackedBlock = plugin.blockTrackingManager().get(location);
        if (trackedBlock != null) {
            if (event.getPlayer().hasPermission("regionblock.bypass")) {
                plugin.blockTrackingManager().untrack(location);
                plugin.placedBlockManager().removePlaced(location);
                event.setCancelled(false);
                return;
            }

            if (trackedBlock.allowBreak()) {
                plugin.blockTrackingManager().untrack(location);
                plugin.placedBlockManager().removePlaced(location);
                event.setCancelled(false);
                return;
            }

            event.setCancelled(true);
            plugin.messageManager().send(event.getPlayer(), "break-denied");
            return;
        }

        if (event.getPlayer().hasPermission("regionblock.bypass")) {
            plugin.placedBlockManager().removePlaced(location);
            return;
        }
        if (plugin.blockOverrideManager().onlyBreakPlayerPlacedRegion(location) == null) {
            return;
        }
        if (plugin.placedBlockManager().isPlaced(location)) {
            plugin.placedBlockManager().removePlaced(location);
            return;
        }
        event.setCancelled(true);
        plugin.messageManager().send(event.getPlayer(), "break-not-player-placed");
    }
}
