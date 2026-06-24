package dev.Jules.foliaRegionBlockOverride.listener;

import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import dev.Jules.foliaRegionBlockOverride.model.TrackedBlock;
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
        TrackedBlock trackedBlock = plugin.blockTrackingManager().get(event.getBlock().getLocation());
        if (trackedBlock == null) {
            return;
        }

        if (event.getPlayer().hasPermission("regionblock.bypass")) {
            plugin.blockTrackingManager().untrack(event.getBlock().getLocation());
            event.setCancelled(false);
            return;
        }

        if (trackedBlock.allowBreak()) {
            plugin.blockTrackingManager().untrack(event.getBlock().getLocation());
            event.setCancelled(false);
            return;
        }

        event.setCancelled(true);
        plugin.messageManager().send(event.getPlayer(), "break-denied");
    }
}
