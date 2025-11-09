package ru.levin.modules.setting;


import java.util.function.Supplier;

public class BooleanSetting extends Setting {

    private String name;
    private String desc;
    private boolean status;
    private Supplier<Boolean> visible;

    public BooleanSetting(String name, boolean status) {
        this.name = name;
        this.status = status;
        setVisible(() -> true);
    }
    public BooleanSetting(String name, boolean status, String desc) {
        this.name = name;
        this.status = status;
        this.desc = desc;
        setVisible(() -> true);
    }

    public BooleanSetting(String name, boolean status, Supplier<Boolean> visible) {
        this.name = name;
        this.status = status;
        setVisible(visible);
    }
    public BooleanSetting(String name, boolean status, String desc, Supplier<Boolean> visible) {
        this.name = name;
        this.status = status;
        this.desc = desc;
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

    public String getDesc() {
        return this.desc;
    }
    public boolean get() {
        return this.status;
    }

    public void set(boolean value) {
        this.status = value;
    }
}