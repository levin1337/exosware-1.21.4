package ru.levin.mixin.player;

import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.levin.events.Event;
import ru.levin.events.impl.player.EventPlayerTravel;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.mixin.iface.MixinEntityAccessor;
import ru.levin.modules.combat.AttackAura;
import ru.levin.modules.combat.rotation.RotationController;

@Mixin(PlayerEntity.class)
public class MixinPlayerEntity implements IMinecraft {

    @Redirect(method = "travel", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getRotationVector()Lnet/minecraft/util/math/Vec3d;"))
    private Vec3d redirectGetRotationVector(PlayerEntity instance) {
        AttackAura attackAura = Manager.FUNCTION_MANAGER.attackAura;
        RotationController rotationController = Manager.ROTATION;
        if (attackAura.state && attackAura.target != null && attackAura.correction.get() || rotationController.isControlling() && instance == mc.player) {
            float pitch = rotationController.getPitch();
            float yaw = rotationController.getYaw();
            return ((MixinEntityAccessor) instance).invokeGetRotationVector(pitch, yaw);
        }

        return ((MixinEntityAccessor) instance).invokeGetRotationVector(instance.getPitch(), instance.getYaw());
    }

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void onTravelhookPre(Vec3d movementInput, CallbackInfo ci) {
        if (mc.player == null)
            return;

        final EventPlayerTravel event = new EventPlayerTravel(movementInput, true);
        Event.call(event);
        if (event.isCancel()) {
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
            ci.cancel();
        }
    }

    @Inject(method = "travel", at = @At("RETURN"), cancellable = true)
    private void onTravelhookPost(Vec3d movementInput, CallbackInfo ci) {
        if (mc.player == null)
            return;
        final EventPlayerTravel event = new EventPlayerTravel(movementInput, false);
        Event.call(event);
        if (event.isCancel()) {
            mc.player.move(MovementType.SELF, mc.player.getVelocity());
            ci.cancel();
        }
    }
}