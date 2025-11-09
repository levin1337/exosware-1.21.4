package ru.levin.modules.movement;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import ru.levin.events.Event;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.util.player.TimerUtil;

@SuppressWarnings("all")
@FunctionAnnotation(name = "Spider", desc = "Автоматически ставит воду и карабкается после прыжка", type = ru.levin.modules.Type.Move)
public class Spider extends Function {
    public final ModeSetting mode = new ModeSetting("Тип", "RwWater", "RwWater", "Matrix");
    private final TimerUtil timerUtil = new TimerUtil();

    private boolean climbing = false;
    private boolean switched = false;
    private int waterSlot = -1;
    private int originalSlot = -1;
    private int movedFromInvSlot = -1;

    public Spider() {
        addSettings(mode);
    }

    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) return;

        if (event instanceof EventPacket eventPacket) {
            if (eventPacket.getPacket() instanceof GameMessageS2CPacket packet) {
                String message = packet.content().getString();
                if (message.contains("Извините, но вы не можете поставить блок здесь.")) {
                    eventPacket.setCancel(true);
                }
            }
        }

        if (event instanceof EventUpdate) {
            if (mode.is("RwWater")) {
                handleRwWater();
            } else if (mode.is("Matrix")) {
                handleMatrix();
            }
        }

        if (event instanceof EventMotion) {
            if (climbing && mode.is("RwWater")) {
                Vec3d eyePos = mc.player.getCameraPosVec(1.0F);
                Vec3d lookVec = mc.player.getRotationVec(1.0F).normalize();
                Vec3d targetVec = eyePos.add(lookVec);

                BlockPos targetBlock = new BlockPos((int) targetVec.x, (int) targetVec.y - 1, (int) targetVec.z);

                float neededYaw = getNeededYaw(targetBlock.toCenterPos());
                float neededPitch = getNeededPitch(targetBlock.toCenterPos());

                mc.player.setPitch(neededPitch);
                mc.player.setYaw(neededYaw);
            }
        }
    }

    private void handleRwWater() {
        if (mc.options.jumpKey.isPressed() && mc.player.horizontalCollision && !mc.player.isOnGround()) {
            if (!climbing) {
                climbing = true;
                switched = false;
                originalSlot = mc.player.getInventory().selectedSlot;
                waterSlot = ensureWaterBucketInHotbar();
            }
        }

        if (climbing) {
            if (waterSlot == -1) {
                stopClimbing();
                return;
            }

            if (!mc.player.horizontalCollision || mc.player.isOnGround()) {
                stopClimbing();
                return;
            }

            Vec3d eyePos = mc.player.getCameraPosVec(1.0F);
            Vec3d lookVec = mc.player.getRotationVec(1.0F).normalize();
            Vec3d targetVec = eyePos.add(lookVec);

            BlockPos targetBlock = new BlockPos((int) targetVec.x, (int) targetVec.y - 1, (int) targetVec.z);

            float neededYaw = getNeededYaw(targetBlock.toCenterPos());
            float neededPitch = getNeededPitch(targetBlock.toCenterPos());

            if (!switched) {
                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(waterSlot));
                switched = true;
            }

            if (timerUtil.hasTimeElapsed(20, true)) {
                mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, 0, neededYaw, neededPitch));
            }

            mc.player.fallDistance = 0;
            mc.player.setVelocity(0, 0.3, 0);
        }
    }

    private void handleMatrix() {
        if (mc.options.jumpKey.isPressed() && mc.player.horizontalCollision) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
            mc.player.fallDistance = 0;
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stopClimbing();
        if (mc.player != null) {
            mc.player.setVelocity(0, 0, 0);
        }
        timerUtil.reset();
    }

    private void stopClimbing() {
        if (!climbing) return;
        climbing = false;
        switched = false;

        if (originalSlot != -1 && mc.player != null) {
            mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(originalSlot));
            originalSlot = -1;
        }
        if (waterSlot != -1 && movedFromInvSlot != -1 && mc.player != null) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, waterSlot, movedFromInvSlot,
                    net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
        }

        waterSlot = -1;
        movedFromInvSlot = -1;
    }

    private int ensureWaterBucketInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.WATER_BUCKET) {
                movedFromInvSlot = -1;
                return i;
            }
        }

        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.WATER_BUCKET) {
                int freeHotbar = findFreeHotbarSlot();
                if (freeHotbar == -1) freeHotbar = 0;
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, freeHotbar,
                        net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
                movedFromInvSlot = i;
                return freeHotbar;
            }
        }
        return -1;
    }

    private int findFreeHotbarSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    private float getNeededYaw(Vec3d target) {
        double dx = target.x - mc.player.getX();
        double dz = target.z - mc.player.getZ();
        return (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90F);
    }

    private float getNeededPitch(Vec3d target) {
        double dx = target.x - mc.player.getX();
        double dy = target.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = target.z - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        return (float) -Math.toDegrees(Math.atan2(dy, dist));
    }
}
