package ru.levin.mixin.world;

import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.ItemEntityRenderer;
import net.minecraft.client.render.entity.state.ItemEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.ItemEntity;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.modules.render.ItemPhysic;

import static ru.levin.manager.IMinecraft.mc;

@Mixin(ItemEntityRenderer.class)
public abstract class MixinItemEntityRenderer implements IMinecraft {

    @Unique
    private boolean isOnGround = false;

    @Unique
    private float entityAge = 0;

    @Unique
    private float uniqueOffset = 0;

    @Inject(method = "updateRenderState", at = @At("HEAD"))
    private void onUpdateRenderState(ItemEntity entity, ItemEntityRenderState state, float tickDelta, CallbackInfo ci) {
        isOnGround = entity.isOnGround();
        entityAge = state.age;
        uniqueOffset = state.uniqueOffset;
    }

    @ModifyVariable(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/ItemEntityRenderer;renderStack(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/ItemStackEntityRenderState;Lnet/minecraft/util/math/random/Random;)V"), ordinal = 0)
    private MatrixStack modifyMatrixStack(MatrixStack matrices, ItemEntityRenderState state, MatrixStack original, VertexConsumerProvider vertexConsumers, int light) {
        ItemPhysic itemPhysic = Manager.FUNCTION_MANAGER.itemPhysic;
        if (itemPhysic.state) {
            if (itemPhysic.mode.is("Обычная")) {
                matrices.pop();
                matrices.push();
                float rotation = ItemEntity.getRotation(entityAge, uniqueOffset);
                if (isOnGround) {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
                } else {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rotation * 300));
                }
            } else if (itemPhysic.mode.is("2D")) {
                matrices.pop();
                matrices.push();
                matrices.translate(0.0F, 0.10F, 0.0F);

                matrices.multiply(mc.getEntityRenderDispatcher().getRotation());
                matrices.scale(1.1F, 1.1F, 0.0F);
                state.itemRenderState.render(matrices, vertexConsumers, 0xF000F0, OverlayTexture.DEFAULT_UV);
            }
        }
        return matrices;
    }
}