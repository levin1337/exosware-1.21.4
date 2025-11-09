package ru.levin.mixin.world;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.levin.manager.Manager;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState {

    @Inject(
            method = "getCollisionShape(Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/ShapeContext;)Lnet/minecraft/util/shape/VoxelShape;",
            at = @At("RETURN"),
            cancellable = true
    )
    private void removeXZCollision(BlockView world, BlockPos pos, ShapeContext context, CallbackInfoReturnable<VoxelShape> cir) {
        if (Manager.FUNCTION_MANAGER != null && Manager.FUNCTION_MANAGER.phase.state) {
            VoxelShape original = cir.getReturnValue();
            double minY = original.getMin(Direction.Axis.Y);
            double maxY = original.getMax(Direction.Axis.Y);

            if (minY >= maxY) {
                maxY = minY + 0.001;
            }


            VoxelShape finalShape = VoxelShapes.union(
                    VoxelShapes.cuboid(0.0, minY, 0.0, 0.0, maxY, 0.0)
            );

            cir.setReturnValue(finalShape);
        }
    }
}