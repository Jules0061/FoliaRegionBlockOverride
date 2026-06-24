package dev.Jules.foliaRegionBlockOverride.command;

import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import dev.Jules.foliaRegionBlockOverride.model.BlockRule;
import dev.Jules.foliaRegionBlockOverride.model.RegionConfig;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class CommandManager implements TabExecutor {

    private static final List<String> SUBCOMMANDS = List.of("reload", "info");

    private final FoliaRegionBlockOverride plugin;

    public CommandManager(FoliaRegionBlockOverride plugin) {
        this.plugin = plugin;
    }

    public void register() {
        org.bukkit.command.PluginCommand command = plugin.getCommand("regionblock");
        if (command != null) {
            command.setExecutor(this);
            command.setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NonNull [] args) {
        if (args.length == 0) {
            plugin.messageManager().send(sender, "usage");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "reload" -> {
                if (!sender.hasPermission("regionblock.reload")) {
                    plugin.messageManager().send(sender, "no-permission");
                    return true;
                }
                try {
                    plugin.reloadAll();
                    plugin.messageManager().send(sender, "reload-success");
                } catch (Exception exception) {
                    plugin.messageManager().send(sender, "reload-failed", Map.of("error", String.valueOf(exception.getMessage())));
                    plugin.getLogger().severe("Reload failed: " + exception.getMessage());
                }
            }
            case "info" -> {
                if (!sender.hasPermission("regionblock.admin")) {
                    plugin.messageManager().send(sender, "no-permission");
                    return true;
                }
                sendInfo(sender);
            }
            default -> plugin.messageManager().send(sender, "usage");
        }
        return true;
    }

    private void sendInfo(CommandSender sender) {
        Map<String, RegionConfig> regions = plugin.configManager().regions();
        sender.sendMessage(Component.text("FoliaRegionBlockOverride", NamedTextColor.GOLD)
                .append(Component.text(" — tracked blocks: " + plugin.blockTrackingManager().size(), NamedTextColor.GRAY)));
        if (regions.isEmpty()) {
            sender.sendMessage(Component.text("No regions configured.", NamedTextColor.RED));
            return;
        }
        for (RegionConfig region : regions.values()) {
            Component header = Component.text("Region ", NamedTextColor.YELLOW)
                    .append(Component.text(region.name(), NamedTextColor.WHITE))
                    .append(Component.text(region.enabled() ? " [enabled]" : " [disabled]",
                            region.enabled() ? NamedTextColor.GREEN : NamedTextColor.RED));
            sender.sendMessage(header);
            for (BlockRule rule : region.rules().values()) {
                sender.sendMessage(Component.text("  - ", NamedTextColor.DARK_GRAY)
                        .append(Component.text(rule.material().name(), NamedTextColor.AQUA))
                        .append(Component.text(" despawn=" + (rule.despawnTicks() / 20.0) + "s", NamedTextColor.GRAY))
                        .append(Component.text(" break=" + rule.allowBreak(), NamedTextColor.GRAY)));
            }
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NonNull [] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            List<String> out = new ArrayList<>();
            for (String sub : SUBCOMMANDS) {
                if (sub.startsWith(partial)) {
                    out.add(sub);
                }
            }
            return out;
        }
        return List.of();
    }
}
