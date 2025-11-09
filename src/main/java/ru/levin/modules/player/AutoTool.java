package ru.levin.modules.player;

import net.minecraft.block.AirBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("All")
@FunctionAnnotation(name = "AutoTool", desc = "Берёт в руку лучший инструмент для ломания", type = Type.Player)
public class AutoTool extends Function {

    public static int itemIndex;
    private boolean swap;
    private long swapDelay;
    private final List<Integer> lastItem = new ArrayList<>();

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate)) return;
        if (!(mc.crosshairTarget instanceof BlockHitResult)) return;

        BlockHitResult result = (BlockHitResult) mc.crosshairTarget;
        if (result == null) return;

        BlockPos pos = result.getBlockPos();
        if (pos == null || mc.world.getBlockState(pos).isAir()) return;

        int toolSlot = getBest(pos);

        if (toolSlot != -1 && mc.options.attackKey.isPressed()) {
            lastItem.add(mc.player.getInventory().selectedSlot);
            mc.player.getInventory().selectedSlot = toolSlot;
            itemIndex = toolSlot;
            swap = true;
            swapDelay = System.currentTimeMillis();
        } else if (swap && !lastItem.isEmpty() && System.currentTimeMillis() >= swapDelay + 200) {
            mc.player.getInventory().selectedSlot = lastItem.get(0);
            itemIndex = lastItem.get(0);
            lastItem.clear();
            swap = false;
        }
    }

    public static int getBest(final BlockPos pos) {
        if (pos == null) return -1;

        int index = -1;
        float currentFastest = 1.0f;

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack == ItemStack.EMPTY) continue;
            if (stack.getMaxDamage() - stack.getDamage() <= 10) continue;

            float digSpeed = EnchantmentHelper.getLevel(mc.world.getRegistryManager().getOrThrow(Enchantments.EFFICIENCY.getRegistryRef()).getEntry(Enchantments.EFFICIENCY.getValue()).get(), stack);
            float destroySpeed = stack.getMiningSpeedMultiplier(mc.world.getBlockState(pos));

            if (mc.world.getBlockState(pos).getBlock() instanceof AirBlock) continue;

            if (digSpeed + destroySpeed > currentFastest) {
                currentFastest = digSpeed + destroySpeed;
                index = i;
            }
        }

        return index;
    }
}
