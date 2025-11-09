package ru.levin.mixin.util;

import net.minecraft.block.*;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.modules.player.CustomCoolDown;

import static ru.levin.manager.IMinecraft.mc;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {

    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    private void cancelUseItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        CustomCoolDown coolDown = Manager.FUNCTION_MANAGER.customCoolDown;

        if (!coolDown.state) return;
        if (coolDown.PVPonly.get() && !ClientManager.playerIsPVP()) return;

        if (coolDown.isItemEnabled(stack.getItem()) && coolDown.lastUseMap.containsKey(stack.getItem())) {
            cir.setReturnValue(ActionResult.FAIL);
            cir.cancel();
        }
    }

    @Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
    private void interactBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
        Block bs = mc.world.getBlockState(hitResult.getBlockPos()).getBlock();
        if (Manager.FUNCTION_MANAGER.noInteract.state && (
                bs == Blocks.CHEST ||
                        bs == Blocks.TRAPPED_CHEST ||
                        bs == Blocks.FURNACE ||
                        bs == Blocks.ANVIL ||
                        bs == Blocks.CRAFTING_TABLE ||
                        bs == Blocks.HOPPER ||
                        bs == Blocks.JUKEBOX ||
                        bs == Blocks.NOTE_BLOCK ||
                        bs == Blocks.ENDER_CHEST ||
                        bs == Blocks.DISPENSER ||
                        bs == Blocks.DROPPER ||
                        bs == Blocks.LOOM ||
                        bs == Blocks.BEACON ||
                        bs == Blocks.SMITHING_TABLE ||
                        bs instanceof ShulkerBoxBlock ||
                        bs instanceof FenceBlock ||
                        bs instanceof FenceGateBlock ||
                        bs instanceof TrapdoorBlock)
                && (Manager.FUNCTION_MANAGER.attackAura.state || !Manager.FUNCTION_MANAGER.noInteract.onlyAura.get())) {
            cir.setReturnValue(ActionResult.PASS);
        }
    }
}