package dev.Jules.foliaRegionBlockOverride.config;

import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import dev.Jules.foliaRegionBlockOverride.model.BlockRule;
import dev.Jules.foliaRegionBlockOverride.model.RegionConfig;
import dev.Jules.foliaRegionBlockOverride.util.TimeUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class ConfigManager {

    private final FoliaRegionBlockOverride plugin;

    private volatile Map<String, RegionConfig> regions = Map.of();
    private volatile Map<String, Set<Material>> blacklist = Map.of();
    private volatile boolean debug;
    private volatile long defaultDespawnTicks = 100L;
    private volatile boolean defaultAllowBreak = true;

    public ConfigManager(FoliaRegionBlockOverride plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        File file = new File(plugin.getDataFolder(), "config.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection settings = config.getConfigurationSection("settings");
        this.debug = settings != null && settings.getBoolean("debug", false);

        ConfigurationSection defaults = config.getConfigurationSection("defaults");
        if (defaults != null) {
            this.defaultDespawnTicks = TimeUtil.parseTicks(defaults.getString("despawn-time", "5s"), 100L);
            this.defaultAllowBreak = defaults.getBoolean("allow-break", true);
        }

        Map<String, RegionConfig> parsed = new HashMap<>();
        ConfigurationSection regionsSection = config.getConfigurationSection("regions");
        if (regionsSection != null) {
            for (String regionName : regionsSection.getKeys(false)) {
                ConfigurationSection regionSection = regionsSection.getConfigurationSection(regionName);
                if (regionSection == null) {
                    continue;
                }
                boolean enabled = regionSection.getBoolean("enabled", true);
                Map<Material, BlockRule> rules = new HashMap<>();
                ConfigurationSection blocksSection = regionSection.getConfigurationSection("blocks");
                if (blocksSection != null) {
                    for (String blockKey : blocksSection.getKeys(false)) {
                        ConfigurationSection blockSection = blocksSection.getConfigurationSection(blockKey);
                        long despawn = defaultDespawnTicks;
                        boolean allowBreak = defaultAllowBreak;
                        if (blockSection != null) {
                            despawn = TimeUtil.parseTicks(blockSection.getString("despawn-time"), defaultDespawnTicks);
                            allowBreak = blockSection.getBoolean("allow-break", defaultAllowBreak);
                        }
                        for (String blockName : blockKey.split(",")) {
                            blockName = blockName.trim();
                            if (blockName.isEmpty()) {
                                continue;
                            }
                            Material material = Material.matchMaterial(blockName);
                            if (material == null || !material.isBlock()) {
                                plugin.getLogger().warning("Invalid block '" + blockName + "' in region '" + regionName + "', skipping.");
                                continue;
                            }
                            rules.put(material, new BlockRule(material, despawn, allowBreak));
                        }
                    }
                }
                parsed.put(regionName.toLowerCase(), new RegionConfig(regionName, enabled, Map.copyOf(rules)));
            }
        }

        this.regions = Map.copyOf(parsed);

        Map<String, Set<Material>> parsedBlacklist = new HashMap<>();
        ConfigurationSection blacklistSection = config.getConfigurationSection("blacklist");
        if (blacklistSection != null) {
            for (String regionName : blacklistSection.getKeys(false)) {
                ConfigurationSection regionSection = blacklistSection.getConfigurationSection(regionName);
                if (regionSection == null || !regionSection.getBoolean("enabled", true)) {
                    continue;
                }
                Set<Material> materials = EnumSet.noneOf(Material.class);
                List<String> blockNames = regionSection.getStringList("blocks");
                for (String blockName : blockNames) {
                    Material material = Material.matchMaterial(blockName);
                    if (material == null || !material.isBlock()) {
                        plugin.getLogger().warning("Invalid blacklist block '" + blockName + "' in region '" + regionName + "', skipping.");
                        continue;
                    }
                    materials.add(material);
                }
                if (!materials.isEmpty()) {
                    parsedBlacklist.put(regionName.toLowerCase(), Set.copyOf(materials));
                }
            }
        }
        this.blacklist = Map.copyOf(parsedBlacklist);

        plugin.getLogger().info("Loaded " + regions.size() + " region(s), " + blacklist.size() + " blacklist region(s).");
    }

    public boolean isBlacklisted(String regionId, Material material) {
        if (regionId == null) {
            return false;
        }
        Set<Material> materials = blacklist.get(regionId.toLowerCase());
        return materials != null && materials.contains(material);
    }

    public RegionConfig region(String name) {
        if (name == null) {
            return null;
        }
        return regions.get(name.toLowerCase());
    }

    public Map<String, RegionConfig> regions() {
        return regions;
    }

    public boolean debug() {
        return debug;
    }
}
