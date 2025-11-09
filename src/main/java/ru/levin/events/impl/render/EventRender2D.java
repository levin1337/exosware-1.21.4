package ru.levin.events.impl.render;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.math.MatrixStack;
import ru.levin.events.Event;

@SuppressWarnings("All")
public class EventRender2D extends Event {
    private DrawContext drawContext;
    private MatrixStack matrixStack;
    private RenderTickCounter deltatick;

    public EventRender2D(DrawContext drawContext, MatrixStack matrixStack, RenderTickCounter deltatick) {
        this.drawContext = drawContext;
        this.matrixStack = matrixStack;
        this.deltatick = deltatick;
    }

    public MatrixStack getMatrixStack() {
        return matrixStack;
    }

    public DrawContext getDrawContext() {
        return drawContext;
    }

    public RenderTickCounter getDeltatick() {
        return deltatick;
    }
}
