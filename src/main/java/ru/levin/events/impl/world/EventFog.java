package ru.levin.events.impl.world;

import net.minecraft.client.render.FogShape;
import ru.levin.events.Event;

public class EventFog extends Event {
    public boolean modified = false;
    public float r, g, b, alpha;
    public float start, end;
    public FogShape shape;
}
