package ru.levin.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.events.impl.world.EventFog;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.math.MathUtil;
import ru.levin.util.player.TimerUtil;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.render.providers.ResourceProvider;
import ru.levin.util.vector.VectorUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@SuppressWarnings("All")
@FunctionAnnotation(name = "World", desc = "Позволяет менять время суток, погоду и туман", type = Type.Render)
public class World extends Function {

    private final BooleanSetting timeBox = new BooleanSetting("Изменять время", true);
    private final ModeSetting timeMode = new ModeSetting(
            timeBox::get,
            "Время суток",
            "День", "День", "Ночь", "Утро", "Восход", "Кастомное"
    );
    private final SliderSetting customTime = new SliderSetting(
            "Кастомное время", 6000, 0, 24000, 100,
            () -> timeBox.get() && timeMode.is("Кастомное")
    );

    private final BooleanSetting weatherBox = new BooleanSetting("Изменять погоду", true);
    private final ModeSetting weatherMode = new ModeSetting(
            weatherBox::get,
            "Погода",
            "Ясно", "Ясно", "Дождь", "Гроза"
    );

    public final BooleanSetting fog = new BooleanSetting("Туман", false);
    public final SliderSetting fogEnd = new SliderSetting(
            "Дальность тумана", 200, 0, 500, 1, fog::get
    );

    public World() {
        addSettings(timeBox, timeMode, customTime, weatherBox, weatherMode, fog, fogEnd);
    }

    @Override
    public void onEvent(Event event) {
        if (mc.world == null) return;
        if (event instanceof EventPacket packet && timeBox.get()) {
            if (packet.getPacket() instanceof WorldTimeUpdateS2CPacket) {
                packet.setCancel(true);
            }
        }

        if (event instanceof EventUpdate) {
            if (timeBox.get()) {
                mc.world.setTime(resolveTime(), resolveTime(), false);
            }

            if (weatherBox.get()) {
                switch (weatherMode.get()) {
                    case "Ясно" -> {
                        mc.world.setRainGradient(0f);
                        mc.world.setThunderGradient(0f);
                    }
                    case "Дождь" -> {
                        mc.world.setRainGradient(1f);
                        mc.world.setThunderGradient(0f);
                    }
                    case "Гроза" -> {
                        mc.world.setRainGradient(1f);
                        mc.world.setThunderGradient(1f);
                    }
                }
            }
        }

        if (event instanceof EventFog fogEvent && fog.get()) {
            int themeColor = ColorUtil.gradient(15, 360, Manager.STYLE_MANAGER.getFirstColor(), Manager.STYLE_MANAGER.getSecondColor());
            fogEvent.r = ((themeColor >> 16) & 0xFF) / 255.0f;
            fogEvent.g = ((themeColor >> 8) & 0xFF) / 255.0f;
            fogEvent.b = (themeColor & 0xFF) / 255.0f;
            fogEvent.alpha = 1.0f;
            fogEvent.start = 0.0f;
            fogEvent.end = fogEnd.get().floatValue();
            fogEvent.shape = FogShape.SPHERE;
            fogEvent.modified = true;
        }
    }

    private long resolveTime() {
        return switch (timeMode.get()) {
            case "День" -> 1000L;
            case "Ночь" -> 13000L;
            case "Утро" -> 0L;
            case "Восход" -> 23000L;
            case "Кастомное" -> (long) customTime.get().floatValue();
            default -> 6000L;
        };
    }
}
