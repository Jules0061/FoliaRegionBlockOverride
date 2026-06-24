package dev.Jules.foliaRegionBlockOverride.listener;

import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import dev.Jules.foliaRegionBlockOverride.manager.BlockOverrideManager;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;

public final class BlockPlaceListener implements Listener {

    private final FoliaRegionBlockOverride plugin;

    public BlockPlaceListener(FoliaRegionBlockOverride plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlace(BlockPlaceEvent event) {
        if (event.getPlayer().hasPermission("regionblock.bypass")) {
            return;
        }

        Block block = event.getBlockPlaced();
        Material material = block.getType();

        if (plugin.blockOverrideManager().blacklistedRegion(block.getLocation(), material) != null) {
            event.setCancelled(true);
            plugin.messageManager().send(event.getPlayer(), "place-blacklisted",
                    Map.of("block", material.name()));
            return;
        }

        BlockOverrideManager.Match match = plugin.blockOverrideManager().resolve(block.getLocation(), material);
        if (match == null) {
            return;
        }

        if (event.isCancelled()) {
            event.setCancelled(false);
        }

        plugin.blockTrackingManager().track(block.getLocation(), match.region(), match.rule());
    }
}
