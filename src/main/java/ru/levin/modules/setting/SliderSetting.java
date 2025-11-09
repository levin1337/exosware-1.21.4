package ru.levin.modules.setting;


import net.minecraft.util.math.MathHelper;

import java.util.function.Supplier;

public class SliderSetting extends Setting {

    public boolean dragging;
    public float circleScale = 1f;
    private String name;
    private double value;
    private double min;
    private double max;
    private double increment;
    private Supplier<Boolean> visible;
    public double circlePos = -1;
    public SliderSetting(String name, double value, double min, double max,double increment) {
        this.name = name;
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
        setVisible(() -> true);
    }

    public SliderSetting(String name, double value, double min, double max,double increment, Supplier<Boolean> visible) {
        this.name = name;
        this.value = value;
        this.min = min;
        this.max = max;
        this.increment = increment;
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

    public Number get() {
        return MathHelper.clamp(value, getMin(), getMax());
    }

    public void set(double value) {
        this.value = MathHelper.clamp(value, getMin(), getMax());
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public double getIncrement() {
        return this.increment;
    }
}
