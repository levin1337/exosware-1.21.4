package ru.levin.mixin.display;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.Fog;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.levin.events.Event;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.modules.combat.AttackAura;
import ru.levin.modules.combat.CrystalAura;
import ru.levin.modules.combat.SelfTrap;
import ru.levin.modules.combat.rotation.RotationController;
import ru.levin.modules.render.AspectRatio;
import ru.levin.util.math.RayTraceUtil;
import ru.levin.util.render.Render3DUtil;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.vector.VectorUtil;

import java.util.function.Predicate;

@Mixin(GameRenderer.class)
public abstract class MixinGameRenderer implements IMinecraft {

    @Shadow public abstract float getViewDistance();

    @Inject(method = "renderWorld", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0))
    private void renderWorld(RenderTickCounter tickCounter, CallbackInfo callbackInfo, @Local(ordinal = 2) Matrix4f matrix4f) {
        MatrixStack matrixStack = new MatrixStack();
        matrixStack.multiplyPositionMatrix(matrix4f);
        matrixStack.translate(mc.getEntityRenderDispatcher().camera.getPos().negate());
        VectorUtil.previousProjectionMatrix = RenderSystem.getProjectionMatrix();
        VectorUtil.lastWorldSpaceMatrix = matrixStack.peek();

        Render3DUtil.setLastProjMat(RenderSystem.getProjectionMatrix());
        Render3DUtil.setLastWorldSpaceMatrix(matrixStack.peek());
        EventRender3D event = new EventRender3D(matrixStack, tickCounter);
        Event.call(event);
        Render3DUtil.onWorldRender(event);
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/GameRenderer;renderHand:Z", opcode = Opcodes.GETFIELD, ordinal = 0), method = "renderWorld")
    private void render3dHook(RenderTickCounter tickCounter, CallbackInfo ci) {
        MatrixStack matrixStack = new MatrixStack();
        Camera camera = mc.gameRenderer.getCamera();
        RenderSystem.getModelViewStack().pushMatrix().mul(matrixStack.peek().getPositionMatrix());
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0f));
        RenderSystem.setShaderFog(Fog.DUMMY);
        RenderUtil.render3D.setTranslation(matrixStack);
        Event.call(new EventRender3D(matrixStack, tickCounter));
        RenderSystem.getModelViewStack().popMatrix();
        RenderSystem.getModelViewMatrix();
    }

    @Inject(method = "getBasicProjectionMatrix", at = @At("TAIL"), cancellable = true)
    private void getBasicProjectionMatrixHook(float fov, CallbackInfoReturnable<Matrix4f> cir) {
        AspectRatio aspectRatio = Manager.FUNCTION_MANAGER.aspectRatio;
        if (aspectRatio.state) {
            float aspect = 1.0f;
            String mode = aspectRatio.mods.get();

            switch (mode) {
                case "4:3":
                    aspect = 4f / 3f;
                    break;
                case "16:9":
                    aspect = 16f / 9f;
                    break;
                case "1:1":
                    aspect = 1f;
                    break;
                case "16:10":
                    aspect = 16f / 10f;
                    break;
                case "Кастомный":
                    aspect = aspectRatio.slider.get().floatValue();
                    break;
            }

            MatrixStack matrixStack = new MatrixStack();
            matrixStack.peek().getPositionMatrix().identity();
            matrixStack.peek().getPositionMatrix().mul(new Matrix4f().setPerspective((float) (fov * (Math.PI / 180.0)), aspect, 0.05f, getViewDistance() * 4.0f));

            cir.setReturnValue(matrixStack.peek().getPositionMatrix());
        }
    }


    @Inject(method = "findCrosshairTarget", at = @At("HEAD"), cancellable = true)
    private void onFindCrosshairTarget(Entity camera, double blockRange, double entityRange, float tickDelta, CallbackInfoReturnable<HitResult> cir) {
        AttackAura attackAura = Manager.FUNCTION_MANAGER.attackAura;
        CrystalAura crystalAura = Manager.FUNCTION_MANAGER.crystalAura;
        SelfTrap selfTrap = Manager.FUNCTION_MANAGER.selfTrap;
        RotationController rotation = Manager.ROTATION;

        float yaw;
        float pitch;

        if (attackAura.state || rotation.isControlling()) {
            yaw = rotation.getYaw();
            pitch = rotation.getPitch();
        } else if (crystalAura.state && crystalAura.rotate != null && crystalAura.closestCrystal != null) {
            yaw = crystalAura.rotate.x;
            pitch = crystalAura.rotate.y;
        } else if (selfTrap.state && selfTrap.active) {
            yaw = selfTrap.rotate.x;
            pitch = crystalAura.rotate.y;
        } else {
            yaw = mc.player.getYaw();
            pitch = mc.player.getPitch();
        }

        double maxRange = Math.max(blockRange, entityRange);
        Vec3d cameraPos = camera.getCameraPosVec(tickDelta);

        EntityHitResult entityHit = RayTraceUtil.rayCastEntity(maxRange, yaw, pitch, e -> !e.isSpectator() && e.canHit());
        BlockHitResult blockHit = RayTraceUtil.rayCast(maxRange, yaw, pitch, false);
        if (entityHit != null) {
            double entityDistSq = entityHit.getPos().squaredDistanceTo(cameraPos);
            double blockDistSq = blockHit != null ? blockHit.getPos().squaredDistanceTo(cameraPos) : Double.MAX_VALUE;

            if (entityDistSq <= entityRange * entityRange && entityDistSq < blockDistSq) {
                cir.setReturnValue(entityHit);
                return;
            }
        }

        if (blockHit != null && blockHit.getPos().squaredDistanceTo(cameraPos) <= blockRange * blockRange) {
            cir.setReturnValue(blockHit);
        } else {
            cir.setReturnValue(BlockHitResult.createMissed(cameraPos, null, null));
        }
    }


    @Redirect(method = "findCrosshairTarget", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileUtil;raycast(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/math/Box;Ljava/util/function/Predicate;D)Lnet/minecraft/util/hit/EntityHitResult;"))
    private EntityHitResult findCrosshairTarget(Entity entity, Vec3d start, Vec3d end, Box box, Predicate<Entity> predicate, double maxDistance) {
        if (!Manager.FUNCTION_MANAGER.noRayTrace.state) {
            return ProjectileUtil.raycast(entity, start, end, box, predicate, maxDistance);
        }
        return null;
    }


    @Inject(method = "tiltViewWhenHurt", at = @At("HEAD"), cancellable = true)
    private void tiltViewWhenHurtHook(MatrixStack matrices, float tickDelta, CallbackInfo ci) {
        if (Manager.FUNCTION_MANAGER.noRender.state && Manager.FUNCTION_MANAGER.noRender.mods.get("Тряска камеры"))
            ci.cancel();
    }
    @Redirect(method = "renderWorld", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;lerp(FFF)F"))
    private float badEffects(float delta, float first, float second) {
        if (Manager.FUNCTION_MANAGER.noRender.state && Manager.FUNCTION_MANAGER.noRender.mods.get("Плохие эффекты")) return 0;
        return MathHelper.lerp(delta, first, second);
    }
}