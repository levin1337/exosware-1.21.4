package ru.levin.mixin.iface;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface MixinEntityAccessor {
    @Invoker("getRotationVector")
    Vec3d invokeGetRotationVector(float pitch, float yaw);
}