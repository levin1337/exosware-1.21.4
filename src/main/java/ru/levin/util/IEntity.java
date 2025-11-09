package ru.levin.util;

import net.minecraft.util.math.Vec3d;
import ru.levin.modules.render.Trails;

import java.util.List;

public interface IEntity {
    List<Trails.Trail> exosWareFabric1_21_4$getTrails();
    Vec3d exosWareFabric1_21_4$getLastTrailPos();
    void exosWareFabric1_21_4$setLastTrailPos(Vec3d pos);
}
