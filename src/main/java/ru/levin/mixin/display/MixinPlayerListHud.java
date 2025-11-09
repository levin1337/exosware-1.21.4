package ru.levin.mixin.display;

import net.minecraft.client.gui.hud.PlayerListHud;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Nullables;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ru.levin.manager.Manager;

import java.util.Comparator;
import java.util.List;

import static ru.levin.manager.IMinecraft.mc;

@Mixin(PlayerListHud.class)
public class MixinPlayerListHud {
    private static final Comparator<PlayerListEntry> ENTRY_ORDERING = Comparator.<PlayerListEntry>comparingInt(entry -> entry.getGameMode() == GameMode.SPECTATOR ? 1 : 0).thenComparing(entry -> Nullables.mapOrElse(entry.getScoreboardTeam(), Team::getName, "")).thenComparing(entry -> entry.getProfile().getName(), String::compareToIgnoreCase);

    @Inject(method = "collectPlayerEntries", at = @At("HEAD"), cancellable = true)
    private void collectPlayerEntriesHook(CallbackInfoReturnable<List<PlayerListEntry>> cir) {
        if (mc.player == null || mc.player.networkHandler == null) return;
        int limit = Manager.FUNCTION_MANAGER.extraTab.state ? 200 : 80;
        List<PlayerListEntry> list = new java.util.ArrayList<>(mc.player.networkHandler.getListedPlayerListEntries());
        list.sort(ENTRY_ORDERING);
        if (list.size() > limit) {
            list = list.subList(0, limit);
        }
        cir.setReturnValue(list);
    }
}
