package dev.Jules.foliaRegionBlockOverride.model;

import org.bukkit.Material;

public record BlockRule(Material material, long despawnTicks, boolean allowBreak) {
}
