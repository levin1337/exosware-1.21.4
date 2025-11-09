package ru.levin.events.impl.render;

import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import ru.levin.events.Event;

@SuppressWarnings("All")
public class EventRender3D extends Event {
    private RenderTickCounter deltatick;
    private MatrixStack matrixStack;

    public EventRender3D(MatrixStack matrixStack, RenderTickCounter deltatick) {
        this.matrixStack = matrixStack;
        this.deltatick = deltatick;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }
    public RenderTickCounter getDeltatick() {
        return deltatick;
    }
}
