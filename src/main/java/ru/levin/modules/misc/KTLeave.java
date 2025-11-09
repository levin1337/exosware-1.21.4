package ru.levin.modules.misc;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.VehicleMoveS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.events.impl.player.EventPlayerTravel;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.move.MoveUtil;
import ru.levin.util.move.NetworkUtils;

import java.util.ArrayList;
import java.util.concurrent.CopyOnWriteArrayList;

@FunctionAnnotation(name = "KTLeave", desc = "вы стали калестиум юзером", type = Type.Misc)
public class KTLeave extends Function {

    private final ModeSetting mode = new ModeSetting("Мод", "Motion", "Motion", "Packet");
    private final BooleanSetting phase = new BooleanSetting("Phase", false);
    private final BooleanSetting gravity = new BooleanSetting("Gravity", false);
    private final BooleanSetting automount = new BooleanSetting("AutoMount", false);
    private final BooleanSetting allowShift = new BooleanSetting("AllowShift", false);

    private final SliderSetting speed = new SliderSetting("Speed", 2f, 0.0f, 25f, 0.1f);
    private final SliderSetting yspeed = new SliderSetting("YSpeed", 1f, 0.0f, 10f, 0.1f);

    private final BooleanSetting cancel = new BooleanSetting("Cancel", false);

    private final BooleanSetting stopunloaded = new BooleanSetting("StopUnloaded", false);
    private final BooleanSetting cancelrotations = new BooleanSetting("CancelRotations", false);
    private final BooleanSetting limit = new BooleanSetting("Limit", false);
    private final SliderSetting jitter = new SliderSetting("Jitter", 0.1f, 0.0f, 10f, 0.1f);
    private final BooleanSetting spoofpackets = new BooleanSetting("SpoofPackets", false);
    private final BooleanSetting ongroundpacket = new BooleanSetting("OnGroundPacket", false);
    private final CopyOnWriteArrayList<VehicleMoveC2SPacket> vehiclePackets = new CopyOnWriteArrayList<>();

    private int ticksEnabled = 0;
    private int enableDelay = 0;
    private boolean waitedCooldown = false;
    private boolean returnGravity = false;
    private boolean jitterSwitch = false;

    public KTLeave() {
        addSettings(mode, phase, gravity, automount, allowShift, speed, yspeed,
                cancel, stopunloaded, cancelrotations, limit, jitter, spoofpackets, ongroundpacket);
    }

    private boolean fullNullCheck() {
        return mc.player == null || mc.world == null;
    }

    @Override
    public void onEnable() {
        if (fullNullCheck()) {
            toggle();
            return;
        }

        if (automount.get()) mountToBoat();
    }

    @Override
    public void onDisable() {
        vehiclePackets.clear();
        waitedCooldown = false;

        if (mc.player == null) return;

        if (phase.get() && mode.is("Motion")) {
            if (mc.player.getControllingVehicle() != null)
                mc.player.getControllingVehicle().noClip = false;
            mc.player.noClip = false;
        }
        if (mc.player.getControllingVehicle() != null)
            mc.player.getControllingVehicle().setNoGravity(false);
        mc.player.setNoGravity(false);
    }

    private float randomizeYOffset() {
        jitterSwitch = !jitterSwitch;
        return jitterSwitch ? jitter.get().floatValue() : -jitter.get().floatValue();
    }

    private void sendMovePacket(VehicleMoveC2SPacket pac) {
        vehiclePackets.add(pac);
        send(pac);
    }

    private void teleportToGround(Entity boat) {
        if (boat == null || mc.world == null) return;
        BlockPos blockPos = BlockPos.ofFloored(boat.getPos());
        for (int i = 0; i < 255; ++i) {
            if (!mc.world.getBlockState(blockPos).isReplaceable() || mc.world.getBlockState(blockPos).getBlock() == Blocks.WATER) {
                boat.setPosition(boat.getX(), blockPos.getY() + 1, boat.getZ());
                sendMovePacket(VehicleMoveC2SPacket.fromVehicle(boat));
                boat.setPosition(boat.getX(), boat.getY(), boat.getZ());
                break;
            }
            blockPos = blockPos.down();
        }
    }

    private void mountToBoat() {
        if (mc.player == null || mc.world == null) return;
        for (Entity entity : Manager.SYNC_MANAGER.getEntities()) {
            if (!(entity instanceof BoatEntity) || mc.player.squaredDistanceTo(entity) > 25.0f) continue;
            send(PlayerInteractEntityC2SPacket.interact(entity, false, Hand.MAIN_HAND));
            break;
        }
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventPlayerTravel ev) {
            if (!ev.isPre() || fullNullCheck()) return;
            Entity entity = mc.player.getControllingVehicle();

            if (entity == null) {
                if (automount.get()) mountToBoat();
                return;
            }

            if (phase.get() && mode.is("Motion")) {
                entity.noClip = true;
                entity.setNoGravity(true);
                mc.player.noClip = true;
            }

            if (!returnGravity) {
                entity.setNoGravity(!gravity.get());
                mc.player.setNoGravity(!gravity.get());
            }

            if ((!mc.world.isChunkLoaded((int) entity.getX() >> 4, (int) entity.getZ() >> 4) || entity.getY() < -60) && stopunloaded.get()) {
                returnGravity = true;
                return;
            }

            entity.setYaw(mc.player.getYaw());
            double[] boatMotion = MoveUtil.forward(speed.get().floatValue());
            double predictedX = entity.getX() + boatMotion[0];
            double predictedZ = entity.getZ() + boatMotion[1];
            double predictedY = entity.getY();

            if ((!mc.world.isChunkLoaded((int) predictedX >> 4, (int) predictedZ >> 4) || predictedY < -60) && stopunloaded.get()) {
                returnGravity = true;
                return;
            }

            returnGravity = false;

            if (mode.is("Motion"))
                entity.setVelocity(boatMotion[0], entity.getVelocity().y, boatMotion[1]);

            if (mc.options.jumpKey.isPressed()) {
                if (mode.is("Motion"))
                    entity.setVelocity(entity.getVelocity().x, entity.getVelocity().y + yspeed.get().floatValue(), entity.getVelocity().z);
                else predictedY += yspeed.get().floatValue();
            } else if (mc.options.sneakKey.isPressed()) {
                if (mode.is("Motion"))
                    entity.setVelocity(entity.getVelocity().x, entity.getVelocity().y - yspeed.get().floatValue(), entity.getVelocity().z);
                else predictedY -= yspeed.get().floatValue();
            }

            if (!MoveUtil.isMoving()) entity.setVelocity(0, entity.getVelocity().y, 0);

            if (ongroundpacket.get()) teleportToGround(entity);

            if (mode.is("Packet")) {
                entity.setPosition(predictedX, predictedY, predictedZ);
                sendMovePacket(VehicleMoveC2SPacket.fromVehicle(entity));
            }

            ev.setCancel(true);
            ++ticksEnabled;
        }

        if (event instanceof EventPacket eventPacket) {
            if (eventPacket.isReceivePacket()) {
                if (fullNullCheck()) return;
                if (eventPacket.getPacket() instanceof DisconnectS2CPacket) toggle();

                if (!mc.player.isRiding() || returnGravity || waitedCooldown) return;

                if (cancel.get()) {
                    if (eventPacket.getPacket() instanceof VehicleMoveS2CPacket
                            || eventPacket.getPacket() instanceof EntityS2CPacket
                            || eventPacket.getPacket() instanceof EntityAttachS2CPacket
                        || !(eventPacket.getPacket() instanceof KeepAliveC2SPacket)) {
                        eventPacket.setCancel(true);
                    }
                }
            }
        }

        if (event instanceof EventPacket eventPacket2) {
            if (eventPacket2.isSendPacket()) {
                if (fullNullCheck()) return;

                if ((eventPacket2.getPacket() instanceof PlayerMoveC2SPacket.LookAndOnGround && cancelrotations.get() || eventPacket2.getPacket() instanceof PlayerInputC2SPacket) && mc.player.isRiding())
                    eventPacket2.setCancel(true);

                if (returnGravity && eventPacket2.getPacket() instanceof VehicleMoveC2SPacket)
                    eventPacket2.setCancel(true);

                if (eventPacket2.getPacket() instanceof PlayerInputC2SPacket && allowShift.get())
                    eventPacket2.setCancel(true);

                if (mc.player.getControllingVehicle() == null || returnGravity || waitedCooldown)
                    return;

                Vec3d boatPos = mc.player.getControllingVehicle().getPos();
                if ((!mc.world.isChunkLoaded((int) boatPos.x >> 4, (int) boatPos.z >> 4) || boatPos.y < -60) && stopunloaded.get())
                    return;

                if (eventPacket2.getPacket() instanceof VehicleMoveC2SPacket pac && limit.get() && mode.is("Packet")) {
                    if (vehiclePackets.contains(pac)) vehiclePackets.remove(pac);
                    else eventPacket2.setCancel(true);
                }
            }
        }
    }

    private void send(Packet<?> packet) {
        if (mc.player != null && mc.player.networkHandler != null)
            NetworkUtils.sendSilentPacket(packet);
    }
}
