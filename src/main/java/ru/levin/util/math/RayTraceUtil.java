package ru.levin.util.math;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import ru.levin.manager.IMinecraft;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class RayTraceUtil implements IMinecraft {

    private static final Map<UUID, Long> lastHitTimes = new HashMap<>();
    private static final long EFFECT_DURATION = 200;

    public static void markHit(Entity entity) {
        lastHitTimes.put(entity.getUuid(), System.currentTimeMillis());
    }
    public static float getHitProgress(Entity entity) {
        Long hitTime = lastHitTimes.get(entity.getUuid());
        if (hitTime == null) return 0f;

        long elapsed = System.currentTimeMillis() - hitTime;
        if (elapsed > EFFECT_DURATION) {
            lastHitTimes.remove(entity.getUuid());
            return 0f;
        }

        return 1f - ((float) elapsed / EFFECT_DURATION);
    }
    private static Vec3d getVector(float pitch, float yaw) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);
        float cosPitch = (float) Math.cos(-pitchRad);
        return new Vec3d(
                -Math.sin(yawRad) * cosPitch,
                -Math.sin(pitchRad),
                Math.cos(yawRad) * cosPitch
        );
    }
    public static Entity getMouseOver(Entity target, float yaw, float pitch, double distance) {
        Entity entity = mc.getCameraEntity();
        if (entity == null || mc.world == null || target == null) {
            return null;
        }

        Box playerBox = entity.getBoundingBox();
        Box targetBox = target.getBoundingBox();
        Vec3d startVec = entity.getEyePos();
        Vec3d directionVec = getVector(pitch, yaw);
        Vec3d endVec = startVec.add(directionVec.x * distance, directionVec.y * distance, directionVec.z * distance);
        if (playerBox.intersects(targetBox)) {
            EntityHitResult hitResult = rayCastEntity(distance, yaw, pitch, (e) -> e == target && !e.isSpectator() && e.canBeHitByProjectile());
            if (hitResult != null && hitResult.getEntity() == target) {
                return target;
            }
        }

        EntityHitResult entityHitResult = rayCastEntities(entity, startVec, endVec, targetBox, (e) -> e == target && !e.isSpectator() && e.canBeHitByProjectile(), distance);

        if (entityHitResult != null && startVec.distanceTo(entityHitResult.getPos()) <= distance) {
            return entityHitResult.getEntity();
        }

        return null;
    }

    public static EntityHitResult rayCastEntity(double range, float yaw, float pitch, Predicate<Entity> filter) {
        Entity entity = mc.getCameraEntity();
        if (entity == null || mc.world == null) {
            return null;
        }
        Vec3d cameraVec = entity.getCameraPosVec(1.0F);
        float pitchRad = pitch * 0.017453292F;
        float yawRad = -yaw * 0.017453292F;
        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);
        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);
        Vec3d rotationVec = new Vec3d(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
        Vec3d end = cameraVec.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
        Box box = entity.getBoundingBox().stretch(rotationVec.multiply(range)).expand(1.0, 1.0, 1.0);

        return ProjectileUtil.raycast(entity, cameraVec, end, box, filter, range * range);
    }
    private static EntityHitResult rayCastEntities(Entity source, Vec3d start, Vec3d end, Box boundingBox, java.util.function.Predicate<Entity> predicate, double maxDistance) {
        World world = source.getWorld();
        double closestDistance = maxDistance;
        Entity closestEntity = null;
        Vec3d closestHitPos = null;

        for (Entity entity : world.getEntitiesByClass(Entity.class, boundingBox, predicate)) {
            if (entity == source) continue;

            Box entityBox = entity.getBoundingBox();
            var hit = entityBox.raycast(start, end);

            if (hit.isPresent()) {
                Vec3d hitPos = hit.get();
                double distance = start.distanceTo(hitPos);

                if (distance < closestDistance) {
                    closestEntity = entity;
                    closestHitPos = hitPos;
                    closestDistance = distance;
                }
            }
        }

        if (closestEntity != null) {
            return new EntityHitResult(closestEntity, closestHitPos);
        }
        return null;
    }
    public static BlockHitResult rayCast(double range, float yaw, float pitch, boolean includeFluids) {
        Entity entity = mc.getCameraEntity();
        if (entity == null || mc.world == null) {
            return null;
        }
        Vec3d start = entity.getCameraPosVec(1.0F);
        float pitchRad = pitch * 0.017453292F;
        float yawRad = -yaw * 0.017453292F;
        float cosPitch = (float) Math.cos(pitchRad);
        float sinPitch = (float) Math.sin(pitchRad);
        float cosYaw = (float) Math.cos(yawRad);
        float sinYaw = (float) Math.sin(yawRad);
        Vec3d rotationVec = new Vec3d(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
        Vec3d end = start.add(rotationVec.x * range, rotationVec.y * range, rotationVec.z * range);
        World world = mc.world;
        RaycastContext.FluidHandling fluidHandling = includeFluids ? RaycastContext.FluidHandling.ANY : RaycastContext.FluidHandling.NONE;
        RaycastContext context = new RaycastContext(start, end, RaycastContext.ShapeType.OUTLINE, fluidHandling, entity);

        return world.raycast(context);
    }
}