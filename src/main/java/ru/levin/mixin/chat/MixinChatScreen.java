package ru.levin.mixin.chat;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;

@Mixin(ChatScreen.class)
public abstract class MixinChatScreen implements IMinecraft {

    @Inject(method = "render", at = {@At("HEAD")}, cancellable = true)
    public void render(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Manager.DRAG_MANAGER.draggables.values().forEach((dragging) -> {
            if (dragging.getModule() != null && dragging.getModule().state) {
                dragging.onDraw(mouseX, mouseY, mc.getWindow());
                dragging.renderGuides(mc.getWindow());
            }
        });
    }

    @Inject(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/ChatInputSuggestor;mouseClicked(DDI)Z", shift = At.Shift.AFTER), cancellable = true)
    private void afterSuggestionsClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) {
            Manager.DRAG_MANAGER.draggables.values().forEach(dragging -> {
                if (dragging.getModule() != null && dragging.getModule().state) {
                    dragging.onClick(mouseX, mouseY, button);
                }
            });
        }
    }
}