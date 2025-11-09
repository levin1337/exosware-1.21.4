package ru.levin.modules.movement;

import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "SuperFirework",desc  = "Даёт больше буста от фейерверк", type = Type.Move)
public class SuperFirework extends Function {
    public ModeSetting mode = new ModeSetting("Мод","BravoHvH",
            "BravoHvH",
            "ReallyWorld",
            "PulseHVH",
            "Custom");
    public final SliderSetting speed = new SliderSetting("Скорость", 1.70F, 1.50F, 8.00F, 0.01F,() -> mode.is("Custom"));
    public final BooleanSetting nearBoost = new BooleanSetting("Ускорение если рядом игрок",false,"Может флагать на новый версиях GrimAC");
    public float speedXZ = 1.5F;
    public float speedY = 1.5F;

    public float diag1 = 5;
    public float diag2 = 5;
    public float diag3 = 5;
    public float diag4 = 5;
    public float diag5 = 5;
    public float diag6 = 5;
    public float diag7 = 5;
    public float diag8 = 5;
    public float diag9 = 5;
    public float diag10 = 5;

    public float speedD_1 = 1.5F;
    public float speedD_2 = 1.5F;
    public float speedD_3 = 1.5F;
    public float speedD_4 = 1.5F;
    public float speedD_5 = 1.5F;
    public float speedD_6 = 1.5F;
    public float speedD_7 = 1.5F;
    public float speedD_8 = 1.5F;
    public float speedD_9 = 1.5F;

    public float speedPitch = 1.5F;
    public float speedPitchY = 1.5F;

    public float speedNXZ = 1.5F;
    public float speedNY = 1.5F;

    public SuperFirework() {
        addSettings(mode,speed,nearBoost);
    }

    @Override
    public void onEvent(Event event) {
        speedXZ = 1.61F;
        speedY = 1.61F;

        diag1 = 4;
        diag2 = 8;
        diag3 = 12;
        diag4 = 16;
        diag5 = 20;
        diag6 = 24;
        diag7 = 28;
        diag8 = 32;
        diag9 = 36;
        diag10 = 40;

        speedPitch = 2.5F;
        speedPitchY = 2.5F;

        speedD_1 = 2.2F;
        speedD_2 = 2.06F;
        speedD_3 = 1.98F;
        speedD_4 = 1.87F;
        speedD_5 = 1.8F;
        speedD_6 = 1.74F;
        speedD_7 = 1.7F;
        speedD_8 = 1.65F;
        speedD_9 = 1.63F;

        speedNXZ = 1.66F;
        speedNY = 1.66F;
    }
}