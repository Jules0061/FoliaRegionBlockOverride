package dev.Jules.foliaRegionBlockOverride;

import dev.Jules.foliaRegionBlockOverride.command.CommandManager;
import dev.Jules.foliaRegionBlockOverride.config.ConfigManager;
import dev.Jules.foliaRegionBlockOverride.config.MessageManager;
import dev.Jules.foliaRegionBlockOverride.hook.WorldGuardHook;
import dev.Jules.foliaRegionBlockOverride.listener.BlockBreakListener;
import dev.Jules.foliaRegionBlockOverride.listener.BlockPlaceListener;
import dev.Jules.foliaRegionBlockOverride.listener.BucketEmptyListener;
import dev.Jules.foliaRegionBlockOverride.listener.WorldGuardBreakListener;
import dev.Jules.foliaRegionBlockOverride.listener.WorldGuardPlaceListener;
import dev.Jules.foliaRegionBlockOverride.manager.BlockOverrideManager;
import dev.Jules.foliaRegionBlockOverride.manager.BlockTrackingManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FoliaRegionBlockOverride extends JavaPlugin {

    private ConfigManager configManager;
    private MessageManager messageManager;
    private WorldGuardHook worldGuardHook;
    private BlockOverrideManager blockOverrideManager;
    private BlockTrackingManager blockTrackingManager;

    @Override
    public void onEnable() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) {
            getLogger().severe("WorldGuard not found. Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.configManager.load();
        this.messageManager.load();

        try {
            this.worldGuardHook = new WorldGuardHook();
        } catch (Throwable throwable) {
            getLogger().severe("Failed to hook WorldGuard: " + throwable.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.blockOverrideManager = new BlockOverrideManager(this);
        this.blockTrackingManager = new BlockTrackingManager(this);

        getServer().getPluginManager().registerEvents(new WorldGuardPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldGuardBreakListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockPlaceListener(this), this);
        getServer().getPluginManager().registerEvents(new BucketEmptyListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(this), this);

        CommandManager commandManager = new CommandManager(this);
        commandManager.register();

        getLogger().info("FoliaRegionBlockOverride enabled.");
    }

    @Override
    public void onDisable() {
        if (blockTrackingManager != null) {
            blockTrackingManager.shutdown();
        }
    }

    public void reloadAll() {
        configManager.load();
        messageManager.load();
    }

    public ConfigManager configManager() {
        return configManager;
    }

    public MessageManager messageManager() {
        return messageManager;
    }

    public WorldGuardHook worldGuardHook() {
        return worldGuardHook;
    }

    public BlockOverrideManager blockOverrideManager() {
        return blockOverrideManager;
    }

    public BlockTrackingManager blockTrackingManager() {
        return blockTrackingManager;
    }
}
