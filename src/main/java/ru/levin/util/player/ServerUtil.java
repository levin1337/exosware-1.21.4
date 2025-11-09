package ru.levin.util.player;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import ru.levin.manager.IMinecraft;

import java.util.Locale;

public class ServerUtil implements IMinecraft {
    public static void selectCompass() {
        int slot = InventoryUtil.getHotBarSlot(Items.COMPASS);
        if (slot == -1) {
            return;
        }

        mc.player.getInventory().selectedSlot = slot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
    }
    public static float getHealth(LivingEntity target) {
        if (mc.getCurrentServerEntry() == null) {
            return target.getHealth() / target.getMaxHealth();
        }

        String serverAddress = mc.getCurrentServerEntry().address.toLowerCase(Locale.ROOT);
        boolean isLocal = mc.isConnectedToLocalServer();

        if (isLocal || serverAddress.isEmpty()) {
            return target.getHealth() / target.getMaxHealth();
        }

        if (target instanceof MobEntity) {
            return target.getHealth() / target.getMaxHealth();
        }

        if (serverAddress.contains("reallyworld") || serverAddress.contains("playrw") || serverAddress.contains("saturn-x") || serverAddress.contains("skytime") || serverAddress.contains("space-times")) {
            Scoreboard scoreboard = target.getWorld().getScoreboard();
            ScoreboardObjective scoreObjective = scoreboard.getObjectiveForSlot(ScoreboardDisplaySlot.BELOW_NAME);

            if (scoreObjective != null) {
                try {
                    int hp = scoreboard.getOrCreateScore(ScoreHolder.fromName(target.getNameForScoreboard()), scoreObjective).getScore();
                    if (hp >= 0 && hp <= target.getMaxHealth()) {
                        return (float) hp / target.getMaxHealth();
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return target.getHealth() / target.getMaxHealth();
    }
    public static boolean isConnected(String ip) {
        if (mc.getCurrentServerEntry() == null) return false;
        String serverAddress = mc.getCurrentServerEntry().address;
        return serverAddress != null && serverAddress.contains(ip);
    }

}
