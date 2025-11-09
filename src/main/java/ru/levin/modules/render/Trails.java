package ru.levin.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;
import ru.levin.manager.IMinecraft;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.IEntity;
import ru.levin.util.render.RenderUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@FunctionAnnotation(name = "Trails", type = Type.Render, desc = "Красивая линия за вами")
public class Trails extends Function {

    private final MultiSetting targets = new MultiSetting("Отображать у",
            Arrays.asList("Друзей", "Меня"),
            new String[]{"Игроков", "Друзей", "Меня"});

    private final long trailLifetimeMs = 250L;
    private final double minDistance = 0.01;

    public Trails() {
        addSettings(targets);
    }

    @Override
    public void onEvent(Event event) {
        long now = System.currentTimeMillis();
        if (event instanceof EventUpdate) {
            for (PlayerEntity entity : Manager.SYNC_MANAGER.getPlayers()) {
                if (!shouldRenderTrails(entity)) continue;
                List<Trail> trails = ((IEntity) entity).exosWareFabric1_21_4$getTrails();
                trails.removeIf(t -> t.isExpired(now));
            }
            return;
        }

        if (event instanceof EventRender3D renderEvent) {
            float tickDelta = renderEvent.getDeltatick().getTickDelta(true);
            Vec3d cameraPos = mc.gameRenderer.getCamera().getPos();
            for (PlayerEntity entity : Manager.SYNC_MANAGER.getPlayers()) {
                if (!shouldRenderTrails(entity)) continue;
                Vec3d interp = interpolateEntityPosition(entity, tickDelta);
                List<Trail> trails = ((IEntity) entity).exosWareFabric1_21_4$getTrails();
                if (trails.isEmpty()) {
                    trails.add(new Trail(interp, getTrailColor(entity), now));
                } else {
                    Trail last = trails.get(trails.size() - 1);
                    if (last.pos.distanceTo(interp) >= minDistance) {
                        trails.add(new Trail(interp, getTrailColor(entity), now));
                    }
                }
                render(renderEvent, entity, cameraPos, now);
            }
        }
    }

    private int getTrailColor(PlayerEntity entity) {
        if (Manager.FRIEND_MANAGER.isFriend(entity.getName().getString())) {
            return new Color(0, 255, 0).getRGB();
        }
        return ColorUtil.getColorStyle(360);
    }

    private boolean shouldRenderTrails(PlayerEntity entity) {
        if (entity == mc.player) {
            if (mc.options.getPerspective() == Perspective.FIRST_PERSON) {
                return false;
            }
            return targets.get("Меня");
        }
        if (targets.get("Друзей") && Manager.FRIEND_MANAGER.isFriend(entity.getName().getString())) {
            return true;
        }
        return targets.get("Игроков");
    }

    private Vec3d interpolateEntityPosition(PlayerEntity entity, float tickDelta) {
        double ix = entity.prevX + (entity.getX() - entity.prevX) * tickDelta;
        double iy = entity.prevY + (entity.getY() - entity.prevY) * tickDelta;
        double iz = entity.prevZ + (entity.getZ() - entity.prevZ) * tickDelta;
        return new Vec3d(ix, iy, iz);
    }

    private void render(EventRender3D event, PlayerEntity entity, Vec3d cameraPos, long now) {
        List<Trail> trails = ((IEntity) entity).exosWareFabric1_21_4$getTrails();
        if (trails.isEmpty()) return;

        float playerHeight = entity.getHeight();
        event.getMatrixStack().push();
        RenderSystem.disableCull();
        RenderUtil.enableRender(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);

        BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.TRIANGLE_STRIP, VertexFormats.POSITION_COLOR);

        for (Trail p : trails) {
            if (p.isExpired(now)) continue;
            float ageFrac = (float) (now - p.time) / (float) trailLifetimeMs;
            float alpha = 1f - Math.min(1f, ageFrac);
            alpha = Math.max(0.01f, alpha);
            int color = RenderUtil.injectAlpha(p.color, (int) (alpha * 255));
            Vec3d posRel = p.pos.subtract(cameraPos);

            buffer.vertex(event.getMatrixStack().peek().getPositionMatrix(), (float) posRel.x, (float) (posRel.y + playerHeight), (float) posRel.z).color(color);
            buffer.vertex(event.getMatrixStack().peek().getPositionMatrix(), (float) posRel.x, (float) posRel.y, (float) posRel.z).color(color);
        }

        RenderUtil.render3D.endBuilding(buffer);
        RenderUtil.disableRender();
        RenderSystem.disableDepthTest();
        event.getMatrixStack().pop();
    }

    public class Trail {
        public final Vec3d pos;
        public final int color;
        public final long time;

        public Trail(Vec3d pos, int color, long time) {
            this.pos = pos;
            this.color = color;
            this.time = time;
        }

        public boolean isExpired(long now) {
            return (now - time) > trailLifetimeMs;
        }
    }
}
