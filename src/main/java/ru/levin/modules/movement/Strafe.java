package ru.levin.modules.movement;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.move.MoveUtil;

@FunctionAnnotation(name = "Strafe", desc = "Быстрое перемещение", type = Type.Move)
public class Strafe extends Function {
    private final ModeSetting mode = new ModeSetting("Тип", "MetaHvH", "MetaHvH");

    public Strafe() {
        addSettings(mode);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventMotion && mc.player != null) {
            if (mode.is("MetaHvH")) {
                if (!mc.player.isGliding() && (!mc.player.isTouchingWater() || !mc.player.isSwimming())) {
                    if (MoveUtil.isMoving()) {
                        float motion = 0.19f;
                        StatusEffectInstance speedEffect = mc.player.getStatusEffect(StatusEffects.SPEED);
                        if (speedEffect != null) {
                            int amplifier = speedEffect.getAmplifier();

                            switch (amplifier) {
                                case 0:
                                    motion = 0.25f;
                                    break;
                                case 1:
                                    motion = 0.37f;
                                    break;
                                case 2:
                                    motion = 0.46f;
                                    break;
                                case 3:
                                    motion = 0.7f;
                                    break;
                                default:
                                    motion = 0.75f + (amplifier - 3) * 0.05f;
                                    break;
                            }
                        }


                        if (mc.options.jumpKey.isPressed()) {
                            motion += 0.1f;
                        }

                        MoveUtil.setMotion(motion);
                    }
                }
            }
        }
    }
}
