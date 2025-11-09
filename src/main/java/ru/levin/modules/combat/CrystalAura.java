package ru.levin.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import org.joml.Vector2f;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.input.EventKeyBoard;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.*;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.move.MoveUtil;
import ru.levin.util.player.InventoryUtil;
import ru.levin.util.player.TimerUtil;
import ru.levin.util.render.RenderUtil;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("All")
@FunctionAnnotation(
        name = "CrystalAura",
        type = Type.Combat,
        desc = "Умное автоустановление и взрыв кристаллов (test)"
)
public class CrystalAura extends Function {

    private final MultiSetting options = new MultiSetting(
            "Настройки",
            List.of("Не взрывать себя", "Коррекция движения", "Игрок в падении", "Подсветка блока"),
            new String[]{"Не взрывать себя", "Коррекция движения", "Игрок в падении", "Подсветка блока"}
    );

    private final ModeSetting distanceMode = new ModeSetting("Тип радиуса", "Свой", "Свой", "Grim");
    private final SliderSetting customDistance = new SliderSetting("Радиус", 5, 2.5f, 6, 0.05f, () -> distanceMode.is("Свой"));
    private final SliderSetting breakDelay = new SliderSetting("Задержка", 100, 0, 500, 1);
    public final BooleanSetting offHandCrystal = new BooleanSetting("Брать крист. в левую руку", true);
    private final BooleanSetting renderBlock = new BooleanSetting("Подсветка блока", true);
    private final BooleanSetting rgCheck = new BooleanSetting("Проверка региона", true, "для гриферских серверов");
    private final BooleanSetting twoPlace = new BooleanSetting("Ставить в несколько мест", true);

    private BlockPos closestObsidian = null;
    public EndCrystalEntity closestCrystal = null;
    private Vec3d obsidianVec = null;

    private final TimerUtil attackTimer = new TimerUtil();
    private final TimerUtil placeTimer = new TimerUtil();
    public final Vector2f rotate = new Vector2f(0f, 0f);
    private int originalSlot = -1;

    private boolean regionBlocked = false;
    private Vec3d regionBlockPos = null;
    private static final double REGION_BLOCK_RADIUS = 12.0;
    private static final double MIN_DISTANCE_BETWEEN_CRYSTALS = 2.5;

    private final List<BlockPos> multiPlaceTargets = new ArrayList<>();
    private Item originalOffhandItem = null;

    public CrystalAura() {
        addSettings(distanceMode, options, customDistance, breakDelay, offHandCrystal, renderBlock, rgCheck, twoPlace);
    }

    private double getEffectiveDistance() {
        return distanceMode.is("Grim") ? 3.6 : customDistance.get().intValue();
    }

    public boolean check() {
        return (closestObsidian != null || closestCrystal != null) && rotate != null && options.get("Коррекция движения");
    }

    @Override
    public void onDisable() {
        reset();
        super.onDisable();
    }

    @Override
    public void onEvent(Event event) {
        if (rgCheck.get() && event instanceof EventPacket eventPacket) {
            if (eventPacket.getPacket() instanceof GameMessageS2CPacket packet) {
                String message = packet.content().getString();
                if (message.contains("can't place that block here")) {
                    regionBlocked = true;
                    regionBlockPos = mc.player.getPos();
                }
            }
        }

        if (event instanceof EventRender3D) {
            if (renderBlock.get() && obsidianVec != null) {
                BlockPos pos = BlockPos.ofFloored(obsidianVec);
                RenderUtil.render3D.drawHoleOutline(new Box(pos), ColorUtil.getColorStyle(360), 2);

                if (!multiPlaceTargets.isEmpty()) {
                    for (BlockPos p : multiPlaceTargets) {
                        RenderUtil.render3D.drawHoleOutline(new Box(p), ColorUtil.getColorStyle(220), 1);
                    }
                }
            }
        }

        if (event instanceof EventKeyBoard input && check()) {
            MoveUtil.fixMovement(input, rotate.x);
        }

        if (event instanceof EventMotion motion) {
            if (offHandCrystal.get()) {
                float currentHp = mc.player.getHealth();
                float minHp = Manager.FUNCTION_MANAGER.autoTotem.hp.get().floatValue();
                if (currentHp > minHp) {
                    InventoryUtil.moveToOffhand(Items.END_CRYSTAL);
                }
            }
            handleCrystalLogic(motion);
        }
    }

    private void handleCrystalLogic(EventMotion motion) {
        if (regionBlocked && regionBlockPos != null) {
            if (mc.player.getPos().distanceTo(regionBlockPos) < REGION_BLOCK_RADIUS) return;
            regionBlocked = false;
            regionBlockPos = null;
        }
        double maxDist = getEffectiveDistance();
        closestCrystal = findNearestCrystal(maxDist);
        if (closestCrystal != null) {
            breakCrystal(closestCrystal, motion);
            return;
        }
        if (originalSlot == -1) originalSlot = mc.player.getInventory().selectedSlot;
        int crystalSlot = InventoryUtil.getItem(Items.END_CRYSTAL.getClass(), true);
        if (crystalSlot == -1 && !offHandCrystal.get()) {
            restoreOriginalSlot();
            return;
        }
        closestObsidian = null;
        double bestDamage = 0.0;
        for (Entity e : Manager.SYNC_MANAGER.getEntities()) {
            if (!(e instanceof LivingEntity target) || e == mc.player || !e.isAlive()) continue;
            BlockPos pos = findBestCrystalPosition(target, maxDist);
            if (pos == null) continue;
            double damage = calculateCrystalDamage(pos, e);
            if (options.get("Не взрывать себя") && calculateCrystalDamage(pos, mc.player) > 6.0) continue;
            if (damage > bestDamage) {
                bestDamage = damage;
                closestObsidian = pos;
            }
        }
        multiPlaceTargets.clear();
        if (closestObsidian != null) {
            obsidianVec = Vec3d.ofCenter(closestObsidian);
            aimAt(obsidianVec, motion);
            multiPlaceTargets.add(closestObsidian);
            if (twoPlace.get()) {
                List<BlockPos> candidates = new ArrayList<>();
                BlockPos[] nearby = new BlockPos[]{closestObsidian.north(), closestObsidian.south(), closestObsidian.east(), closestObsidian.west(), closestObsidian.north().east(), closestObsidian.north().west(), closestObsidian.south().east(), closestObsidian.south().west()};
                for (BlockPos pos : nearby) {
                    if (!canPlaceCrystal(pos, maxDist)) continue;
                    boolean tooClose = false;
                    for (BlockPos existing : multiPlaceTargets) {
                        if (existing.getSquaredDistance(pos) < MIN_DISTANCE_BETWEEN_CRYSTALS * MIN_DISTANCE_BETWEEN_CRYSTALS) {
                            tooClose = true;
                            break;
                        }
                    }
                    if (!tooClose) candidates.add(pos);
                }
                Collections.shuffle(candidates);
                for (BlockPos pos : candidates) {
                    if (multiPlaceTargets.size() >= 3) break;
                    multiPlaceTargets.add(pos);
                }
            }
        }
        if (!multiPlaceTargets.isEmpty() && placeTimer.hasTimeElapsed((long) breakDelay.get().doubleValue())) {
            if (!offHandCrystal.get()) mc.player.getInventory().selectedSlot = crystalSlot;
            for (BlockPos pos : multiPlaceTargets) {
                tryPlaceCrystal(pos, motion);
            }
            multiPlaceTargets.clear();
            placeTimer.reset();
        }
    }
    private void breakCrystal(Entity crystal, EventMotion motion) {
        if (crystal == null) return;
        aimAt(crystal.getPos().add(0, 0.5, 0), motion);

        if (attackTimer.hasTimeElapsed((long) breakDelay.get().doubleValue())) {
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(PlayerInteractEntityC2SPacket.attack(crystal, mc.player.isSneaking()));
            attackTimer.reset();
        }
    }

    private BlockPos findBestCrystalPosition(LivingEntity target, double maxDist) {
        BlockPos base = target.getBlockPos();
        BlockPos[] positions = new BlockPos[]{
                base.north(), base.south(), base.east(), base.west(),
                base.north().east(), base.north().west(), base.south().east(), base.south().west(),
                base.up(), base.up().north(), base.up().south(), base.up().east(), base.up().west(),
                base.up().north().east(), base.up().north().west(), base.up().south().east(), base.up().south().west()
        };

        BlockPos bestPos = null;
        double bestDamage = 0.0;

        for (BlockPos airPos : positions) {
            BlockPos baseBlock = airPos.down();
            if (baseBlock.equals(mc.player.getBlockPos().down())) continue;
            if (!canPlaceCrystal(baseBlock, maxDist)) continue;

            double damage = calculateCrystalDamage(baseBlock, target);
            double selfDamage = calculateCrystalDamage(baseBlock, mc.player);
            if (options.get("Не взрывать себя") && selfDamage > 6.0) continue;

            if (damage > bestDamage) {
                bestDamage = damage;
                bestPos = baseBlock;
            }
        }
        return bestPos;
    }

    private boolean canPlaceCrystal(BlockPos baseBlock, double maxDist) {
        if (mc.world == null || mc.player == null) return false;
        if (!(mc.world.getBlockState(baseBlock).getBlock() == Blocks.OBSIDIAN || mc.world.getBlockState(baseBlock).getBlock() == Blocks.BEDROCK))
            return false;

        BlockPos air1 = baseBlock.up();
        BlockPos air2 = baseBlock.up(2);
        if (!mc.world.getBlockState(air1).isAir() || !mc.world.getBlockState(air2).isAir()) return false;
        if (mc.player.getPos().distanceTo(Vec3d.ofCenter(baseBlock)) > maxDist) return false;
        Box placeBox = new Box(air1.getX(), air1.getY(), air1.getZ(), air1.getX() + 1, air1.getY() + 1, air1.getZ() + 1);
        for (Entity e : mc.world.getOtherEntities(null, placeBox)) {
            if (e instanceof EndCrystalEntity) return false;
        }

        return true;
    }

    private void tryPlaceCrystal(BlockPos pos, EventMotion motion) {
        if (pos == null || mc.player == null || mc.interactionManager == null) return;

        BlockHitResult hitResult = new BlockHitResult(Vec3d.of(pos).add(0.5, 1, 0.5), net.minecraft.util.math.Direction.UP, pos, false);
        mc.interactionManager.interactBlock(mc.player, offHandCrystal.get() ? Hand.OFF_HAND : Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(offHandCrystal.get() ? Hand.OFF_HAND : Hand.MAIN_HAND);

        EndCrystalEntity newCrystal = null;
        for (Entity e : Manager.SYNC_MANAGER.getEntities()) {
            if (e instanceof EndCrystalEntity && e.squaredDistanceTo(Vec3d.ofCenter(pos.up())) < 3.2) {
                newCrystal = (EndCrystalEntity) e;
                break;
            }
        }
        if (newCrystal != null) breakCrystal(newCrystal, motion);
    }

    private EndCrystalEntity findNearestCrystal(double maxDist) {
        EndCrystalEntity closest = null;
        double minDist = Double.MAX_VALUE;
        Vec3d eyePos = mc.player.getEyePos();

        for (Entity entity : Manager.SYNC_MANAGER.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (!crystal.isAlive()) continue;

            double dist = mc.player.squaredDistanceTo(crystal);
            if (dist <= maxDist * maxDist && dist < minDist) {
                closest = crystal;
                minDist = dist;
            }
        }
        return closest;
    }
    private double calculateCrystalDamage(BlockPos pos, Entity target) {
        if (target == null || mc.world == null) return 0.0;
        double distance = target.squaredDistanceTo(Vec3d.ofCenter(pos));
        if (distance > 12.0) return 0.0;
        double exposure = 1.0 - (distance / 12.0);
        double damage = exposure * 12.0;
        return Math.max(0, damage);
    }

    private void aimAt(Vec3d vec, EventMotion motion) {
        float[] rot = rotations(vec);
        if (motion != null) {
            motion.setYaw(rot[0]);
            motion.setPitch(rot[1]);
        }
        rotate.set(rot[0], rot[1]);
    }

    private void restoreOriginalSlot() {
        if (originalSlot >= 0 && originalSlot <= 8) {
            mc.player.getInventory().selectedSlot = originalSlot;
            originalSlot = -1;
        }
    }

    public void reset() {
        restoreOriginalSlot();
        closestObsidian = null;
        closestCrystal = null;
        obsidianVec = null;
        multiPlaceTargets.clear();
        attackTimer.reset();
        placeTimer.reset();
        regionBlocked = false;
        regionBlockPos = null;
    }

    public static float[] rotations(Vec3d vec) {
        double dx = vec.x - mc.player.getX();
        double dy = vec.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = vec.z - mc.player.getZ();
        double dist = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, dist));
        return new float[]{yaw, pitch};
    }
}
