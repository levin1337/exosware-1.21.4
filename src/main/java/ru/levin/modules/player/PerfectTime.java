package ru.levin.modules.player;

import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.TridentItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "PerfectTime", desc = "Автоматически отпускает трезубец или арбалет, когда они полностью натянуты", type = Type.Player)
public class PerfectTime extends Function {

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate) || mc.player == null || !mc.player.isUsingItem()) return;
        ItemStack stack = mc.player.getMainHandStack();
        Item item = stack.getItem();
        int useTime = stack.getMaxUseTime(mc.player) - mc.player.getItemUseTimeLeft();

        if (item instanceof TridentItem && useTime >= TridentItem.MIN_DRAW_DURATION) {
            releaseUse();
        } else if (item instanceof CrossbowItem && useTime >= stack.getMaxUseTime(mc.player) - 1) {
            releaseUse();
        }
    }

    private void releaseUse() {
        mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.DOWN));
        mc.player.stopUsingItem();
    }
}
