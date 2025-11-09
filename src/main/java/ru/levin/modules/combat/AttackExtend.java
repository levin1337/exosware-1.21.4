package ru.levin.modules.combat;

import ru.levin.events.Event;
import ru.levin.events.impl.input.EventKeyBoard;
import ru.levin.events.impl.player.EventAttack;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.util.move.MoveUtil;

@FunctionAnnotation(name = "WTap", type = Type.Combat, keywords = {"ExtendedAttack","ExtendedKnockBack"}, desc = "Позволяет оттолкнуть противника дальше")
public class AttackExtend extends Function {
    private final BooleanSetting onlyOnGround = new BooleanSetting("Только на земле", true);

    public AttackExtend() {
        addSettings(onlyOnGround);
    }

    private int sprintResetTicks;

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventKeyBoard e && sprintResetTicks > 0 && MoveUtil.isMoving()) {
            e.setMovementForward(0);
            sprintResetTicks--;
        }

        if (event instanceof EventAttack && (!onlyOnGround.get() || mc.player.isOnGround()) && !mc.player.isInFluid() && mc.player.isSprinting()) {
            sprintResetTicks = 1;
        }
    }
}
