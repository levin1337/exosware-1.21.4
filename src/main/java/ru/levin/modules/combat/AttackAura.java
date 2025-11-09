package ru.levin.modules.combat;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.input.EventKeyBoard;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.events.impl.player.EventSprint;
import ru.levin.manager.Manager;
import ru.levin.mixin.iface.ClientPlayerEntityAccessor;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.movement.ElytraTarget;
import ru.levin.modules.render.littlePet.GhostWolfEntity;
import ru.levin.modules.setting.BindBooleanSetting;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.math.RayTraceUtil;
import ru.levin.util.move.MoveUtil;
import ru.levin.util.player.AuraUtil;
import ru.levin.util.player.InventoryUtil;
import ru.levin.util.vector.VectorUtil;

import java.util.*;

import static net.minecraft.util.Hand.MAIN_HAND;

@SuppressWarnings("All")
@FunctionAnnotation(name = "AttackAura", keywords = {"Пиздить","Хуячить","KillAura"}, desc = "Ебашит бошки всем вокруг", type = Type.Combat)
public class AttackAura extends Function {

    private final ModeSetting mode = new ModeSetting("Мод",
            "ReallyWorld",
            "ReallyWorld",
            "HollyWorld",
            "FunTime",
            "SpookyTime",
            "Snap",
            "KoopinAc",
            "LonyGrief",
            "1.8.8"
    );

    private final MultiSetting targets = new MultiSetting(
            "Цели",
            Arrays.asList("Игроки", "Голые", "Мобы", "Монстры"),
            new String[]{"Игроки", "Голые", "Друзья", "Мобы", "Монстры", "Жители"}
    );

    private final ModeSetting sort = new ModeSetting("Сортировать",
            "По здоровью",
            "По здоровью",
            "По дистанции",
            "По броне"
    );

    private final MultiSetting setting = new MultiSetting(
            "Настройки",
            Arrays.asList("Только критами", "Ломать щит", "Отжим щита"),
            new String[]{"Только критами", "Ломать щит", "Отжим щита"}
    );

    private final SliderSetting distance = new SliderSetting("Радиус атаки", 3.0f, 1.8f, 6f, 0.1f);
    private final SliderSetting rotateDistance = new SliderSetting("Радиус обнаружения", 5f, 0.0f, 10f, 0.1f);

    private final SliderSetting elytraDistance = new SliderSetting("Радиус на элитрах", 40f, 0f, 80f, 1f);
    private final SliderSetting snapSpeed = new SliderSetting("Скорость снапов", 150, 50, 300, 50f, () -> mode.is("Snap"));

    private final BindBooleanSetting onlySpaceCritical = new BindBooleanSetting("Только с пробелом", false, () -> setting.get("Только критами"));
    private final BooleanSetting noAttackIfEat = new BooleanSetting("Не бить если ешь", false);
    private final BooleanSetting raycast = new BooleanSetting("Проверять наведение", false);

    public final BooleanSetting correction = new BooleanSetting("Коррекция", true);
    public final ModeSetting correctionType = new ModeSetting(() -> correction.get(), "Тип коррекции", "Free", "Free", "Focus");
    private final ModeSetting sprintreset = new ModeSetting("Тип спринта", "Rage", "Rage", "Legit", "None");

    public LivingEntity target = null;
    private long cpsLimit = 0L;
    private long lastHitMs = 0L;
    private int preSprintTicks = 0;

    public AttackAura() {
        addSettings(
                mode,
                targets,
                sort,
                setting,
                distance,
                rotateDistance,
                elytraDistance,
                snapSpeed,
                correction,
                correctionType,
                sprintreset,
                onlySpaceCritical,
                noAttackIfEat,
                raycast
        );
    }

    @Override
    public void onEvent(Event event) {
        ClientPlayerEntity player = mc.player;
        if (player == null || player.isDead()) {
            target = null;
            preSprintTicks = 0;
            return;
        }

        if (event instanceof EventKeyBoard e) {
            if (correction.get() && correctionType.is("Free")) {
                MoveUtil.fixMovement(e, Manager.FUNCTION_MANAGER.autoPotion.isActivePotion ? Manager.ROTATION.getPitch() : Manager.ROTATION.getYaw());
            }
        }

        if (event instanceof EventSprint sprint) {
            if (sprintreset.is("Legit")) {
                if (canAttack() && target != null  && player.isSprinting()) {
                    sprint.setSprinting(false);
                }
            }
        }

        if (event instanceof EventUpdate) {
            if (target == null || !isValidTarget(target)) {
                target = findTarget();
            }

            if (target == null) {
                Manager.ROTATION.set(player.getYaw(), player.getPitch());
                cpsLimit = System.currentTimeMillis();
                return;
            }

            handleAttackAndRotation(target);
        }

        if (event instanceof EventMotion motion) {
            motion.setYaw(Manager.ROTATION.getYaw());
            motion.setPitch(Manager.ROTATION.getPitch());
        }
    }

    @Override
    protected void onEnable() {
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        if (target != null && isValidTarget(target)) {
            String modeName = mode.get();
            if (modeName.equals("FunTime") || modeName.equals("HollyWorld") || modeName.equals("ReallyWorld")) {
                Manager.ROTATION.smoothReturn(350);
            } else {
                Manager.ROTATION.set(mc.player.getYaw(), mc.player.getPitch());
            }
        }

        target = null;
        cpsLimit = System.currentTimeMillis();
        super.onDisable();
    }

    private boolean isValidTarget(LivingEntity entity) {
        if (entity == null || entity.isDead() || !entity.isAlive() || entity == mc.player) return false;

        double dist = AuraUtil.getDistance(entity);
        double attackRange = distance.get().doubleValue();
        double detectRange = mc.player.isGliding() ? elytraDistance.get().doubleValue() : rotateDistance.get().doubleValue();

        if (dist > attackRange && (detectRange <= 0 || dist > detectRange)) return false;
        if (Manager.FUNCTION_MANAGER.antiBot.check(entity)) return false;

        if (entity instanceof PlayerEntity) {
            if (!targets.get("Игроки")) return false;
            if (!targets.get("Друзья") && Manager.FRIEND_MANAGER.isFriend(entity.getName().getString())) return false;
        } else if (entity instanceof VillagerEntity && !targets.get("Жители")) return false;
        else if (entity instanceof MobEntity || entity instanceof AnimalEntity) {
            if (!targets.get("Мобы")) return false;
        } else if (entity instanceof Monster && !targets.get("Монстры")) return false;

        if (entity instanceof ArmorStandEntity) return false;
        if (Manager.FUNCTION_MANAGER.littleSnickers.state && (entity instanceof GhostWolfEntity)) return false;

        return true;
    }

    private LivingEntity findTarget() {
        List<LivingEntity> list = new ArrayList<>();
        for (Entity e : Manager.SYNC_MANAGER.getEntities()) {
            if (e instanceof LivingEntity le && isValidTarget(le)) list.add(le);
        }
        if (list.isEmpty()) return null;

        switch (sort.get()) {
            case "По здоровью":
                list.sort(Comparator.comparing(LivingEntity::getHealth));
                break;
            case "По дистанции":
                list.sort(Comparator.comparingDouble(mc.player::distanceTo));
                break;
            case "По броне":
                list.sort(Comparator.comparingDouble(AuraUtil::getArmor));
                break;
            default:
                break;
        }
        return list.get(0);
    }

    private float randomYawOffset = 0;
    private float randomPitchOffset = 0;
    private int randomUpdateTicks = 0;
    private float bodyYaw, bodyPitch, prevBodyYaw, prevBodyPitch;
    private float headYaw, headPitch, prevHeadYaw, prevHeadPitch;

    private final int updateInterval = 2;
    private final float maxYawShake = 0.3f;
    private final float maxPitchShake = 0.25f;
    private final Random random = new Random();


    private long shakeStartTime = 0L;
    private void handleAttackAndRotation(LivingEntity t) {
        float currYaw = Manager.ROTATION.getYaw();
        float currPitch = Manager.ROTATION.getPitch();

        boolean canAttackNow = shouldAttack(t);
        boolean passRay = !raycast.get() || RayTraceUtil.getMouseOver(t, currYaw, currPitch, distance.get().floatValue()) == t;
        boolean noPotion = !Manager.FUNCTION_MANAGER.autoPotion.isActivePotion;

        if (handleElytraRotation(t)) {
            if (canAttackNow && passRay && noPotion) attackTarget(mc.player);
            return;
        }

        if (mode.is("KoopinAc") || mode.is("1.8.8")) {
            koopinVector(t, true);
            if (canAttackNow && passRay && noPotion) attackTarget(mc.player);
            return;
        }

        if (mode.is("SpookyTime")) {
            if (mc.player == null) return;

            if (target == null) {
                randomYawOffset = 0;
                randomPitchOffset = 0;
                float centerYaw = mc.player.getYaw();
                float centerPitch = mc.player.getPitch();

                {
                    prevBodyYaw = bodyYaw;
                    prevBodyPitch = bodyPitch;

                    float yawDiff = MathHelper.wrapDegrees(centerYaw - bodyYaw);
                    float pitchDiff = centerPitch - bodyPitch;

                    float yawStep = MathHelper.clamp(yawDiff, -45, 45);
                    float pitchStep = MathHelper.clamp(pitchDiff, -45, 45);

                    bodyYaw += yawStep;
                    bodyPitch = MathHelper.clamp(bodyPitch + pitchStep, -90f, 90f);
                }

                {
                    prevHeadYaw = headYaw;
                    prevHeadPitch = headPitch;

                    float yawDiff = MathHelper.wrapDegrees(centerYaw - headYaw);
                    float pitchDiff = centerPitch - headPitch;

                    float yawStep = MathHelper.clamp(yawDiff, -50, 50);
                    float pitchStep = MathHelper.clamp(pitchDiff, -50, 50);

                    headYaw += yawStep;
                    headPitch = MathHelper.clamp(headPitch + pitchStep, -90f, 90f);
                }
                return;
            }

            randomUpdateTicks++;
            if (randomUpdateTicks >= updateInterval) {
                randomUpdateTicks = 0;
                randomYawOffset = (random.nextFloat() * 2 - 1) * maxYawShake;
                randomPitchOffset = (random.nextFloat() * 2 - 1) * maxPitchShake;
            }

            Vec3d targetPos;
            {
                double x = target.getBoundingBox().getCenter().x;
                double y = target.getY();
                double z = target.getBoundingBox().getCenter().z;

                int randPoint = random.nextInt(4);
                switch (randPoint) {
                    case 0 -> y += target.getHeight() * 0.9;
                    case 1 -> y += target.getHeight() * 0.75;
                    case 2 -> y += target.getHeight() * 0.5;
                    case 3 -> y += target.getHeight() * 0.25;
                }

                x += (random.nextDouble() * 0.4 - 0.2);
                z += (random.nextDouble() * 0.4 - 0.2);

                targetPos = new Vec3d(x, y, z);
            }

            Vec2f rot;
            {
                Vec3d eyePos = mc.player.getEyePos();
                double deltaX = targetPos.x - eyePos.x;
                double deltaY = targetPos.y - eyePos.y;
                double deltaZ = targetPos.z - eyePos.z;
                double hDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

                double yaw = (Math.atan2(deltaZ, deltaX) * 180 / Math.PI) - 90.0f;
                double pitch = -(Math.atan2(deltaY, hDistance) * 180 / Math.PI);

                rot = new Vec2f((float) pitch, (float) yaw);
            }
            {
                prevBodyYaw = bodyYaw;
                prevBodyPitch = bodyPitch;

                float yawDiff = MathHelper.wrapDegrees(rot.y - bodyYaw);
                float pitchDiff = rot.x - bodyPitch;

                bodyYaw += MathHelper.clamp(yawDiff, -45, 45);
                bodyPitch = MathHelper.clamp(bodyPitch + pitchDiff, -45, 45);
            }

            Vec2f headRotation;
            {
                prevHeadYaw = headYaw;
                prevHeadPitch = headPitch;

                float yawDiff = MathHelper.wrapDegrees(rot.y - headYaw);
                float pitchDiff = rot.x - headPitch;

                headYaw += MathHelper.clamp(yawDiff, -50, 50);
                headPitch = MathHelper.clamp(pitchDiff, -50, 50);

                headRotation = new Vec2f(
                        rot.x + randomPitchOffset,
                        rot.y + randomYawOffset
                );
            }
            Manager.ROTATION.setSmooth(headRotation.y, headRotation.x, 0.8f, 60, 90, true);
            if (canAttackNow && passRay && noPotion) {
                attackTarget(mc.player);
            }

            return;
        }

        if (mode.is("LonyGrief")) {
            Vec3d tp = predictPos(t);
            double yawToTarget = Math.toDegrees(Math.atan2(tp.z - mc.player.getZ(), tp.x - mc.player.getX())) - 90.0;
            double yawDiff = Math.abs(MathHelper.wrapDegrees((float) yawToTarget - currYaw));
            if (yawDiff <= 180 && canAttackNow && passRay && noPotion) attackTarget(mc.player);
            Manager.ROTATION.set(mc.player.getYaw(), mc.player.getPitch());
            return;
        }

        if (mode.is("FunTime")) {
            if (canAttackNow && canAttack() && noPotion) {
                if (passRay) {
                    attackTarget(mc.player);
                }
                lastHitMs = System.currentTimeMillis();
            }
           if (System.currentTimeMillis() - lastHitMs < 450) {
               funtime(t);

           } else {
               long currentTime = System.currentTimeMillis();
               if (shakeStartTime == 0L)
                   shakeStartTime = currentTime;
               float elapsedSec = (currentTime - shakeStartTime) / 1000f;

               double angle = 2 * Math.PI * 2.4 * elapsedSec;
               float yawOffset = (float) Math.sin(angle) * 24f;

               double angle2 = 2 * Math.PI * 0.08f * elapsedSec;
               double[] options = {5.0, 5.5, 5.8, 6.0};
               double randAmplitude = options[(int)(Math.random() * options.length)];
               float yawOffset2 = (float) ((float) Math.sin(angle2) * randAmplitude);


               float finalYaw = mc.player.getYaw() + yawOffset + yawOffset2;
               float finalPitch = 0.0f + yawOffset2;
               Manager.ROTATION.setSmooth(finalYaw, finalPitch, 1.0f, 20f, 10f, true);
           }

            return;
        }

        if (mode.is("HollyWorld")) {
            if (canAttackNow && canAttack() && passRay && noPotion) {
                hollyworld(t, true);
                attackTarget(mc.player);
            } else {
                hollyworld(t, false);
            }
            return;
        }

        if (mode.is("Snap")) {
            if (canAttackNow && canAttack() && passRay && noPotion) {
                attackTarget(mc.player);
                lastHitMs = System.currentTimeMillis();
            }
            if (System.currentTimeMillis() - lastHitMs < (long) snapSpeed.get().floatValue()) {
                setRotation(t, true);
            } else {
                Manager.ROTATION.set(mc.player.getYaw(), mc.player.getPitch());
            }
            return;
        }

        if (canAttackNow && passRay && noPotion) {
            attackTarget(mc.player);
        }
        setRotation(t, true);
    }
    private void setRotation(LivingEntity entity, boolean applyGcd) {
        Vec3d tp = predictPos(entity);
        double dx = tp.x - mc.player.getX();
        double dy = (tp.y + entity.getEyeHeight(entity.getPose()) / 2.0) - (mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()));
        double dz = tp.z - mc.player.getZ();

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz)));
        Manager.ROTATION.setSmooth(yaw, pitch, 1.2f, 180f, 15f, applyGcd);
    }

    private void koopinVector(LivingEntity entity, boolean attackContext) {
        Vec3d head = entity.getEyePos().add(0, entity.getHeight(), 0);
        Vec3d chest = entity.getEyePos().add(0, entity.getStandingEyeHeight() / 2.0f, 0);
        Vec3d legs = entity.getEyePos().add(0, 0.05, 0);
        Vec3d[] points = new Vec3d[]{head, chest, legs};

        float bestPitchDelta = Float.MAX_VALUE;
        Vec3d best = chest;
        float currPitch = Manager.ROTATION.getPitch();
        float currYaw = Manager.ROTATION.getYaw();

        for (Vec3d p : points) {
            Vec3d eye = mc.player.getEyePos();
            double dx = p.x - eye.x;
            double dy = p.y - eye.y;
            double dz = p.z - eye.z;
            float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
            float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
            float pitchDelta = Math.abs(pitch - currPitch);
            if (pitchDelta < bestPitchDelta) {
                bestPitchDelta = pitchDelta;
                best = p;
            }
        }

        Vec3d eye = mc.player.getEyePos();
        double dx = best.x - eye.x;
        double dy = best.y - eye.y;
        double dz = best.z - eye.z;
        double dst = Math.sqrt(dx * dx + dz * dz);

        float yawTo = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitchTo = (float) (-Math.toDegrees(Math.atan2(dy, dst)));

        float yawDelta = MathHelper.wrapDegrees(yawTo - currYaw);
        float pitchDelta = pitchTo - currPitch;

        float addYaw = Math.min(Math.max(Math.abs(yawDelta), 1), 80);
        if (Math.abs(addYaw) <= 3.0f) addYaw = 3.1f;

        float addPitch = Math.max(attackContext ? Math.abs(pitchDelta) : 1.0f, 2.0f);

        float ny = currYaw + (yawDelta > 0 ? addYaw : -addYaw);
        float np = MathHelper.clamp(currPitch + (pitchDelta > 0 ? addPitch : -addPitch), -90.0f, 90.0f);

        Manager.ROTATION.set(ny, np);
    }

    private boolean swingSideRight = false;
    private float jitterYaw = 0f, jitterYawTarget = 0f, jitterYawSpeed = 0f;
    private float microJitter = 0f;
    private float swayPhase = 0f;
    private float swaySpeed = 0.04f;
    private float swayAmplitude = 2.5f;
    private long lastSwitch = 0L;
    private long lastBreathChange = 0L;


    private void funtime(LivingEntity entity) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d base = entity.getPos();

        float[] points = new float[]{0.82f, 0.67f, 0.43f, 0.27f};
        float mul = points[(int) (System.currentTimeMillis() / 180 % points.length)];
        Vec3d targetPos = new Vec3d(base.x, base.y + entity.getHeight() * mul, base.z);

        double halfWidth = entity.getWidth() / 2.0;
        double sideOffset = swingSideRight ? halfWidth * 1.2f : -halfWidth * 1;

        double yawToEntity = Math.atan2(entity.getZ() - mc.player.getZ(), entity.getX() - mc.player.getX());
        double offsetX = Math.cos(yawToEntity + Math.PI / 2) * sideOffset;
        double offsetZ = Math.sin(yawToEntity + Math.PI / 2) * sideOffset;
        targetPos = targetPos.add(offsetX, 0, offsetZ);

        double dx = targetPos.x - eye.x;
        double dy = targetPos.y - eye.y;
        double dz = targetPos.z - eye.z;
        double dist = Math.sqrt(dx * dx + dz * dz);

        float baseYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float basePitch = (float) -Math.toDegrees(Math.atan2(dy, dist));

        long now = System.currentTimeMillis();

        if (now - lastSwitch > 200 + random.nextInt(250)) {
            lastSwitch = now;
            swingSideRight = !swingSideRight;

            float distanceFactor = (float) MathHelper.clamp(dist / 6.0f, 0.4f, 1.0f);
            float maxDeviation = 4.0f * distanceFactor;

            jitterYawTarget = (swingSideRight ? maxDeviation : -maxDeviation) + (float) (random.nextGaussian() * 0.6f);
        }

        float diff = jitterYawTarget - jitterYaw;
        jitterYawSpeed += diff * 0.05f;
        jitterYawSpeed *= 0.88f;
        jitterYaw += jitterYawSpeed;
        jitterYaw *= 0.985f;

        if (now - lastBreathChange > 2000 + random.nextInt(1500)) {
            lastBreathChange = now;
            swaySpeed = 0.035f + random.nextFloat() * 0.02f;
            swayAmplitude = 2.0f + random.nextFloat() * 1.2f;
        }

        swayPhase += swaySpeed;
        float sway = (float) Math.sin(swayPhase) * swayAmplitude;
        float totalYawOffset = (float) MathHelper.clamp(jitterYaw + sway, -halfWidth * 8.5f, halfWidth * 8.5f);
        microJitter += (random.nextFloat() - 0.5f) * 0.25f;
        microJitter *= 0.85f;

        float finalYaw = baseYaw + totalYawOffset + microJitter;
        float finalPitch = basePitch + (float) Math.sin(swayPhase * 0.8f) * 0.5f;


        Manager.ROTATION.setSmooth(finalYaw, finalPitch, 1.1f, 180f, 15f, true);
    }



    private void hollyworld(LivingEntity entity, boolean force) {
        Vec3d eye = mc.player.getEyePos();
        Vec3d base = entity.getPos();
        float[] points = new float[]{0.85f, 0.65f, 0.35f, 0.25f};
        float mul = points[(int) (System.nanoTime() % points.length)];
        Vec3d aim = new Vec3d(base.x, base.y + entity.getHeight() * mul, base.z);

        double dx = aim.x - eye.x;
        double dy = aim.y - eye.y;
        double dz = aim.z - eye.z;

        double hd = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, hd));

        if (force) {
            Manager.ROTATION.set(yaw, MathHelper.clamp(pitch, -89.9f, 89.9f));
        } else {
            Manager.ROTATION.setSmooth(yaw, pitch, 0.25f, 45f, 12f, true);
        }
    }

    private boolean handleElytraRotation(LivingEntity t) {
        ElytraTarget ely = Manager.FUNCTION_MANAGER.elytraTarget;
        if (ely.state && mc.player.isGliding()) {
            if (ely.mode.is("Продвинутый")) ely.overtakingElytra(t, false);
            else ely.targetDefault(t, false);
            return true;
        }
        return false;
    }

    public void attackTarget(PlayerEntity player) {
        boolean sprintStop = false;
        boolean canStartSprint = mc.player.input.movementForward > 0
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && !mc.player.isGliding()
                && !mc.player.isUsingItem()
                && !mc.player.horizontalCollision
                && mc.player.getHungerManager().getFoodLevel() > 6
                && !mc.player.isSneaking();

        if (setting.get("Отжим щита") && mc.player.isBlocking()) {
            mc.interactionManager.stopUsingItem(mc.player);
        }

        if (sprintreset.is("Legit")) {
            if (mc.player.isSprinting() || canAttack()) {
                if (mc.player.isSprinting()) return;
            }
        }

        if (sprintreset.is("Rage")) {
            if (((ClientPlayerEntityAccessor) mc.player).getLastSprinting()) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
                mc.player.setSprinting(false);
                sprintStop = true;
            }
        }

        cpsLimit = System.currentTimeMillis() + 500L;

        mc.interactionManager.attackEntity(player, target);
        mc.player.swingHand(MAIN_HAND);

        ElytraTarget elytraTarget = Manager.FUNCTION_MANAGER.elytraTarget;
        if (elytraTarget.mode.is("Продвинутый")) {
            elytraTarget.trueFireWork = true;
            if (elytraTarget.prefer.get()) elytraTarget.nextPhase(target);
        }

        if (setting.get("Ломать щит")) shieldBreaker(false);

        if (sprintreset.is("Rage") && sprintStop && canStartSprint) {
            if (((ClientPlayerEntityAccessor) mc.player).getLastSprinting()) {
                mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));
                mc.player.setSprinting(true);
            }
        }
    }

    private boolean shouldAttack(LivingEntity e) {
        if (e == null || cpsLimit > System.currentTimeMillis()) return false;
        if (AuraUtil.getDistance(e) > distance.get().doubleValue()) return false;
        return canAttack();
    }

    private boolean canAttack() {
        if (noAttackIfEat.get() && mc.player.isUsingItem() && !mc.player.getActiveItem().isOf(Items.SHIELD)) return false;

        if (System.currentTimeMillis() < cpsLimit
                || (!(mc.player.getMainHandStack().isOf(Items.MACE))
                && mc.player.getAttackCooldownProgress(mc.getRenderTickCounter().getTickDelta(true)) < 0.9F)) return false;

        boolean restrict = mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                || mc.player.hasStatusEffect(StatusEffects.LEVITATION)
                || mc.player.hasStatusEffect(StatusEffects.SLOW_FALLING)
                || mc.player.isInLava()
                || mc.player.inPowderSnow
                || mc.player.isClimbing()
                || mc.player.hasVehicle()
                || mc.player.getAbilities().flying
                || (mc.player.isInFluid() && !mc.options.jumpKey.isPressed())
                || MoveUtil.isInWeb();

        boolean needSpace = onlySpaceCritical.get()
                && mc.player.isOnGround()
                && !mc.options.jumpKey.isPressed();

        if (setting.get("Только критами") && !restrict) {
            return needSpace || (!mc.player.isOnGround() && mc.player.fallDistance > 0.0f);
        }
        return true;
    }

    private boolean shieldBreaker(boolean instant) {
        int axeSlot = InventoryUtil.getAxe().slot();
        if (axeSlot == -1) return false;
        if (!(target instanceof PlayerEntity)) return false;
        if (!((PlayerEntity) target).isUsingItem() && !instant) return false;
        if (((PlayerEntity) target).getOffHandStack().getItem() != Items.SHIELD
                && ((PlayerEntity) target).getMainHandStack().getItem() != Items.SHIELD) return false;

        if (axeSlot >= 9) {
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, axeSlot, mc.player.getInventory().selectedSlot, SlotActionType.SWAP, mc.player);
            mc.getNetworkHandler().sendPacket(new CloseHandledScreenC2SPacket(mc.player.currentScreenHandler.syncId));
        } else {
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(axeSlot));
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
            mc.getNetworkHandler().sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));
        }
        return true;
    }

    private Vec3d predictPos(LivingEntity e) {
        Vec3d p = e.getPos();
        var ts = Manager.FUNCTION_MANAGER.targetStrafe;
        if (ts.state && ts.predictCheck.get()) {
            float pr = ts.predict.get().floatValue();
            if (pr > 0) {
                Vec3d v = e.getVelocity();
                p = p.add(v.x * pr, v.y * pr, v.z * pr);
            }
        }
        return p;
    }
}