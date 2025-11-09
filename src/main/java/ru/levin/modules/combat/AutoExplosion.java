package ru.levin.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import org.joml.Vector2f;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.input.EventKey;
import ru.levin.events.impl.input.EventKeyBoard;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.events.impl.world.EventObsidianPlace;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BindSetting;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.move.MoveUtil;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.*;

@FunctionAnnotation(name = "AutoExplosion", type = Type.Combat, desc = "Автоматически ставит кристал, при ставке обсидиана")
public class AutoExplosion extends Function {

    private final BooleanSetting correction = new BooleanSetting("Коррекция движения", true);
    private final SliderSetting delay = new SliderSetting("Задержка", 100f, 50f, 300f, 1f);

    private final BooleanSetting sanya = new BooleanSetting("Ставить по бинду", false);
    private final BindSetting bind = new BindSetting("Кнопка", 0, () -> sanya.get());

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private BlockPos crystalPos = null;
    private Entity crystalEntity = null;
    private int previousSlot = -1;

    public Vector2f serverRot = null;
    private boolean rotating = false;

    public AutoExplosion() {
        addSettings(correction, delay, sanya, bind);
    }
    public boolean check() {
        return correction.get() &&
                crystalEntity != null &&
                crystalPos != null &&
                serverRot != null &&
                state;
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventKey key && sanya.get()) {
            if (key.key == bind.getKey()) {
                schedulePlaceSequence();
            }
        }
        if (event instanceof EventKeyBoard input && shouldCorrect()) {
            MoveUtil.fixMovement(input, serverRot.x);
        }

        if (event instanceof EventMotion motion) {
            handleRotation(motion);
        }

        if (event instanceof EventObsidianPlace place) {
            scheduleCrystalPlace(place.getPos());
        }

        if (event instanceof EventUpdate) {
            updateLogic();
        }
    }

    private void schedulePlaceSequence() {
        BlockPos pos = getLookingBlockPos();
        if (pos == null) return;

        int obsidianSlot = findObsidianSlot();
        if (obsidianSlot == -1) return;

        previousSlot = mc.player.getInventory().selectedSlot;
        mc.player.getInventory().selectedSlot = obsidianSlot;

        scheduler.schedule(() -> mc.execute(() -> {
            placeBlock(pos);
            mc.player.getInventory().selectedSlot = previousSlot;
            scheduleCrystalPlace(pos);

        }), 50, TimeUnit.MILLISECONDS);
    }

    private void placeBlock(BlockPos pos) {
        BlockHitResult bhr = new BlockHitResult(
                Vec3d.ofCenter(pos),
                Direction.UP,
                pos,
                false
        );

        ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        if (result == ActionResult.SUCCESS) mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void scheduleCrystalPlace(BlockPos pos) {
        int crystalSlot = findCrystalSlot();
        if (crystalSlot == -1 || !canPlaceCrystal(pos)) return;

        scheduler.schedule(() -> mc.execute(() -> {
            previousSlot = mc.player.getInventory().selectedSlot;
            mc.player.getInventory().selectedSlot = crystalSlot;

            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(pos),
                    Direction.UP,
                    pos,
                    false
            );

            ActionResult result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            if (result == ActionResult.SUCCESS) {
                mc.player.swingHand(Hand.MAIN_HAND);
                crystalPos = pos;
            }

            mc.player.getInventory().selectedSlot = previousSlot;

        }), (long) delay.get().longValue(), TimeUnit.MILLISECONDS);
    }

    private boolean canPlaceCrystal(BlockPos pos) {
        BlockPos above = pos.up();
        return mc.world.isAir(above);
    }
    private void updateLogic() {
        if (crystalPos == null) return;

        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(crystalPos)) > 6.0) {
            reset();
            return;
        }

        List<Entity> crystals = mc.world.getOtherEntities(
                        null,
                        new Box(crystalPos).expand(1.0))
                .stream()
                .filter(e -> e instanceof EndCrystalEntity)
                .toList();

        if (!crystals.isEmpty()) {
            crystalEntity = crystals.get(0);
            tryAttack(crystalEntity);
        }
    }

    private void handleRotation(EventMotion event) {
        if (crystalEntity == null) return;

        Vector2f targetRot = rotationToEntity(crystalEntity);
        if (serverRot == null) serverRot = targetRot;

        serverRot.x += clampRotation(targetRot.x - serverRot.x, 10);
        serverRot.y += clampRotation(targetRot.y - serverRot.y, 10);

        event.setYaw(serverRot.x);
        event.setPitch(serverRot.y);
    }

    private float clampRotation(float value, float maxStep) {
        if (value > maxStep) return maxStep;
        if (value < -maxStep) return -maxStep;
        return value;
    }

    private void tryAttack(Entity entity) {
        if (entity == null || mc.player.getAttackCooldownProgress(0) < 1) return;

        mc.interactionManager.attackEntity(mc.player, entity);
        mc.player.swingHand(Hand.MAIN_HAND);
        reset();
    }

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++)
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) return i;
        return -1;
    }

    private int findObsidianSlot() {
        for (int i = 0; i < 9; i++)
            if (mc.player.getInventory().getStack(i).isOf(Items.OBSIDIAN)) return i;
        return -1;
    }

    private BlockPos getLookingBlockPos() {
        Vec3d eyes = mc.player.getCameraPosVec(1f);
        Vec3d look = mc.player.getRotationVec(1f).multiply(4);

        BlockHitResult bhr = mc.world.raycast(new net.minecraft.world.RaycastContext(
                eyes,
                eyes.add(look),
                net.minecraft.world.RaycastContext.ShapeType.OUTLINE,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                mc.player
        ));

        if (bhr == null || bhr.getBlockPos() == null) return null;
        return bhr.getBlockPos();
    }

    private boolean shouldCorrect() {
        return correction.get() && crystalEntity != null && crystalPos != null && serverRot != null && state;
    }

    private void reset() {
        crystalEntity = null;
        crystalPos = null;
        serverRot = null;
        previousSlot = -1;
    }

    public static Vector2f rotationToEntity(Entity entity) {
        Vec3d diff = entity.getPos().subtract(mc.player.getPos());
        double flatDist = Math.hypot(diff.x, diff.z);

        float yaw = (float) Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90f;
        float pitch = (float) -Math.toDegrees(Math.atan2(diff.y, flatDist));

        return new Vector2f(yaw, pitch);
    }

    @Override
    protected void onDisable() {
        reset();
        super.onDisable();
    }
}
