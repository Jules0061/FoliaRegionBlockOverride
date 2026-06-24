package dev.Jules.foliaRegionBlockOverride.listener;

import com.sk89q.worldguard.bukkit.event.block.PlaceBlockEvent;
import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public final class WorldGuardPlaceListener implements Listener {

    private final FoliaRegionBlockOverride plugin;

    public WorldGuardPlaceListener(FoliaRegionBlockOverride plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlace(PlaceBlockEvent event) {
        Player player = event.getCause().getFirstPlayer();
        if (player != null && player.hasPermission("regionblock.bypass")) {
            return;
        }

        Material material = event.getEffectiveMaterial();
        for (Block block : event.getBlocks()) {
            if (plugin.blockOverrideManager().resolve(block.getLocation(), material) != null) {
                event.setSilent(true);
                return;
            }
        }
    }
}
