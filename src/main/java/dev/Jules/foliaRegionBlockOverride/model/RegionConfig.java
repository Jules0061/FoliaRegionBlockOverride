package dev.Jules.foliaRegionBlockOverride.model;

import org.bukkit.Material;

import java.util.Map;

public record RegionConfig(String name, boolean enabled, Map<Material, BlockRule> rules) {

    public BlockRule rule(Material material) {
        return rules.get(material);
    }
}
