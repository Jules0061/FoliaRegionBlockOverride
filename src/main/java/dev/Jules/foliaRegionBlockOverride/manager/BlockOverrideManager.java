package dev.Jules.foliaRegionBlockOverride.manager;

import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import dev.Jules.foliaRegionBlockOverride.config.ConfigManager;
import dev.Jules.foliaRegionBlockOverride.hook.WorldGuardHook;
import dev.Jules.foliaRegionBlockOverride.model.BlockRule;
import dev.Jules.foliaRegionBlockOverride.model.RegionConfig;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.Set;

public final class BlockOverrideManager {

    public record Match(String region, BlockRule rule) {
    }

    private final ConfigManager configManager;
    private final WorldGuardHook worldGuardHook;

    public BlockOverrideManager(FoliaRegionBlockOverride plugin) {
        this.configManager = plugin.configManager();
        this.worldGuardHook = plugin.worldGuardHook();
    }

    public Match resolve(Location location, Material material) {
        Set<String> ids = worldGuardHook.regionIds(location);
        if (ids.isEmpty()) {
            return null;
        }
        for (String id : ids) {
            RegionConfig region = configManager.region(id);
            if (region == null || !region.enabled()) {
                continue;
            }
            BlockRule rule = region.rule(material);
            if (rule != null) {
                return new Match(region.name(), rule);
            }
        }
        return null;
    }

    public String onlyBreakPlayerPlacedRegion(Location location) {
        Set<String> ids = worldGuardHook.regionIds(location);
        if (ids.isEmpty()) {
            return null;
        }
        for (String id : ids) {
            RegionConfig region = configManager.region(id);
            if (region != null && region.enabled() && region.onlyBreakPlayerPlaced()) {
                return region.name();
            }
        }
        return null;
    }

    public String blacklistedRegion(Location location, Material material) {
        Set<String> ids = worldGuardHook.regionIds(location);
        if (ids.isEmpty()) {
            return null;
        }
        for (String id : ids) {
            if (configManager.isBlacklisted(id, material)) {
                return id;
            }
        }
        return null;
    }
}
