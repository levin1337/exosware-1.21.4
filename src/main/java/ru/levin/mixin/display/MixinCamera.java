package ru.levin.mixin.display;

import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.modules.movement.freelook.CameraOverriddenEntity;
import ru.levin.modules.movement.freelook.FreeLookState;

@Mixin(Camera.class)
public abstract class MixinCamera implements IMinecraft {
    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Unique
    private boolean initialized = false;
    @Shadow
    private boolean thirdPerson;
    @Inject(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V", shift = At.Shift.AFTER))
    private void onUpdate(CallbackInfo ci) {
        if (!FreeLookState.active || !(mc.player instanceof CameraOverriddenEntity entity))
            return;

        if (!initialized) {
            entity.setCameraPitch(mc.player.getPitch());
            entity.setCameraYaw(mc.player.getYaw());
            initialized = true;
        }

        setRotation(entity.getCameraYaw(), entity.getCameraPitch());
    }

    @Inject(method = "update", at = @At("TAIL"))
    private void updateHook(BlockView area, Entity focusedEntity, boolean thirdPerson, boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (Manager.FUNCTION_MANAGER.freeCamera.state) {
            this.thirdPerson = true;
        }
    }
    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setRotation(FF)V"))
    private void setRotationHook(Args args) {
        if(Manager.FUNCTION_MANAGER.freeCamera.state)
            args.setAll(Manager.FUNCTION_MANAGER.freeCamera.getFakeYaw(), Manager.FUNCTION_MANAGER.freeCamera.getFakePitch());
    }

    @ModifyArgs(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;setPos(DDD)V"))
    private void setPosHook(Args args) {
        if(Manager.FUNCTION_MANAGER.freeCamera.state)
            args.setAll(Manager.FUNCTION_MANAGER.freeCamera.getFakeX(), Manager.FUNCTION_MANAGER.freeCamera.getFakeY(), Manager.FUNCTION_MANAGER.freeCamera.getFakeZ());
    }
}