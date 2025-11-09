package ru.levin.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.levin.manager.Manager;

@Mixin(AbstractBoatEntity.class)
public class MixinAbstractBoatEntity {

    @Unique
    private float prevYaw, prevHeadYaw;

    @Inject(method = "updatePassengerPosition", at = @At("HEAD"))
    protected void updatePassengerPositionHookPre(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo ci) {
    //    if(Manager.FUNCTION_MANAGER.ktLeave.state) {
            prevYaw = passenger.getYaw();
            prevHeadYaw = passenger.getHeadYaw();
       // }
    }

    @Inject(method = "updatePassengerPosition", at = @At("RETURN"))
    protected void updatePassengerPositionHookPost(Entity passenger, Entity.PositionUpdater positionUpdater, CallbackInfo ci) {
       // if(Manager.FUNCTION_MANAGER.ktLeave.state) {
            passenger.setYaw(prevYaw);
            passenger.setHeadYaw(prevHeadYaw);
        //}
    }
}
