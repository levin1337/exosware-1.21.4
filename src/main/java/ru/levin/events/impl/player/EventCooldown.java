package ru.levin.events.impl.player;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.item.Item;
import ru.levin.events.Event;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventCooldown extends Event {
    public Item itemStack;
    public float cooldown;

    public EventCooldown(Item item) {
        this.itemStack = item;
    }
}
