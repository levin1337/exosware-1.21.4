package ru.levin.modules.setting;

import java.util.*;
import java.util.function.Supplier;

public class MultiSetting extends Setting {
    private final String name;
    private final List<String> modes;
    private final Set<String> selected;
    private Supplier<Boolean> visible;
    public boolean expanded;
    public int modeOffset;

    public int getModeOffset() {
        return modeOffset;
    }
    public MultiSetting(String name, String[] modes) {
        this(name, Collections.emptySet(), modes);
    }

    public MultiSetting(String name, Collection<String> selected, String[] modes) {
        this(name, selected, Arrays.asList(modes));
    }
    public MultiSetting(Supplier<Boolean> visible, String name, Collection<String> selected, String[] modes) {
        this(name, selected, Arrays.asList(modes),visible);
    }

    public MultiSetting(String name, Collection<String> selected, List<String> modes) {
        this.name = name;
        this.modes = new ArrayList<>(modes);
        this.selected = new LinkedHashSet<>();
        setVisible(() -> true);
        setSelected(selected);
    }
    public MultiSetting(String name, Collection<String> selected, List<String> modes, Supplier<Boolean> visible) {
        this.name = name;
        this.modes = new ArrayList<>(modes);
        this.selected = new LinkedHashSet<>();
        setVisible(visible);
        setSelected(selected);
    }


    public void toggle(String mode) {
        if (modes.contains(mode)) {
            if (selected.contains(mode)) {
                selected.remove(mode);
            } else {
                selected.add(mode);
            }
        }
    }

    public void setSelected(Collection<String> modes) {
        selected.clear();
        modes.stream().filter(this.modes::contains).forEach(selected::add);
    }

    public void clearSelection() {
        selected.clear();
    }

    public boolean get(String name) {
        return selected.contains(name);
    }

    public boolean hasAnySelected() {
        return !selected.isEmpty();
    }


    public String getConfigValue() {
        return String.join(",", selected);
    }

    public void setConfigValue(String value) {
        setSelected(Arrays.asList(value.split(",")));
    }


    public List<String> getAvailableModes() {
        return new ArrayList<>(modes);
    }

    public Set<String> getSelectedModes() {
        return new LinkedHashSet<>(selected);
    }

    public String getName() {
        return name;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }

    public int getAllSelected() {
        return selected.size();
    }
}