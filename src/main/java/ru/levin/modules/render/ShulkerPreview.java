package ru.levin.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.registry.RegistryWrapper;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "ShulkerPreview", desc = "Просмотр содержимого шалкеров", type = Type.Render)
public class ShulkerPreview extends Function {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    @Override
    public void onEvent(Event event) {
    }

    private boolean isShulkerBox(ItemStack stack) {
        return stack.isOf(Items.SHULKER_BOX)
                || stack.isOf(Items.WHITE_SHULKER_BOX)
                || stack.isOf(Items.ORANGE_SHULKER_BOX)
                || stack.isOf(Items.MAGENTA_SHULKER_BOX)
                || stack.isOf(Items.LIGHT_BLUE_SHULKER_BOX)
                || stack.isOf(Items.YELLOW_SHULKER_BOX)
                || stack.isOf(Items.LIME_SHULKER_BOX)
                || stack.isOf(Items.PINK_SHULKER_BOX)
                || stack.isOf(Items.GRAY_SHULKER_BOX)
                || stack.isOf(Items.LIGHT_GRAY_SHULKER_BOX)
                || stack.isOf(Items.CYAN_SHULKER_BOX)
                || stack.isOf(Items.PURPLE_SHULKER_BOX)
                || stack.isOf(Items.BLUE_SHULKER_BOX)
                || stack.isOf(Items.BROWN_SHULKER_BOX)
                || stack.isOf(Items.GREEN_SHULKER_BOX)
                || stack.isOf(Items.RED_SHULKER_BOX)
                || stack.isOf(Items.BLACK_SHULKER_BOX);
    }

    private DefaultedList<ItemStack> getItems(ItemStack stack) {
        DefaultedList<ItemStack> list = DefaultedList.ofSize(27, ItemStack.EMPTY);

        RegistryWrapper.WrapperLookup registries = MinecraftClient.getInstance()
                .getNetworkHandler()
                .getRegistryManager();

        NbtElement nbtElement = stack.toNbtAllowEmpty(registries);

        if (!(nbtElement instanceof NbtCompound compound)) return list;

        if (!compound.contains("BlockEntityTag", 10)) return list;

        NbtCompound blockEntityTag = compound.getCompound("BlockEntityTag");

        if (!blockEntityTag.contains("Items", 9)) return list;

        NbtList nbtList = blockEntityTag.getList("Items", 10);

        for (int i = 0; i < nbtList.size(); i++) {
            NbtCompound nbt = nbtList.getCompound(i);
            int slot = nbt.getByte("Slot") & 255;
            if (slot < list.size()) {
                list.set(slot, ItemStack.fromNbtOrEmpty(registries, nbt));
            }
        }

        return list;
    }


    private void drawPreview(DrawContext context, DefaultedList<ItemStack> items, int x, int y) {
        ItemRenderer itemRenderer = mc.getItemRenderer();
        RenderSystem.enableBlend();

        for (int i = 0; i < items.size(); i++) {
            ItemStack s = items.get(i);
            if (s.isEmpty()) continue;

            int row = i / 9;
            int col = i % 9;
            int px = x + col * 18;
            int py = y + row * 18;

            context.drawItem(s, px, py);
        }

        RenderSystem.disableBlend();
    }
}
