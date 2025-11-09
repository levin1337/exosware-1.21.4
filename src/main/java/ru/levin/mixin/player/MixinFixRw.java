package ru.levin.mixin.player;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Scoreboard.class)
public class MixinFixRw {
    @Inject(method = "removeScoreHolderFromTeam", at = @At("HEAD"),cancellable = true)
    public void removeScoreHolderFromTeam(String scoreHolderName, Team team, CallbackInfo ci) {
        ci.cancel();
    }
}