package ru.levin.events.impl.render;

import lombok.Getter;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import ru.levin.events.Event;

@Getter
public class EventHeldItemRenderer extends Event {
    private final Hand hand;
    private final ItemStack item;
    private final float ep;
    private final MatrixStack stack;

    public EventHeldItemRenderer(Hand hand, ItemStack item, float equipProgress, MatrixStack stack) {
        this.hand = hand;
        this.item = item;
        this.ep = equipProgress;
        this.stack = stack;
    }

}