package ru.levin.manager;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.Tessellator;

@SuppressWarnings("All")
public interface IMinecraft {
    MinecraftClient mc = MinecraftClient.getInstance();

    static RenderTickCounter tickCounter() {
        return Holder.tickCounter;
    }
    static Tessellator tessellator() {
        return Holder.tessellator;
    }
    static MinecraftClient getMc() {
        return Holder.minecraftClient;
    }
    class Holder {
        private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();
        private static final RenderTickCounter tickCounter = mc.getRenderTickCounter();
        private static final Tessellator tessellator = Tessellator.getInstance();
    }
}
