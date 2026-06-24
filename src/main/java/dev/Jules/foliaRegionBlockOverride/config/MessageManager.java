package dev.Jules.foliaRegionBlockOverride.config;

import dev.Jules.foliaRegionBlockOverride.FoliaRegionBlockOverride;
import dev.Jules.foliaRegionBlockOverride.util.TextUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class MessageManager {

    private final FoliaRegionBlockOverride plugin;
    private volatile Map<String, String> messages = Map.of();
    private volatile String prefix = "";

    public MessageManager(FoliaRegionBlockOverride plugin) {
        this.plugin = plugin;
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        try (InputStream defaultStream = plugin.getResource("messages.yml")) {
            if (defaultStream != null) {
                config.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStream, StandardCharsets.UTF_8)));
            }
        } catch (IOException ignored) {
        }

        Map<String, String> parsed = new HashMap<>();
        for (String key : config.getKeys(false)) {
            if (config.isString(key)) {
                parsed.put(key, config.getString(key, ""));
            }
        }
        this.prefix = parsed.getOrDefault("prefix", "");
        this.messages = Map.copyOf(parsed);
    }

    public String raw(String key) {
        return messages.getOrDefault(key, key);
    }

    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        String body = raw(key);
        if (body.isEmpty()) {
            return;
        }
        String full = prefix.isEmpty() ? body : prefix + " " + body;
        sender.sendMessage(TextUtil.parse(applyPlaceholders(full, placeholders)));
    }

    public void send(CommandSender sender, String key) {
        send(sender, key, Map.of());
    }

    private String applyPlaceholders(String input, Map<String, String> placeholders) {
        String result = input;
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                result = result.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return result;
    }
}
