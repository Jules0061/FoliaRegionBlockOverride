package dev.Jules.foliaRegionBlockOverride.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtil {

    private static final Pattern PATTERN = Pattern.compile("(?i)\\s*(\\d+)\\s*(ms|t|s|m|h)?\\s*");

    private TimeUtil() {
    }

    public static long parseTicks(String raw, long fallbackTicks) {
        if (raw == null || raw.isBlank()) {
            return fallbackTicks;
        }
        Matcher matcher = PATTERN.matcher(raw.trim());
        if (!matcher.matches()) {
            return fallbackTicks;
        }
        long value = Long.parseLong(matcher.group(1));
        String unit = matcher.group(2) == null ? "s" : matcher.group(2).toLowerCase();
        return switch (unit) {
            case "ms" -> Math.max(1L, value / 50L);
            case "t" -> value;
            case "m" -> value * 60L * 20L;
            case "h" -> value * 60L * 60L * 20L;
            default -> value * 20L;
        };
    }
}
