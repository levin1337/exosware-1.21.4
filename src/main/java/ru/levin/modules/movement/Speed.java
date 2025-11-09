package ru.levin.modules.movement;

import ru.levin.modules.combat.TargetStrafe;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.move.MoveUtil;
import ru.levin.util.player.TimerUtil;

@FunctionAnnotation(name = "Speed", desc = "Поможет умереть быстрее", type = Type.Move)
public class Speed extends Function {

    private final ModeSetting mode = new ModeSetting("Режим", "Vanilla", "Vanilla");

    private final SliderSetting speed = new SliderSetting("Скорость",1f,0.1f,3f,0.1f);
    private final TimerUtil timerUtil = new TimerUtil();

    public Speed() {
        addSettings(mode,speed);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventMotion eventMotion) {
            if (mc.player == null || mc.world == null) return;

            switch (mode.get()) {
                case "Vanilla" -> vanilla();
            }
        }
    }

    private void vanilla() {
        if (MoveUtil.isMoving() && !mc.player.isGliding()) {
            MoveUtil.setSpeed(speed.get().floatValue());
        }
    }


    @Override
    protected void onEnable() {
        TargetStrafe targetStrafe = Manager.FUNCTION_MANAGER.targetStrafe;
        if (targetStrafe.state) {
            targetStrafe.setState(false);
        }
        timerUtil.reset();
        super.onEnable();
    }

    @Override
    public void onDisable() {
        ClientManager.TICK_TIMER = 1.0f;
        super.onDisable();
    }
}
