package ru.levin.util.vector;

import net.minecraft.entity.Entity;
import org.joml.Vector3d;

import static ru.levin.util.math.MathUtil.interpolate;

public class EntityPosition extends Vector3d {
    protected EntityPosition(Entity entity, float height, float pt) {
        super(interpolate(entity.lastRenderX, entity.getX(), pt), interpolate(entity.lastRenderY, entity.getY(), pt) + height, interpolate(entity.lastRenderZ, entity.getZ(), pt));
    }
    public static Vector3d get(Entity entity, float height, float pt) {
        return new EntityPosition(entity, height, pt);
    }

    public static Vector3d get(Entity entity, float pt) {
        return get(entity, 0, pt);
    }
}
