package ru.levin.modules.movement;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BindBooleanSetting;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.move.NetworkUtils;
import ru.levin.util.player.InventoryUtil;
import ru.levin.util.player.TimerUtil;
import ru.levin.util.render.RenderAddon;
import ru.levin.util.render.RenderUtil;

import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("All")
@FunctionAnnotation(name = "ElytraTarget", desc = "Target players to elytra!", type = Type.Move)
public class ElytraTarget extends Function {

    public final ModeSetting mode = new ModeSetting("Тип", "Обычный", "Обычный", "Продвинутый");

    private final SliderSetting sila = new SliderSetting("Отлёт", 8, 5, 40, 1, () -> mode.is("Продвинутый"));
    private final SliderSetting silaTime = new SliderSetting("Время отлёта", 400f, 200f, 1000f, 10f, () -> mode.is("Продвинутый"));

    private final BindBooleanSetting perelet = new BindBooleanSetting("Перелёт", true);
    private final SliderSetting predict = new SliderSetting("Предикт", 2.6f, 0.1f, 6f, 0.1f, () -> perelet.get());

    private final BooleanSetting leaveHP = new BooleanSetting("Улетать", true, () -> mode.is("Продвинутый"));
    private final MultiSetting leaveList = new MultiSetting("Улетать при",
            Arrays.asList("Мало здоровья", "При исп. предмета", "Отжим щита"),
            List.of("Мало здоровья", "При исп. предмета"),
            () -> leaveHP.get() && mode.is("Продвинутый"));

    private final BindBooleanSetting resolver = new BindBooleanSetting("Resolver уклонение", true, "Не даёт противнику попасть по вам, не эффективно если в на земле", () -> mode.is("Продвинутый"));
    private final SliderSetting resolverStrength = new SliderSetting("Сила резольвера", 12, 4, 30, 1, () -> resolver.get() && mode.is("Продвинутый"));

    public final BindBooleanSetting prefer = new BindBooleanSetting("Менять направление", true, "Менять направление полёта при ударе", () -> mode.is("Продвинутый"));

    private final MultiSetting preferDir = new MultiSetting("Направление",
            Arrays.asList("Север", "Юг", "Запад", "Восток", "Верх"),
            List.of("Север", "Юг", "Запад", "Восток", "Верх", "Вниз"),
            () -> mode.is("Продвинутый") && prefer.get());

    private final BooleanSetting avoidWalls = new BooleanSetting("Избегать стены", true, "Отклоняет траекторию, если впереди стена", () -> mode.is("Продвинутый"));
    private final BooleanSetting firework = new BooleanSetting("Отправка фейерверков", true, () -> mode.is("Продвинутый"));
    private final SliderSetting fireworkTime = new SliderSetting("Время отправки", 800f, 100f, 2000f, 10.0f, () -> firework.get() && mode.is("Продвинутый"));

    private final BooleanSetting fakelags = new BooleanSetting("Fake Lags", true, () -> mode.is("Продвинутый"));
    private final SliderSetting fakelagsDistance = new SliderSetting("Дистанция флагера", 10.0, 5.0, 20.0, 0.5, () -> fakelags.get() && mode.is("Продвинутый"));
    private final SliderSetting fakelagsMaxDistance = new SliderSetting("Макс дистанция флагера", 30.0, 15.0, 50.0, 1.0, () -> fakelags.get() && mode.is("Продвинутый"));

    private final CopyOnWriteArrayList<Packet<?>> packetBuffer = new CopyOnWriteArrayList<>();
    private boolean isAccumulatingPackets = false;
    private boolean wasInFarRange = false;
    private LivingEntity currentTarget = null;
    private Vec3d defensivePos = null;
    private final TimerUtil defensiveTimer = new TimerUtil();

    private final BooleanSetting visual = new BooleanSetting("Рендерить позицию", false, () -> mode.is("Продвинутый") || mode.is("Обычный"));

    @Getter
    boolean defensiveActive, lastDefensive;

    private int rotationPhase = 0;
    private int directionIndex = 0;
    private long lastHitTime = 0;
    private LivingEntity lastTarget = null;

    private long targetLastAttackTime = 0;

    private final Map<String, Vec3d> namedDirections = Map.of(
            "Север", new Vec3d(0, 0, -1),
            "Юг", new Vec3d(0, 0, 1),
            "Запад", new Vec3d(-1, 0, 0),
            "Восток", new Vec3d(1, 0, 0),
            "Верх", new Vec3d(0, 1, 0),
            "Вниз", new Vec3d(0, -1, 0)
    );

    private List<Vec3d> airDirections = new ArrayList<>();

    private final TimerUtil fireworkTimer = new TimerUtil();
    private final TimerUtil sanyaEblan = new TimerUtil();
    public boolean trueFireWork = false;

    private int x, y, z;

    public ElytraTarget() {
        addSettings(
                mode,
                sila, silaTime,
                perelet, predict,
                leaveHP, leaveList,
                resolver, resolverStrength,
                prefer, preferDir,
                avoidWalls,
                firework, fireworkTime,
                fakelags, fakelagsDistance, fakelagsMaxDistance,
                visual
        );
    }

    @Override
    public void onEnable() {
        rebuildAirDirections();
        packetBuffer.clear();
        isAccumulatingPackets = false;
        wasInFarRange = false;
        currentTarget = null;
        defensivePos = null;
    }

    private void rebuildAirDirections() {
        airDirections.clear();
        boolean isFlying = mc.player != null && mc.player.isGliding();

        for (String name : preferDir.getAvailableModes()) {
            if (!preferDir.get(name)) continue;
            if (name.equals("Вниз") && !isFlying) continue;
            Vec3d dir = namedDirections.get(name);
            if (dir != null) airDirections.add(dir);
        }

        if (airDirections.isEmpty()) {
            for (Map.Entry<String, Vec3d> entry : namedDirections.entrySet()) {
                if (entry.getKey().equals("Вниз") && !isFlying) continue;
                airDirections.add(entry.getValue());
            }
        }
        if (directionIndex >= airDirections.size()) directionIndex = 0;
    }

    @Override
    public void onEvent(Event event) {
        if (mode.is("Продвинутый") && mc.player.isGliding()) {
            if (event instanceof EventPacket packetEvent) {
                handlePacketEvent(packetEvent);
            }

            if (event instanceof EventUpdate) {
                updateFakeLagsState();

                if (firework.get()) {
                    if (fireworkTimer.hasTimeElapsed(fireworkTime.get().longValue())) {
                        if (trueFireWork) {
                            InventoryUtil.inventorySwapClick2(Items.FIREWORK_ROCKET, true, false);
                        }
                        fireworkTimer.reset();
                    }
                }
            }
            if (event instanceof EventUpdate) {
                defensiveActive = shouldLeave();
                lastDefensive = defensiveActive;

                if (resolver.get() && rotationPhase != 0) {
                    float evadePitch = (float) (Math.sin(System.currentTimeMillis() / 100.0) * resolverStrength.get().floatValue());
                    float evadeYaw = (float) (Math.cos(System.nanoTime() / 100.0) * resolverStrength.get().floatValue());
                    float cy = Manager.ROTATION.getYaw();
                    float cp = Manager.ROTATION.getPitch();
                    Manager.ROTATION.set(cy + evadeYaw, MathHelper.clamp(cp + evadePitch, -89.9f, 89.9f));
                }

                if (rotationPhase != 0 && System.currentTimeMillis() - lastHitTime > silaTime.get().floatValue()) {
                    rotationPhase = 0;
                }
            }
            if (event instanceof EventRender3D render) {
                if (visual.get() && mc.player.isGliding() && Manager.FUNCTION_MANAGER.attackAura.target != null) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    Box box = new Box(pos).contract(0.01);
                    RenderUtil.render3D.drawHoleOutline(box, ColorUtil.rgba(128, 255, 128, 255), 2);
                }

                if (defensivePos != null && fakelags.get() && isAccumulatingPackets) {
                    RenderAddon.renderFakePlayer(defensivePos, fakelags.get(), isAccumulatingPackets, render);
                }
            }
        }
    }

    private void handlePacketEvent(EventPacket packetEvent) {
        if (!fakelags.get() || !(packetEvent.getPacket() instanceof Packet)) return;

        if (isAccumulatingPackets && packetEvent.isSendPacket() && !(packetEvent.getPacket() instanceof KeepAliveC2SPacket)) {
            packetBuffer.add(packetEvent.getPacket());
            packetEvent.setCancel(true);
        }
    }

    private void updateFakeLagsState() {
        if (!fakelags.get()) {
            if (isAccumulatingPackets) {
                sendBufferedPackets();
                isAccumulatingPackets = false;
            }
            return;
        }

        boolean isFlying = mc.player.isGliding();
        LivingEntity target = Manager.FUNCTION_MANAGER.attackAura.target;
        boolean hasTarget = target != null;

        if (target != null && !target.equals(currentTarget)) {
            currentTarget = target;
            if (isAccumulatingPackets) {
                sendBufferedPackets();
            }
        }

        if (!isFlying || !hasTarget || perelet.get() || defensiveActive) {
            if (isAccumulatingPackets) {
                sendBufferedPackets();
                isAccumulatingPackets = false;
            }
            wasInFarRange = false;
            defensivePos = null;
            return;
        }

        double distanceToTarget = mc.player.getPos().distanceTo(target.getPos());
        double triggerDistance = fakelagsDistance.get().doubleValue();
        double maxDistance = fakelagsMaxDistance.get().doubleValue();

        if (distanceToTarget > maxDistance) {
            if (isAccumulatingPackets) {
                sendBufferedPackets();
                isAccumulatingPackets = false;
            }
            wasInFarRange = false;
            defensivePos = null;
            return;
        }
        if (distanceToTarget > triggerDistance && distanceToTarget <= maxDistance) {
            if (!isAccumulatingPackets) {
                isAccumulatingPackets = true;
                defensivePos = mc.player.getPos();
                defensiveTimer.reset();
            }
            wasInFarRange = true;
        } else if (distanceToTarget <= triggerDistance && wasInFarRange) {
            sendBufferedPackets();
            isAccumulatingPackets = false;
            wasInFarRange = false;
            defensivePos = null;
        } else if (distanceToTarget <= triggerDistance) {
            isAccumulatingPackets = false;
            wasInFarRange = false;
            defensivePos = null;
        }

        if (isAccumulatingPackets && defensiveTimer.hasTimeElapsed(1500)) {
            sendBufferedPackets();
            isAccumulatingPackets = true;
            defensiveTimer.reset();
        }
    }

    private void sendBufferedPackets() {
        if (packetBuffer.isEmpty()) return;
        for (Packet<?> packet : packetBuffer) {
            NetworkUtils.sendSilentPacket(packet);
        }
        packetBuffer.clear();
    }

    public void onTargetAttack(LivingEntity attacker) {
        if (attacker != null && attacker.equals(lastTarget)) {
            targetLastAttackTime = System.currentTimeMillis();
            if (fakelags.get()) {
                sendBufferedPackets();
                isAccumulatingPackets = false;
                wasInFarRange = false;
                defensivePos = null;
            }
        }
    }

    @Override
    public void onDisable() {
        sendBufferedPackets();
        isAccumulatingPackets = false;
        wasInFarRange = false;
        currentTarget = null;
        defensivePos = null;
    }

    private boolean shouldLeave() {
        boolean lowHP = mc.player.getHealth() <= 4.0f && leaveList.get("Мало здоровья");
        boolean usingItem = mc.player.isUsingItem() && !mc.player.getActiveItem().isOf(Items.SHIELD) && leaveList.get("При исп. предмета");
        return leaveHP.get() && (lowHP || usingItem);
    }

    public void nextPhase(LivingEntity target) {
        lastTarget = target;
        lastHitTime = System.currentTimeMillis();
        rotationPhase = 1;

        if (fakelags.get()) {
            sendBufferedPackets();
            isAccumulatingPackets = false;
            wasInFarRange = false;
            defensivePos = null;
        }

        boolean isFlying = mc.player != null && mc.player.isGliding();
        List<Vec3d> newDirections = new ArrayList<>();
        for (String name : preferDir.getAvailableModes()) {
            if (!preferDir.get(name)) continue;
            if (name.equals("Вниз") && !isFlying) continue;
            Vec3d dir = namedDirections.get(name);
            if (dir != null) newDirections.add(dir);
        }
        if (newDirections.isEmpty()) {
            for (Map.Entry<String, Vec3d> entry : namedDirections.entrySet()) {
                if (entry.getKey().equals("Вниз") && !isFlying) continue;
                newDirections.add(entry.getValue());
            }
        }

        int oldIndexInNew = -1;
        Vec3d oldDir = null;
        if (!airDirections.isEmpty() && directionIndex >= 0 && directionIndex < airDirections.size()) {
            oldDir = airDirections.get(directionIndex);
        }
        if (oldDir != null) {
            for (int i = 0; i < newDirections.size(); i++) {
                Vec3d v = newDirections.get(i);
                if (Double.compare(v.x, oldDir.x) == 0 && Double.compare(v.y, oldDir.y) == 0 && Double.compare(v.z, oldDir.z) == 0) {
                    oldIndexInNew = i;
                    break;
                }
            }
        }

        int size = newDirections.size();
        int candidateIndex = (oldIndexInNew == -1) ? 0 : (oldIndexInNew + 1) % size;
        int attempts = 0;
        int chosenIndex = candidateIndex;

        while (attempts < size) {
            Vec3d dir = newDirections.get(candidateIndex);
            if (resolver.get() && dir.y < 0 && !mc.player.isGliding()) {
                candidateIndex = (candidateIndex + 1) % size;
                attempts++;
                continue;
            }

            Vec3d candidatePos = target.getPos().add(dir.normalize().multiply(sila.get().floatValue()));
            Vec3d tryBetter = findClosestValidPosAround(candidatePos, 4.5);
            Vec3d checkPos = tryBetter != null ? tryBetter : new Vec3d(candidatePos.x, Math.max(candidatePos.y, mc.player.getY() + 6), candidatePos.z);

            if (!(avoidWalls.get() && isObstructed(mc.player.getEyePos(), checkPos)) && isValidFlyPosition(checkPos)) {
                chosenIndex = candidateIndex;
                break;
            }

            candidateIndex = (candidateIndex + 1) % size;
            attempts++;
        }

        airDirections = newDirections;
        directionIndex = chosenIndex;
    }

    public void overtakingElytra(LivingEntity base, boolean attack) {
        boolean leave = shouldLeave();
        boolean inEvasion = rotationPhase != 0 || leave;

        Vec3d targetPos = base.getPos().add(0, base.getHeight() / 2.0, 0);
        Vec3d modifiedPos;

        if (inEvasion) {
            Vec3d dir = airDirections.get(directionIndex);
            double strength = sila.get().floatValue();
            Vec3d candidatePos = targetPos.add(dir.normalize().multiply(strength));
            Vec3d tryBetter = findClosestValidPosAround(candidatePos, 10);
            if (tryBetter != null) {
                trueFireWork = true;
                candidatePos = tryBetter;
            } else {
                candidatePos = new Vec3d(candidatePos.x, Math.max(candidatePos.y, mc.player.getY() + 2), candidatePos.z);
            }
            modifiedPos = candidatePos;

            if (mc.player.getPos().distanceTo(modifiedPos) < 1) {
                rotationPhase = 0;
            }
        } else {
            double bps;
            try {
                bps = Double.parseDouble(ClientManager.getBps(base));
            } catch (Exception e) {
                bps = 0.0;
            }

            boolean canPredict = perelet.get() && bps >= 20.0 && !mc.player.isTouchingWater() && !mc.player.isSubmergedInWater();

            if (canPredict) {
                Vec3d motion = new Vec3d(base.getX() - base.prevX, base.getY() - base.prevY, base.getZ() - base.prevZ);
                double predictFactor = MathHelper.clamp(predict.get().floatValue(), 0.2F, 2.5F);
                Vec3d predicted = motion.multiply(predictFactor * 1.5);

                if (predicted.lengthSquared() < 1.0E-4) {
                    predicted = base.getRotationVecClient().normalize().multiply(predictFactor);
                }
                modifiedPos = base.getPos().add(predicted).add(0, base.getHeight() / 2.0, 0);
            } else {
                modifiedPos = base.getPos().add(0, base.getHeight() / 2.0, 0);
            }
        }

        double dx = modifiedPos.x - mc.player.getX();
        double dy = modifiedPos.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = modifiedPos.z - mc.player.getZ();

        x = (int) modifiedPos.x;
        y = (int) modifiedPos.y;
        z = (int) modifiedPos.z;

        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));

        Manager.ROTATION.setSmooth(
                targetYaw,
                targetPitch,
                1.15f,
                140f,
                40f,
                true
        );
    }

    public void targetDefault(LivingEntity base, boolean attack) {
        Vec3d targetPos = base.getPos();
        double bps;

        try {
            bps = Double.parseDouble(ClientManager.getBps(base));
        } catch (Exception e) {
            bps = 0.0;
        }

        boolean canPredict = perelet.get() && bps >= 20.0 && !mc.player.isTouchingWater() && !mc.player.isSubmergedInWater();

        if (canPredict) {
            Vec3d motion = new Vec3d(base.getX() - base.prevX, base.getY() - base.prevY, base.getZ() - base.prevZ);
            double predictFactor = MathHelper.clamp(predict.get().floatValue(), 0.2F, 2.5F);
            Vec3d predicted = motion.multiply(predictFactor * 1.5);

            if (predicted.lengthSquared() < 1.0E-4) {
                predicted = base.getRotationVecClient().normalize().multiply(predictFactor);
            }
            targetPos = targetPos.add(predicted);
        }

        double diffX = targetPos.x - mc.player.getX();
        double diffY = targetPos.y - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double diffZ = targetPos.z - mc.player.getZ();

        x = (int) targetPos.x;
        y = (int) targetPos.y;
        z = (int) targetPos.z;

        float yaw = (float) (Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f);
        float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, Math.hypot(diffX, diffZ))));

        Manager.ROTATION.setSmooth(
                yaw,
                pitch,
                1.15f,
                140f,
                40f,
                true
        );
    }


    private Vec3d findClosestValidPosAround(Vec3d center, double radius) {
        List<Vec3d> offsets = new ArrayList<>();
        for (int y = 2; y >= -1; y--) {
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx == 0 && dz == 0 && y == 0) continue;
                    offsets.add(new Vec3d(dx, y, dz));
                }
            }
        }

        Vec3d best = null;
        double bestDist = Double.MAX_VALUE;

        for (Vec3d offset : offsets) {
            Vec3d candidate = center;
            if (!isValidFlyPosition(candidate)) continue;
            if (isObstructed(mc.player.getEyePos(), candidate)) continue;
            double dist = candidate.squaredDistanceTo(center);
            if (dist < bestDist) {
                best = candidate;
                bestDist = dist;
            }
        }
        return best;
    }

    private boolean isObstructed(Vec3d from, Vec3d to) {
        return mc.world.raycast(new net.minecraft.world.RaycastContext(
                from, to,
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                mc.player
        )).getType() != HitResult.Type.MISS;
    }

    private boolean isValidFlyPosition(Vec3d pos) {
        Vec3d head = new Vec3d(pos.x, pos.y + mc.player.getHeight(), pos.z);
        Vec3d feet = new Vec3d(pos.x, pos.y, pos.z);
        boolean obstructedVertically = mc.world.raycast(new net.minecraft.world.RaycastContext(
                head, feet,
                net.minecraft.world.RaycastContext.ShapeType.COLLIDER,
                net.minecraft.world.RaycastContext.FluidHandling.NONE,
                mc.player
        )).getType() != HitResult.Type.MISS;

        int blockX = (int) Math.floor(pos.x);
        int blockY = (int) Math.floor(pos.y);
        int blockZ = (int) Math.floor(pos.z);

        boolean isBlockSolid = mc.world.getBlockState(new BlockPos(blockX, blockY, blockZ)).isOpaqueFullCube();
        int groundY = mc.world.getTopPosition(net.minecraft.world.Heightmap.Type.MOTION_BLOCKING, new BlockPos(blockX, 0, blockZ)).getY();

        boolean belowGround = pos.y < groundY + 1.0;

        return !obstructedVertically && !isBlockSolid && !belowGround;
    }
}