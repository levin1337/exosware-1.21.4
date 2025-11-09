package ru.levin.util.player;

import ru.levin.manager.IMinecraft;

public class GCDUtil implements IMinecraft {
    public static float getSensitivity(float rotation) {
        float gcdValue = getGCDValue();
        return getDeltaMouse(rotation, gcdValue) * gcdValue;
    }

    public static float getGCDValue() {
        return getGCD() * 0.15f;
    }

    public static float getGCD() {
        float sens = (float) (mc.options.getMouseSensitivity().getValue() * 0.6 + 0.2);
        return sens * sens * sens * 8.0f;
    }

    public static float getDeltaMouse(float delta, float gcdValue) {
        return Math.round(delta / gcdValue);
    }
}
