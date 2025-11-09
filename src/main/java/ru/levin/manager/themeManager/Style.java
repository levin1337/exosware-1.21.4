package ru.levin.manager.themeManager;

public class Style {
    public final String name;
    public final int[] colors;

    public Style(String name, int... colors) {
        this.name = name;
        this.colors = colors;
    }
}
