package ru.levin.modules.setting;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModeSetting extends Setting {

    public boolean expanded;
    private List<String> modes;
    private String name;
    private String selected;
    private Supplier<Boolean> visible;
    public int modeOffset;

    public void resetModeOffset() {
        this.modeOffset = 0;
    }

    public void incrementModeOffset(int value) {
        this.modeOffset += value;
    }

    public int getModeOffset() {
        return this.expanded ? this.modeOffset : 0;
    }


    public ModeSetting(String name, String selected, String... modes) {
        this.name = name;
        this.selected = selected;
        this.modes = Arrays.asList(modes);
        setVisible(() -> true);
    }

    public ModeSetting(Supplier<Boolean> visible, String name, String selected,  String... modes) {
        this.name = name;
        this.selected = selected;
        this.modes = Arrays.asList(modes);
        setVisible(visible);
    }

    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }

    public String getName() {
        return this.name;
    }

    public String get() {
        return this.selected;
    }

    public boolean is(String name) {
        return selected.contains(name);
    }

    public void set(String in) {
        this.selected = in;
    }

    public int getIndex() {
        return modes.indexOf(selected);
    }

    public int getIndex(String mode) {
        return modes.indexOf(mode);
    }

    public List<String> getModes() {
        return this.modes;
    }
}