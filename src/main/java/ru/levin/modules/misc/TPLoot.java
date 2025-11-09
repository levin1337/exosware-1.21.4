package ru.levin.modules.misc;

import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.*;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.TextSetting;
import ru.levin.util.player.TimerUtil;

@FunctionAnnotation(name = "TPLoot", desc = "Телепортирует на место, где лежат предметы", type = Type.Misc)
public class TPLoot extends Function {
    private final ModeSetting lootEnd = new ModeSetting("После лута", "Ничего", "Ничего", "/hub", "/spawn", "home", "Своя");
    private final TextSetting text = new TextSetting("Команда", "/home home", () -> lootEnd.is("home"));
    private final TextSetting custom = new TextSetting("Команда", "/test", () -> lootEnd.is("Своя"));

    private final TimerUtil timerUtil = new TimerUtil();
    private boolean check;

    public TPLoot() {
        addSettings(lootEnd, text, custom);
    }

    private boolean isValuable(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof SwordItem ||
                item instanceof PlayerHeadItem ||
                item instanceof ArmorItem ||
                item == Items.TOTEM_OF_UNDYING ||
                item == Items.ENDER_PEARL ||
                item == Items.END_CRYSTAL ||
                item == Items.FIREWORK_ROCKET ||
                item == Items.ELYTRA ||
                item == Items.GOLDEN_APPLE ||
                item == Items.ENCHANTED_GOLDEN_APPLE ||
                item == Items.CLAY_BALL ||
                item == Items.MAGMA_CREAM ||
                item == Items.CRYING_OBSIDIAN;
    }

    private boolean isHotbar() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isValuable(stack)) return true;
        }
        return false;
    }

    private boolean isInv() {
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (isValuable(stack)) return true;
        }
        return false;
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate)) return;

        for (Entity entity : Manager.SYNC_MANAGER.getEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;

            ItemStack stack = itemEntity.getStack();
            if (!isValuable(stack)) continue;

            double itemY = itemEntity.getY();
            BlockPos blockBelow = itemEntity.getBlockPos().down();

            if (!mc.world.getBlockState(blockBelow).isAir() && itemY - blockBelow.getY() <= 1.0) {
                double x = itemEntity.getX() + 0.5;
                double y = itemEntity.getY();
                double z = itemEntity.getZ() + 0.5;
                mc.player.networkHandler.sendPacket(
                        new PlayerMoveC2SPacket.Full(x, y, z, mc.player.getYaw(), mc.player.getPitch(), false, true)
                );

                check = true;
            }
        }

        if (check && timerUtil.hasTimeElapsed(100)) {
            if (isHotbar() || isInv()) {
                switch (lootEnd.get()) {
                    case "/hub" -> mc.player.networkHandler.sendChatMessage("/hub");
                    case "/spawn" -> mc.player.networkHandler.sendChatMessage("/spawn");
                    case "home" -> mc.player.networkHandler.sendChatMessage(text.getValue());
                    case "Своя" -> mc.player.networkHandler.sendChatMessage(custom.getValue());
                }
                check = false;
                timerUtil.reset();
            }
        }
    }
}
