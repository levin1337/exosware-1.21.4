package ru.levin.util.render;


import java.util.Objects;
import lombok.Generated;
import net.minecraft.util.math.MathHelper;

public class ColorRGBA {
    public static final ColorRGBA WHITE = new ColorRGBA(255, 255, 255);
    public static final ColorRGBA BLACK = new ColorRGBA(0, 0, 0);
    public static final ColorRGBA GREEN = new ColorRGBA(0, 255, 0);
    public static final ColorRGBA RED = new ColorRGBA(255, 0, 0);
    public static final ColorRGBA BLUE = new ColorRGBA(0, 0, 255);
    public static final ColorRGBA YELLOW = new ColorRGBA(255, 255, 0);
    public static final ColorRGBA GRAY = new ColorRGBA(88, 87, 93);
    private transient float[] hsbValues;
    private final int red;
    private final int green;
    private final int blue;
    private final int alpha;

    public ColorRGBA(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public ColorRGBA(int red, int green, int blue, int alpha) {
        red = MathHelper.clamp(red, 0, 255);
        green = MathHelper.clamp(green, 0, 255);
        blue = MathHelper.clamp(blue, 0, 255);
        alpha = MathHelper.clamp(alpha, 0, 255);
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    public float getHue() {
        return this.getHSBValues()[0];
    }

    public float getSaturation() {
        return this.getHSBValues()[2];
    }

    public float getBrightness() {
        return this.getHSBValues()[1];
    }

    private float[] getHSBValues() {
        if (this.hsbValues == null) {
            this.hsbValues = this.calculateHSB();
        }

        return this.hsbValues;
    }

    private float[] calculateHSB() {
        float r = (float)this.red / 255.0F;
        float g = (float)this.green / 255.0F;
        float b = (float)this.blue / 255.0F;
        float maxC = Math.max(r, Math.max(g, b));
        float minC = Math.min(r, Math.min(g, b));
        float delta = maxC - minC;
        float hue = 0.0F;
        if (delta != 0.0F) {
            if (maxC == r) {
                hue = (g - b) / delta;
            } else if (maxC == g) {
                hue = (b - r) / delta + 2.0F;
            } else {
                hue = (r - g) / delta + 4.0F;
            }

            hue /= 6.0F;
            if (hue < 0.0F) {
                ++hue;
            }
        }

        float saturation = maxC == 0.0F ? 0.0F : delta / maxC;
        return new float[]{hue, saturation, maxC};
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o != null && this.getClass() == o.getClass()) {
            ColorRGBA colorRGBA = (ColorRGBA)o;
            return Float.compare((float)this.red, (float)colorRGBA.red) == 0 && Float.compare((float)this.green, (float)colorRGBA.green) == 0 && Float.compare((float)this.blue, (float)colorRGBA.blue) == 0 && Float.compare((float)this.alpha, (float)colorRGBA.alpha) == 0;
        } else {
            return false;
        }
    }

    public float difference(ColorRGBA colorRGBA) {
        return Math.abs(this.getHue() - colorRGBA.getHue()) + Math.abs(this.getBrightness() - colorRGBA.getBrightness()) + Math.abs(this.getSaturation() - colorRGBA.getSaturation());
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.red, this.green, this.blue, this.alpha});
    }

    @Generated
    public int getRed() {
        return this.red;
    }

    @Generated
    public int getGreen() {
        return this.green;
    }

    @Generated
    public int getBlue() {
        return this.blue;
    }

    @Generated
    public int getAlpha() {
        return this.alpha;
    }
}
