package ru.levin.mixin.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.levin.events.Event;
import ru.levin.events.impl.render.EventPlayerRender;
import ru.levin.manager.IMinecraft;

@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRenderer<T extends LivingEntity, S extends LivingEntityRenderState> implements IMinecraft {
    @Unique
    private float originalPrevHeadYaw, originalHeadYaw, originalPrevHeadPitch, originalHeadPitch, originalBodyYaw, originalPrevBodyYaw;
    @Unique
    private boolean replaced;

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
    private void onUpdateRenderStatePre(T livingEntity, S state, float tickDelta, CallbackInfo ci) {
        if (mc == null || mc.player == null || livingEntity != mc.player) return;

        if (mc.currentScreen instanceof InventoryScreen) return;

        EventPlayerRender playerRender = new EventPlayerRender(livingEntity);
        Event.call(playerRender);

        originalPrevHeadYaw = livingEntity.prevHeadYaw;
        originalHeadYaw = livingEntity.headYaw;
        originalPrevHeadPitch = livingEntity.prevPitch;
        originalHeadPitch = livingEntity.getPitch();
        originalBodyYaw = livingEntity.bodyYaw;
        originalPrevBodyYaw = livingEntity.prevBodyYaw;

        livingEntity.prevHeadYaw = playerRender.getPrevYaw();
        livingEntity.headYaw = playerRender.getYaw();
        livingEntity.prevPitch = playerRender.getPrevPitch();
        livingEntity.setPitch(playerRender.getPitch());
        livingEntity.prevBodyYaw = playerRender.getPrevBodyYaw();
        livingEntity.bodyYaw = playerRender.getBodyYaw();

        replaced = true;
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/client/render/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void onUpdateRenderStatePost(T livingEntity, S state, float tickDelta, CallbackInfo ci) {
        if (!replaced || mc == null || mc.player == null || livingEntity != mc.player) return;

        livingEntity.prevHeadYaw = originalPrevHeadYaw;
        livingEntity.headYaw = originalHeadYaw;
        livingEntity.prevPitch = originalPrevHeadPitch;
        livingEntity.setPitch(originalHeadPitch);
        livingEntity.prevBodyYaw = originalPrevBodyYaw;
        livingEntity.bodyYaw = originalBodyYaw;

        replaced = false;
    }
}
