package dev.Jules.foliaRegionBlockOverride.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final Pattern HEX = Pattern.compile("(?i)&#([0-9a-f]{6})");
    private static final Pattern CODE = Pattern.compile("(?i)&([0-9a-fk-or])");

    private static final Map<Character, String> LEGACY = Map.ofEntries(
            Map.entry('0', "<black>"),
            Map.entry('1', "<dark_blue>"),
            Map.entry('2', "<dark_green>"),
            Map.entry('3', "<dark_aqua>"),
            Map.entry('4', "<dark_red>"),
            Map.entry('5', "<dark_purple>"),
            Map.entry('6', "<gold>"),
            Map.entry('7', "<gray>"),
            Map.entry('8', "<dark_gray>"),
            Map.entry('9', "<blue>"),
            Map.entry('a', "<green>"),
            Map.entry('b', "<aqua>"),
            Map.entry('c', "<red>"),
            Map.entry('d', "<light_purple>"),
            Map.entry('e', "<yellow>"),
            Map.entry('f', "<white>"),
            Map.entry('k', "<obfuscated>"),
            Map.entry('l', "<bold>"),
            Map.entry('m', "<strikethrough>"),
            Map.entry('n', "<underlined>"),
            Map.entry('o', "<italic>"),
            Map.entry('r', "<reset>")
    );

    private TextUtil() {
    }

    public static Component parse(String raw) {
        if (raw == null || raw.isEmpty()) {
            return Component.empty();
        }
        return MINI.deserialize(toMiniMessage(raw)).decoration(TextDecoration.ITALIC, false);
    }

    private static String toMiniMessage(String input) {
        String working = input.replace('§', '&');

        Matcher hexMatcher = HEX.matcher(working);
        StringBuilder hexBuffer = new StringBuilder();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(hexBuffer, "<#" + hexMatcher.group(1).toLowerCase() + ">");
        }
        hexMatcher.appendTail(hexBuffer);

        Matcher codeMatcher = CODE.matcher(hexBuffer.toString());
        StringBuilder codeBuffer = new StringBuilder();
        while (codeMatcher.find()) {
            char code = Character.toLowerCase(codeMatcher.group(1).charAt(0));
            codeMatcher.appendReplacement(codeBuffer, Matcher.quoteReplacement(LEGACY.getOrDefault(code, "")));
        }
        codeMatcher.appendTail(codeBuffer);

        return codeBuffer.toString();
    }
}
