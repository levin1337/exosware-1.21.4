package ru.levin.manager.themeManager;

import net.minecraft.util.math.MathHelper;
import ru.levin.manager.IMinecraft;
import ru.levin.modules.player.GuiWalk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("All")
public class StyleManager implements IMinecraft {
    private final List<Style> styles = new CopyOnWriteArrayList<>();
    private Style currentStyle;

    private final Path file = Paths.get(Objects.requireNonNull(mc.runDirectory).getAbsolutePath(), "files", "themes.ew");

    public void init() {
        addStyle("Клиентский", "#5433FF", "#00FFFF");
        addStyle("Осень", "#FF7D00", "#FFD700");
        addStyle("Кислотный", "#CCFF00", "#00FF00");
        addStyle("Океан", "#0077BE", "#00B4D8");
        addStyle("Вишневый", "#8B0000", "#FF1493");
        loadCustomThemes();
        if (!styles.isEmpty()) {
            currentStyle = styles.get(0);
        }
    }

    private void addStyle(String name, String... hexColors) {
        int[] colors = new int[hexColors.length];
        for (int i = 0; i < hexColors.length; i++) {
            colors[i] = HexColor.toColor(hexColors[i]);
        }
        styles.add(new Style(name, colors));
    }

    public void addCustomTheme(String name, int color1, int color2) {
        Style style = new Style(name, new int[]{color1, color2});
        styles.add(style);
        saveCustomThemes();
    }

    public void removeStyle(Style style) {
        if (style == null) return;
        if (!styles.contains(style)) return;
        if (!style.name.toLowerCase().startsWith("custom")) return;
        styles.remove(style);
        saveCustomThemes();

        if (currentStyle == style) {
            currentStyle = !styles.isEmpty() ? styles.get(0) : null;
        }
    }

    public void setTheme(Style style) {
        if (styles.contains(style)) {
            currentStyle = style;
        }
    }

    public Style getTheme() {
        return currentStyle;
    }

    public List<Style> getStyles() {
        return styles;
    }

    public int getFirstColor() {
        return currentStyle != null && currentStyle.colors.length > 0 ? currentStyle.colors[0] : -1;
    }

    public int getSecondColor() {
        return currentStyle != null && currentStyle.colors.length > 1 ? currentStyle.colors[1] : getFirstColor();
    }

    private void saveCustomThemes() {
        try {
            Files.createDirectories(file.getParent());

            List<String> lines = new ArrayList<>();
            for (Style style : styles) {
                if (style.name.toLowerCase().startsWith("custom")) {
                    StringBuilder sb = new StringBuilder(style.name);
                    for (int color : style.colors) {
                        sb.append(":").append(colorToHex(color));
                    }
                    lines.add(sb.toString());
                }
            }

            Files.write(file, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save themes: " + e.getMessage());
        }
    }

    private void loadCustomThemes() {
        try {
            Files.createDirectories(file.getParent());
            if (Files.notExists(file)) {
                Files.createFile(file);
                return;
            }

            Files.lines(file, StandardCharsets.UTF_8)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(line -> {
                        try {
                            String[] parts = line.split(":");
                            if (parts.length >= 3) {
                                String name = parts[0];
                                String hex1 = parts[1];
                                String hex2 = parts[2];
                                addStyle(name, hex1, hex2);
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to parse theme line: " + line);
                        }
                    });
        } catch (IOException e) {
            System.err.println("Failed to load themes: " + e.getMessage());
        }
    }

    private static String colorToHex(int color) {
        int rgb = color & 0xFFFFFF;
        return "#" + String.format("%06X", rgb);
    }

    public static class HexColor {
        public static int toColor(String hexColor) {
            int rgb = Integer.parseInt(hexColor.substring(1), 16);
            return reAlphaInt(rgb, 255);
        }

        public static int reAlphaInt(int color, int alpha) {
            return (MathHelper.clamp(alpha, 0, 255) << 24) | (color & 0xFFFFFF);
        }
    }
}
