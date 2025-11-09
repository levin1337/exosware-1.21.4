package ru.levin.modules.player;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;
import ru.levin.events.impl.input.EventKeyBoard;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.events.impl.EventPacket;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.math.MathUtil;
import ru.levin.util.move.MoveUtil;

@FunctionAnnotation(name = "FreeCamera",desc = "Свободная камера", type = Type.Player)
public class FreeCamera extends Function {
    private final SliderSetting speed = new SliderSetting("X - Скорость", 1f, 0.1f, 3f,0.1f);
    private final SliderSetting yspeed = new SliderSetting("Y - Скорость", 0.42f, 0.1f, 3f,0.1f);

    private float fakeYaw, fakePitch, prevFakeYaw, prevFakePitch;
    private double fakeX, fakeY, fakeZ, prevFakeX, prevFakeY, prevFakeZ;
    public LivingEntity trackEntity;
    private Vec3d freezePosition = Vec3d.ZERO;

    public FreeCamera() {
        addSettings(speed,yspeed);
    }
    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) {
            toggle();
        }
        if (event instanceof EventPacket eventPacket) {
            if (eventPacket.getPacket() instanceof PlayerMoveC2SPacket) {
                eventPacket.setCancel(true);
            }
        }
        if (event instanceof EventKeyBoard) {
            if (mc.player == null) return;

            if (trackEntity == null) {

                double[] motion = MoveUtil.forward(speed.get().floatValue());

                prevFakeX = fakeX;
                prevFakeY = fakeY;
                prevFakeZ = fakeZ;

                fakeX += motion[0];
                fakeZ += motion[1];

                if (mc.options.jumpKey.isPressed())
                    fakeY += yspeed.get().floatValue();

                if (mc.options.sneakKey.isPressed())
                    fakeY -= yspeed.get().floatValue();
            }

            mc.player.input.movementForward = 0;
            mc.player.input.movementSideways = 0;
        }
        if (event instanceof EventMotion eventMotion) {
            if (mc.player != null && freezePosition != Vec3d.ZERO) {
                eventMotion.setCancel(true);
                mc.player.setPosition(freezePosition);
                mc.player.setVelocity(Vec3d.ZERO);
            }

            prevFakeYaw = fakeYaw;
            prevFakePitch = fakePitch;

            if (trackEntity != null) {
                fakeYaw = trackEntity.getYaw();
                fakePitch = trackEntity.getPitch();

                prevFakeX = fakeX;
                prevFakeY = fakeY;
                prevFakeZ = fakeZ;

                fakeX = trackEntity.getX();
                fakeY = trackEntity.getY() + trackEntity.getEyeHeight(trackEntity.getPose());
                fakeZ = trackEntity.getZ();
            } else {
                fakeYaw = mc.player.getYaw();
                fakePitch = mc.player.getPitch();
            }
        }
        if (event instanceof EventMotion eventMove) {
            eventMove.setX(0.);
            eventMove.setY(0.);
            eventMove.setZ(0.);
            eventMove.setCancel(true);
        }
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            freezePosition = mc.player.getPos();
        }

        mc.chunkCullingEnabled = false;
        trackEntity = null;

        fakePitch = mc.player.getPitch();
        fakeYaw = mc.player.getYaw();

        prevFakePitch = fakePitch;
        prevFakeYaw = fakeYaw;

        fakeX = mc.player.getX();
        fakeY = mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose());
        fakeZ = mc.player.getZ();

        prevFakeX = mc.player.getX();
        prevFakeY = mc.player.getY();
        prevFakeZ = mc.player.getZ();
    }
    @Override
    public void onDisable() {
        mc.chunkCullingEnabled = true;
    }

    public float getFakeYaw() {
        return (float) interpolate(prevFakeYaw, fakeYaw, mc.getRenderTickCounter().getTickDelta(true));
    }

    public float getFakePitch() {
        return (float) interpolate(prevFakePitch, fakePitch, mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFakeX() {
        return MathUtil.interpolate(prevFakeX, fakeX, mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFakeY() {
        return MathUtil.interpolate(prevFakeY, fakeY, mc.getRenderTickCounter().getTickDelta(true));
    }

    public double getFakeZ() {
        return MathUtil.interpolate(prevFakeZ, fakeZ, mc.getRenderTickCounter().getTickDelta(true));
    }
    public static double interpolate(double oldValue, double newValue, double interpolationValue) {
        return (oldValue + (newValue - oldValue) * interpolationValue);
    }
}