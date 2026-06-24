package dev.Jules.foliaRegionBlockOverride.listener;

import com.sk89q.worldguard.bukkit.event.block.BreakBlockEvent;
import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class WorldGuardBreakListener implements Listener {

    private final FoliaRegionBlockOverride plugin;

    public WorldGuardBreakListener(FoliaRegionBlockOverride plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBreak(BreakBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();
        if (player != null && player.hasPermission("regionblock.bypass")) {
            return;
        }

        for (Block block : event.getBlocks()) {
            if (plugin.blockTrackingManager().get(block.getLocation()) != null) {
                event.setSilent(true);
                return;
            }
        }
    }
}
