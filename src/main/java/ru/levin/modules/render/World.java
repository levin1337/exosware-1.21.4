package ru.levin.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.gl.ShaderProgramKeys;
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

    private final BooleanSetting cubes = new BooleanSetting("Летающие кубы", false);
    private final List<Particle> particles = new ArrayList<>();

    public World() {
        addSettings(timeBox, timeMode, customTime, weatherBox, weatherMode, fog, fogEnd, cubes);
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

            if (cubes.get() && mc.player != null) {
                particles.removeIf(p -> p.alpha == 0.0f && p.timer.hasTimeElapsed(p.liveTicks));
                for (Particle p : particles) p.tick();

                if (particles.size() < 100) {
                    particles.add(new Particle(
                            mc.player.getPos().add(MathUtil.random(-20.0, 20.0), MathUtil.random(0.0, 5.0), MathUtil.random(-20.0, 20.0)),
                            Vec3d.ZERO,
                            new Vec3d(MathUtil.random(-1.0, 1.0), MathUtil.random(0.0, 2.0), MathUtil.random(-1.0, 1.0)),
                            new Vec3d(MathUtil.random(-1.0, 1.0), MathUtil.random(-1.0, 1.0), MathUtil.random(-1.0, 1.0)),
                            (long) MathUtil.random(1500.0, 4500.0),
                            MathUtil.random(0.1f, 0.3f)
                    ));
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

        if (event instanceof EventRender3D ms && cubes.get()) {
            Camera camera = mc.gameRenderer.getCamera();
            Vec3d cameraPos = camera.getPos();

            ms.getMatrixStack().push();
            RenderUtil.enableRender(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.setShaderTexture(0, ResourceProvider.firefly);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
            BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

            for (Particle particle : this.particles) {
                Vec3d pos = VectorUtil.getInterpolatedPos(particle.prev, particle.pos, ms.getDeltatick().getTickDelta(true));
                float size = 4.0f * particle.size;
                ms.getMatrixStack().push();
                ms.getMatrixStack().translate(pos.subtract(cameraPos));
                ms.getMatrixStack().multiply(camera.getRotation());

                int color = ColorUtil.applyAlpha(ColorUtil.getColorStyle(360), particle.alpha * 0.4f);
                drawImage(ms.getMatrixStack(), builder, -size / 2f, -size / 2f, 0, size, size, color);

                ms.getMatrixStack().pop();
            }

            RenderUtil.render3D.endBuilding(builder);
            RenderSystem.depthMask(true);
            RenderUtil.disableRender();
            ms.getMatrixStack().pop();

            RenderUtil.enableRender(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.depthMask(false);
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            BufferBuilder linesBuffer = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.DEBUG_LINES, VertexFormats.POSITION_COLOR);

            for (Iterator<Particle> it = particles.iterator(); it.hasNext();) {
                Particle particle = it.next();
                particle.tick();
                if (particle.isDead()) {
                    it.remove();
                    continue;
                }

                Vec3d pos = VectorUtil.getInterpolatedPos(particle.prev, particle.pos, ms.getDeltatick().getTickDelta(true));
                Vec3d rot = VectorUtil.getInterpolatedPos(particle.prevRot, particle.rotate, ms.getDeltatick().getTickDelta(true));

                ms.getMatrixStack().push();
                ms.getMatrixStack().translate(pos.add(-cameraPos.getX(), -cameraPos.getY(), -cameraPos.getZ()));
                ms.getMatrixStack().multiply(new Quaternionf().rotationXYZ((float)rot.x, (float)rot.y, (float)rot.z));
                ms.getMatrixStack().scale(particle.size + 0.1F, particle.size + 0.1F, particle.size + 0.1F);

                int mainColor = ColorUtil.applyAlpha(ColorUtil.getColorStyle(360), particle.alpha * 0.4f);
                int outlineColor = ColorUtil.applyAlpha(ColorUtil.getColorStyle(360), particle.alpha);
                GL11.glEnable(GL11.GL_POLYGON_SMOOTH);

                renderBoxInternalDiagonals(ms.getMatrixStack(), linesBuffer, new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), mainColor);
                renderOutlinedBox(ms.getMatrixStack(), linesBuffer, new Box(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5), outlineColor);

                GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
                ms.getMatrixStack().pop();
            }
            RenderUtil.render3D.endBuilding(linesBuffer);
            RenderSystem.depthMask(true);
            RenderSystem.defaultBlendFunc();
            RenderUtil.disableRender();
        }
    }

    private void drawImage(MatrixStack matrices, BufferBuilder builder, double x, double y, double z, double width, double height, int color) {
        var matrix = matrices.peek().getPositionMatrix();
        float r = ColorUtil.getRed(color) / 255f;
        float g = ColorUtil.getGreen(color) / 255f;
        float b = ColorUtil.getBlue(color) / 255f;
        float a = ColorUtil.getAlpha(color) / 255f;
        builder.vertex(matrix, (float)x, (float)(y + height), (float)z).texture(0f, 1f).color(r, g, b, a);
        builder.vertex(matrix, (float)(x + width), (float)(y + height), (float)z).texture(1f, 1f).color(r, g, b, a);
        builder.vertex(matrix, (float)(x + width), (float)y, (float)z).texture(1f, 0f).color(r, g, b, a);
        builder.vertex(matrix, (float)x, (float)y, (float)z).texture(0f, 0f).color(r, g, b, a);
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

    private static void renderBoxInternalDiagonals(MatrixStack matrices, BufferBuilder buf, Box box, int color) {
        float r = ColorUtil.getRed(color) / 255F;
        float g = ColorUtil.getGreen(color) / 255F;
        float b = ColorUtil.getBlue(color) / 255F;
        float a = ColorUtil.getAlpha(color) / 255F;
        var matrix = matrices.peek().getPositionMatrix();
        buf.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(r, g, b, a);
        buf.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a);
        buf.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(r, g, b, a);
        buf.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a);
        buf.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(r, g, b, a);
        buf.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(r, g, b, a);
        buf.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(r, g, b, a);
        buf.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(r, g, b, a);
    }

    private static void renderOutlinedBox(MatrixStack matrices, BufferBuilder buffer, Box box, int color) {
        float r = ColorUtil.getRed(color) / 255F;
        float g = ColorUtil.getGreen(color) / 255F;
        float b = ColorUtil.getBlue(color) / 255F;
        float a = ColorUtil.getAlpha(color) / 255F;
        var matrix = matrices.peek().getPositionMatrix();
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(r, g, b, a);
    }

    static class Particle {
        Vec3d prev, prevRot, pos, rotate, motion, rotateMotion;
        final long liveTicks;
        float size;
        final TimerUtil timer = new TimerUtil();
        float alpha = 0f;
        public Particle(Vec3d pos, Vec3d rotate, Vec3d motion, Vec3d rotateMotion, long liveTicks, float size) {
            this.pos = pos;
            this.rotate = rotate;
            this.motion = motion.multiply(0.04);
            this.rotateMotion = rotateMotion.multiply(0.04);
            this.liveTicks = liveTicks;
            this.size = size;
            this.prevRot = rotate;
            this.prev = pos;
            this.timer.reset();
        }
        void tick() {
            this.prev = this.pos;
            this.prevRot = this.rotate;
            this.pos = this.pos.add(this.motion);
            this.rotate = this.rotate.add(this.rotateMotion);
            this.motion = this.motion.multiply(0.98);
            this.rotateMotion = this.rotateMotion.multiply(0.98);
            float lifeProgress = Math.min(timer.getTime() / (float) liveTicks, 1f);
            if (lifeProgress < 0.1f) {
                alpha = lifeProgress * 10f;
            } else if (lifeProgress > 0.9f) {
                alpha = (1f - lifeProgress) * 10f;
            } else {
                alpha = 1f;
            }
        }
        boolean isDead() {
            return timer.hasTimeElapsed(liveTicks);
        }
    }
}
