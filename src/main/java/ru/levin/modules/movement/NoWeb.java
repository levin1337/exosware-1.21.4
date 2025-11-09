package ru.levin.modules.movement;

import net.minecraft.util.math.Vec3d;

import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.move.MoveUtil;

@SuppressWarnings("All")
@FunctionAnnotation(name = "NoWeb", type = Type.Move)
public class NoWeb extends Function {
    private final ModeSetting mode = new ModeSetting("Режим", "Custom", "Custom", "ReallyWorld");
    private final SliderSetting speedXZ = new SliderSetting("Скорость по X и Z", 0.1F, 0.1F, 1, 0.1F,() -> mode.is("Custom"));
    private final SliderSetting speedY = new SliderSetting("Скорость по Y", 0.1F, 0.1F, 4, 0.1F,() -> mode.is("Custom"));

    public NoWeb() {
        addSettings(mode, speedXZ, speedY);
    }


    @Override
    public void onEvent(Event event) {
        if (mode.getIndex() == 0) {
            custom();
        }

        if (mode.getIndex() == 1) {
            reallyWorld();
        }
    }

    private void custom() {
        if (MoveUtil.isInWeb()) {
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(velocity.x, 0, velocity.z);

            if (mc.options.jumpKey.isPressed()) {
                mc.player.setVelocity(velocity.x, speedY.get().floatValue(), velocity.z);
            }

            if (mc.options.sneakKey.isPressed()) {
                mc.player.setVelocity(velocity.x, -speedY.get().floatValue(), velocity.z);
            }

            MoveUtil.setSpeed(speedXZ.get().floatValue());
        }
    }

    private void reallyWorld() {
        if (MoveUtil.isInWeb()) {
            Vec3d velocity = mc.player.getVelocity();
            mc.player.setVelocity(velocity.x, 0, velocity.z);

            if (mc.options.jumpKey.isPressed()) {
                mc.player.setVelocity(velocity.x, 0.9, velocity.z);
            }

            if (mc.options.sneakKey.isPressed()) {
                mc.player.setVelocity(velocity.x, -0.9, velocity.z);
            }

            MoveUtil.setSpeed(0.21F);
        }
    }
}