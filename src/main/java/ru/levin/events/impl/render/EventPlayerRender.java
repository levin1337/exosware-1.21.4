package ru.levin.events.impl.render;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.entity.LivingEntity;
import ru.levin.events.Event;

@EqualsAndHashCode(callSuper = true)
@Data
public class EventPlayerRender extends Event {
    private final LivingEntity livingEntity;

    private float prevYaw;
    private float yaw;
    private float prevPitch;
    private float pitch;
    private float prevBodyYaw;
    private float bodyYaw;

    public EventPlayerRender(LivingEntity entity) {
        this.livingEntity = entity;
        this.yaw = entity.headYaw;
        this.prevYaw = entity.prevHeadYaw;
        this.pitch = entity.getPitch();
        this.prevPitch = entity.prevPitch;
        this.bodyYaw = entity.bodyYaw;
        this.prevBodyYaw = entity.prevBodyYaw;
    }
}
