package ru.levin.modules.combat;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import org.joml.Vector2f;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@FunctionAnnotation(name = "SelfTrap", keywords = "Trap", type = Type.Combat, desc = "Застраивает ноги любыми блоками")
public class SelfTrap extends Function {

    public final Vector2f rotate = new Vector2f(0f, 0f);

    private final BlockPos[] baseOffsets = new BlockPos[]{
            BlockPos.ORIGIN.north(),
            BlockPos.ORIGIN.south(),
            BlockPos.ORIGIN.east(),
            BlockPos.ORIGIN.west()
    };

    private List<BlockPos> targets = new ArrayList<>();
    private int index = 0;

    private int originalSlot = -1;
    public boolean active = false;

    private int tickDelay = 0;
    private final Random rnd = new Random();

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        targets.clear();
        BlockPos base = mc.player.getBlockPos();
        for (BlockPos off : baseOffsets) {
            targets.add(base.add(off));
        }

        originalSlot = mc.player.getInventory().selectedSlot;
        index = 0;
        active = true;
        tickDelay = 0;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && originalSlot != -1)
            mc.player.getInventory().selectedSlot = originalSlot;
        targets.clear();
        index = 0;
        active = false;
        tickDelay = 0;
    }

    @Override
    public void onEvent(Event event) {
        if (!active) return;
        if (!(event instanceof EventUpdate || event instanceof EventMotion)) return;

        if (event instanceof EventUpdate) {
            if (tickDelay > 0) {
                tickDelay--;
                return;
            }

            if (index >= targets.size()) {
                finish();
                return;
            }

            BlockPos target = targets.get(index);
            if (!mc.world.getBlockState(target).isAir() || isEntityBlocking(target)) {
                index++;
                return;
            }

            Direction placeSide = findPlaceableSide(target);
            if (placeSide != null) {
                BlockPos neighbor = target.offset(placeSide);
                Vec3d hitVec = Vec3d.ofCenter(neighbor).add(Vec3d.of(placeSide.getVector()).multiply(0.5));
                int blockSlot = findAnyBlockInHotbar();
                if (blockSlot == -1) {
                    finish();
                    return;
                }

                mc.player.getInventory().selectedSlot = blockSlot;

                BlockHitResult hit = new BlockHitResult(hitVec, placeSide.getOpposite(), neighbor, false);
                rotateTo(hit.getBlockPos());
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                mc.player.swingHand(Hand.MAIN_HAND);

                tickDelay = 1 + rnd.nextInt(3);
                index++;
                return;
            }

            index++;
        }

        if (event instanceof EventMotion motion) {
            motion.setYaw(rotate.x);
            motion.setPitch(rotate.y);
        }
    }

    private void finish() {
        active = false;
        if (mc.player != null && originalSlot != -1)
            mc.player.getInventory().selectedSlot = originalSlot;
    }

    private int findAnyBlockInHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() instanceof BlockItem) {
                Block block = ((BlockItem) stack.getItem()).getBlock();
                if (block != Blocks.AIR) return i;
            }
        }
        return -1;
    }

    private boolean isEntityBlocking(BlockPos pos) {
        Box box = new Box(pos);
        for (Entity e : mc.world.getOtherEntities(null, box)) {
            if (e != mc.player) return true;
        }
        return false;
    }

    private Direction findPlaceableSide(BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            if (!mc.world.getBlockState(neighbor).isAir()
                    && mc.world.getBlockState(neighbor).isSolidBlock(mc.world, neighbor)) {
                return dir;
            }
        }
        return null;
    }

    private void rotateTo(BlockPos pos) {
        if (mc.player == null) return;
        double dx = pos.getX() + 0.5 - mc.player.getX();
        double dy = pos.getY() + 0.5 - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = pos.getZ() + 0.5 - mc.player.getZ();

        double distXZ = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));

        rotate.x = yaw;
        rotate.y = pitch;
    }
}
