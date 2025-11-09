package ru.levin.mixin.player;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.modules.combat.AttackAura;
import ru.levin.modules.combat.rotation.RotationController;
import ru.levin.modules.movement.SuperFirework;

@Mixin(FireworkRocketEntity.class)
public abstract class MixinFireworkRocketEntity implements IMinecraft {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V"))
    private void tick(LivingEntity shooter, Vec3d originalVelocity) {
        if (shooter.isGliding() && shooter == mc.player) {
            Vec3d rotationVector;
            SuperFirework superFirework = Manager.FUNCTION_MANAGER.superFirework;
            AttackAura attackAura = Manager.FUNCTION_MANAGER.attackAura;
            RotationController rotationController = Manager.ROTATION;
            if (attackAura.state && attackAura.target != null && attackAura.correction.get() || rotationController.isControlling()) {
                rotationVector = Vec3d.fromPolar(Manager.ROTATION.getPitch(), Manager.ROTATION.getYaw());
            } else {
                rotationVector = shooter.getRotationVecClient();
            }

            double speedXZ = 1.5;
            double speedY = 1.5;

            if (superFirework.state) {
                float yaw = (attackAura.state && attackAura.target != null) || (rotationController.isControlling()) ? (Manager.ROTATION.getYaw() % 360f) : (mc.player.getYaw() % 360f);

                boolean isDiagonal = false;
                boolean nearPlayer = false;
                if (yaw < 0) yaw += 360f;

                if (superFirework.mode.is("ReallyWorld")) {
                    float[] diagonals = new float[]{45, 135, 225, 315};
                    float closestDiff = 180f;
                    for (float d : diagonals) {
                        float diff = Math.abs(yaw - d);
                        diff = Math.min(diff, 360 - diff);
                        if (diff < closestDiff) closestDiff = diff;
                    }
                    if (closestDiff <= superFirework.diag1) {
                        speedXZ = superFirework.speedD_1;
                    } else if (closestDiff <= superFirework.diag2) {
                        speedXZ = superFirework.speedD_2;
                    } else if (closestDiff <= superFirework.diag3) {
                        speedXZ = superFirework.speedD_3;
                    } else if (closestDiff <= superFirework.diag4) {
                        speedXZ = superFirework.speedD_4;
                    } else if (closestDiff <= superFirework.diag5) {
                        speedXZ = superFirework.speedD_5;
                    } else if (closestDiff <= superFirework.diag6) {
                        speedXZ = superFirework.speedD_6;
                    } else if (closestDiff <= superFirework.diag7) {
                        speedXZ = superFirework.speedD_7;
                    } else if (closestDiff <= superFirework.diag8) {
                        speedXZ = superFirework.speedD_8;
                    } else if (closestDiff <= superFirework.diag9) {
                        speedXZ = superFirework.speedD_9;
                    } else {
                        speedXZ = superFirework.speedXZ;
                        speedY = superFirework.speedY;
                    }
                }

                if (superFirework.mode.is("BravoHvH")) {
                    for (float d : new float[]{45, 135, 225, 315}) {
                        float diff = Math.abs(yaw - d);
                        diff = Math.min(diff, 360 - diff);
                        if (diff <= 16) { isDiagonal = true; break; }
                    }
                    if (superFirework.nearBoost.get()) {
                        for (PlayerEntity p : Manager.SYNC_MANAGER.getPlayers()) {
                            if (p == mc.player) continue;
                            if (p.distanceTo(mc.player) <= 4) { nearPlayer = true; break; }
                        }
                    }
                    if (isDiagonal) {
                        speedXZ = 1.963;
                    } else if (superFirework.nearBoost.get() && nearPlayer) {
                        speedXZ = 1.82;
                        speedY = 1.67;
                    } else {
                        speedXZ = 1.675;
                        speedY = 1.66;
                    }
                }

                if (superFirework.mode.is("PulseHVH")) {
                    for (float d : new float[]{45, 135, 225, 315}) {
                        float diff = Math.abs(yaw - d);
                        diff = Math.min(diff, 360 - diff);
                        if (diff <= 16) { isDiagonal = true; break; }
                    }
                    if (superFirework.nearBoost.get()) {
                        for (PlayerEntity p : Manager.SYNC_MANAGER.getPlayers()) {
                            if (p == mc.player) continue;
                            if (p.distanceTo(mc.player) <= 5) { nearPlayer = true; break; }
                        }
                    }
                    if (isDiagonal) {
                        speedXZ = 1.963;
                    } else if (superFirework.nearBoost.get() && nearPlayer) {
                        speedXZ = 1.82;
                        speedY = 1.67;
                    } else {
                        speedXZ = 1.675;
                        speedY = 1.66;
                    }
                }

                if (superFirework.mode.is("Custom")) {
                    for (float d : new float[]{45, 135, 225, 315}) {
                        float diff = Math.abs(yaw - d);
                        diff = Math.min(diff, 360 - diff);
                        if (diff <= 16) { isDiagonal = true; break; }
                    }
                    if (superFirework.nearBoost.get()) {
                        for (PlayerEntity p : Manager.SYNC_MANAGER.getPlayers()) {
                            if (p == mc.player) continue;
                            if (p.distanceTo(mc.player) <= 5) { nearPlayer = true; break; }
                        }
                    }
                    if (isDiagonal) {
                        speedXZ = superFirework.speed.get().floatValue();
                    } else if (superFirework.nearBoost.get() && nearPlayer) {
                        speedXZ = superFirework.speed.get().floatValue() - 0.100f;
                        speedY = 1.67;
                    } else {
                        speedXZ = 1.675;
                        speedY = 1.66;
                    }
                }
            }

            Vec3d vec3d = shooter.getVelocity();
            shooter.setVelocity(vec3d.add(rotationVector.x * 0.1 + (rotationVector.x * speedXZ - vec3d.x) * 0.5, rotationVector.y * 0.1 + (rotationVector.y * speedY - vec3d.y) * 0.5, rotationVector.z * 0.1 + (rotationVector.z * speedXZ - vec3d.z) * 0.5));
        } else {
            shooter.setVelocity(originalVelocity);
        }
    }
}
