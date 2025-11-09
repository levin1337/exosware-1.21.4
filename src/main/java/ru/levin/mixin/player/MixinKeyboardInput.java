package ru.levin.mixin.player;

import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.levin.events.Event;
import ru.levin.events.impl.input.EventKeyBoard;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends Input {
    @Shadow
    @Final
    private GameOptions settings;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        boolean forwardKey = this.settings.forwardKey.isPressed();
        boolean backwardKey = this.settings.backKey.isPressed();
        boolean leftKey = this.settings.leftKey.isPressed();
        boolean rightKey = this.settings.rightKey.isPressed();
        boolean jumpKey = this.settings.jumpKey.isPressed();
        boolean sneakKey = this.settings.sneakKey.isPressed();
        boolean sprintKey = this.settings.sprintKey.isPressed();

        float movementForward = calculateMovement(forwardKey, backwardKey);
        float movementSideways = calculateMovement(leftKey, rightKey);
        EventKeyBoard event = new EventKeyBoard(movementForward, movementSideways, jumpKey, sneakKey, sprintKey);
        Event.call(event);

        this.movementForward = event.getMovementForward();
        this.movementSideways = event.getMovementStrafe();

        this.playerInput = new PlayerInput(event.getMovementForward() > 0, event.getMovementForward() < 0, event.getMovementStrafe() > 0, event.getMovementStrafe() < 0, event.isJump(), event.isSneak(), event.isSprint());

        ci.cancel();
    }

    private float calculateMovement(boolean positive, boolean negative) {
        if (positive == negative) {
            return 0.0F;
        }
        return positive ? 1.0F : -1.0F;
    }
}