package dev.Jules.foliaRegionBlockOverride.hook;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public final class WorldGuardHook {

    private final RegionContainer container;

    public WorldGuardHook() {
        this.container = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    public Set<String> regionIds(Location location) {
        if (location.getWorld() == null) {
            return Collections.emptySet();
        }
        RegionManager manager = container.get(BukkitAdapter.adapt(location.getWorld()));
        if (manager == null) {
            return Collections.emptySet();
        }
        BlockVector3 vector = BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        ApplicableRegionSet set = manager.getApplicableRegions(vector);
        if (set.size() == 0) {
            return Collections.emptySet();
        }
        Set<String> ids = new HashSet<>(set.size());
        for (ProtectedRegion region : set) {
            ids.add(region.getId().toLowerCase());
        }
        return ids;
    }
}
