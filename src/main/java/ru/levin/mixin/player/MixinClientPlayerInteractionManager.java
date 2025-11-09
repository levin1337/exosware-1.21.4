package ru.levin.mixin.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import ru.levin.manager.Manager;
import ru.levin.util.move.MoveUtil;

@SuppressWarnings("All")
@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Shadow
    @Final
    private MinecraftClient client;

    @Redirect(method = "clickSlot", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"))
    private void redirectSendPacket(ClientPlayNetworkHandler networkHandler, Packet<?> packet) {
        if (packet instanceof ClickSlotC2SPacket clickPacket) {
            if (Manager.FUNCTION_MANAGER.guiWalk.state && Manager.FUNCTION_MANAGER.guiWalk.bypass.is("FunTime")  && clickPacket.getSyncId() == 0 && MoveUtil.isMoving() && client.currentScreen instanceof HandledScreen<?>) {
                Manager.FUNCTION_MANAGER.guiWalk.queuePacket(clickPacket);
                return;
            }
        }
        networkHandler.sendPacket(packet);
    }
}