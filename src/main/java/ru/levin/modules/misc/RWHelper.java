package ru.levin.modules.misc;

import ru.levin.events.Event;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BindBooleanSetting;
import ru.levin.util.move.MoveUtil;

@SuppressWarnings("All")
@FunctionAnnotation(name = "RWHelper", desc = "", type = Type.Misc)
public class RWHelper extends Function {

    private final BindBooleanSetting dragonFly = new BindBooleanSetting("DragonFly", "Позволяет летать быстрее на драгоне", true);

    public RWHelper() {
        addSettings(dragonFly);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventMotion)) return;
        if (!dragonFly.get() || !mc.player.getAbilities().flying) return;
        MoveUtil.setSpeed(1);

        float y = 0;

        boolean noForward = mc.player.forwardSpeed == 0 && !mc.options.leftKey.isPressed() && !mc.options.rightKey.isPressed();

        if (mc.options.jumpKey.isPressed()) y = noForward ? 0.5F : 0.25F;
        else if (mc.options.sneakKey.isPressed()) y = noForward ? -0.5F : -0.25F;

        mc.player.setVelocity(mc.player.getVelocity().x, y, mc.player.getVelocity().z);
    }
}
