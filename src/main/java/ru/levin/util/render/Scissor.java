package ru.levin.util.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import ru.levin.manager.IMinecraft;

import java.awt.Rectangle;
import java.util.List;

import static ru.levin.manager.IMinecraft.mc;

public final class Scissor {
    private static final class State implements IMinecraft {
        boolean enabled;
        int transX, transY;
        int x, y, width, height;

        State() {}

        State(State other) {
            this.enabled = other.enabled;
            this.transX = other.transX;
            this.transY = other.transY;
            this.x = other.x;
            this.y = other.y;
            this.width = other.width;
            this.height = other.height;
        }
    }

    private static State state = new State();
    private static final List<State> stateStack = Lists.newArrayListWithCapacity(8);

    public static void push() {
        stateStack.add(new State(state));
        updateScissor();
    }

    public static void pop() {
        if (!stateStack.isEmpty()) {
            state = stateStack.remove(stateStack.size() - 1);
            updateScissor();
        }
    }

    public static void unset() {
        RenderSystem.disableScissor();
        state.enabled = false;
    }

    public static void setFromComponentCoordinates(double x, double y, double width, double height) {
        double scale = mc.getWindow().getScaleFactor();
        set((int)(x * scale), mc.getWindow().getHeight() - (int)((y + height) * scale), (int)(width * scale), (int)(height * scale));
    }

    public static void set(int x, int y, int width, int height) {
        Rectangle current = state.enabled ? new Rectangle(state.x, state.y, state.width, state.height) : new Rectangle(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());

        Rectangle target = new Rectangle(x + state.transX, y + state.transY, width, height);
        Rectangle result = current.intersection(target);

        state.enabled = true;
        state.x = result.x;
        state.y = result.y;
        state.width = Math.max(result.width, 0);
        state.height = Math.max(result.height, 0);

        RenderSystem.enableScissor(state.x, state.y, state.width, state.height);
    }

    public static void translate(int x, int y) {
        state.transX = x;
        state.transY = y;
    }

    private static void updateScissor() {
        if (state.enabled) {
            RenderSystem.enableScissor(state.x, state.y, state.width, state.height);
        } else {
            RenderSystem.disableScissor();
        }
    }
}