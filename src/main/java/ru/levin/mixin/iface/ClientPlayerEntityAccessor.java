package ru.levin.mixin.iface;

import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientPlayerEntity.class)
public interface ClientPlayerEntityAccessor {
    @Accessor("lastYaw")
    float getLastYaw();

    @Accessor("lastPitch")
    float getLastPitch();

    @Invoker("isBlind")
    boolean invokeIsBlind();

    @Invoker("isWalking")
    boolean invokeIsWalking();

    @Invoker("canSprint")
    boolean invokeCanSprint();

    @Accessor("lastSprinting")
    boolean getLastSprinting();
}
