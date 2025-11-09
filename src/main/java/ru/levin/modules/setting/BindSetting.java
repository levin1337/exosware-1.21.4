package ru.levin.modules.setting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class BindSetting extends Setting {
    private static final List<BindSetting> allBindings = new ArrayList<>();
    private Supplier<Boolean> visible;
    private int key;
    private String name;
    private boolean binding;

    public BindSetting(String name, int defaultKey) {
        this.name = name != null ? name : "Кнопка";
        this.key = defaultKey;
        this.binding = false;
        allBindings.add(this);
        setVisible(() -> true);
    }
    public BindSetting(String name, int defaultKey, Supplier<Boolean> visible) {
        this.name = name != null ? name : "Кнопка";
        this.key = defaultKey;
        this.binding = false;
        allBindings.add(this);
        setVisible(visible);
    }

    public boolean isVisible() {
        return visible != null && visible.get();
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public boolean isBinding() {
        return binding;
    }

    public void setBinding(boolean binding) {
        this.binding = binding;
    }

    public String getName() {
        return name;
    }
}
