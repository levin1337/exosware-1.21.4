package ru.levin.events.impl.player;


import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import ru.levin.events.Event;

public class EventAttack extends Event {
    private final PlayerEntity attacker;
    private final Entity target;

    public EventAttack(PlayerEntity attacker, Entity target) {
        this.attacker = attacker;
        this.target = target;
    }

    public PlayerEntity getAttacker() {
        return attacker;
    }

    public Entity getTarget() {
        return target;
    }
}
