package ru.levin.modules.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.option.Perspective;
import net.minecraft.client.render.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4d;
import ru.levin.manager.IMinecraft;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.render.EventRender2D;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.render.RenderUtil;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("All")
@FunctionAnnotation(name = "ESP", desc = "Красивые квадраты на игроках", type = Type.Render)
public class ESP extends Function {

    private final MultiSetting targets = new MultiSetting(
            "Отображать",
            Arrays.asList("Игроков", "Друзей", "Меня"),
            new String[]{"Игроков", "Друзей", "Меня", "Предметы"}
    );

    public ESP() {
        addSettings(targets);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventRender2D e)) return;
        if (mc.options.hudHidden) return;

        Matrix4f matrix = e.getDrawContext().getMatrices().peek().getPositionMatrix();

        RenderUtil.enableRender();
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        List<AbstractClientPlayerEntity> players = Manager.SYNC_MANAGER.getPlayers();
        List<Entity> entities = targets.get("Предметы") ? Manager.SYNC_MANAGER.getEntities() : List.of();

        for (PlayerEntity player : players) {
            if (shouldRender(player)) {
                drawBox(e.getDeltatick(), buffer, player, matrix);
            }
        }

        for (Entity entity : entities) {
            if (entity instanceof ItemEntity) {
                drawBox(e.getDeltatick(), buffer, entity, matrix);
            }
        }

        RenderUtil.render3D.endBuilding(buffer);
        RenderUtil.disableRender();
    }

    private boolean shouldRender(PlayerEntity entity) {
        if (entity == mc.player) {
            if (mc.options.getPerspective() == Perspective.FIRST_PERSON) return false;
            return targets.get("Меня");
        }
        if (targets.get("Друзей") && Manager.FRIEND_MANAGER.isFriend(entity.getName().getString())) {
            return true;
        }
        return targets.get("Игроков");
    }

    public void drawBox(RenderTickCounter tick, BufferBuilder buffer, @NotNull Entity ent, Matrix4f matrix) {
        Vec3d[] corners = getVectors(tick, ent);

        Vector4d pos = null;
        for (Vec3d corner : corners) {
            Vec3d screen = RenderUtil.render3D.worldSpaceToScreenSpace(corner);
            if (screen.z <= 0 || screen.z >= 1) continue;

            if (pos == null) pos = new Vector4d(screen.x, screen.y, screen.x, screen.y);
            else {
                if (screen.x < pos.x) pos.x = screen.x;
                if (screen.y < pos.y) pos.y = screen.y;
                if (screen.x > pos.z) pos.z = screen.x;
                if (screen.y > pos.w) pos.w = screen.y;
            }
        }

        if (pos == null) return;

        double screenW = mc.getWindow().getScaledWidth();
        double screenH = mc.getWindow().getScaledHeight();
        if (pos.z < 0 || pos.x > screenW || pos.w < 0 || pos.y > screenH) return;

        float x1 = (float) pos.x;
        float y1 = (float) pos.y;
        float x2 = (float) pos.z;
        float y2 = (float) pos.w;

        int black = Color.BLACK.getRGB();

        drawRect(buffer, matrix, x1 - 1f, y1, x1 + 0.5f, y2 + 0.5f, black);
        drawRect(buffer, matrix, x1 - 1f, y1 - 0.5f, x2 + 0.5f, y1 + 1f, black);
        drawRect(buffer, matrix, x2 - 1f, y1, x2 + 0.5f, y2 + 0.5f, black);
        drawRect(buffer, matrix, x1 - 1f, y2 - 1f, x2 + 0.5f, y2 + 0.5f, black);

        int cTop = ColorUtil.getColorStyle(270);
        int cRight = ColorUtil.getColorStyle(90);
        int cBottom = ColorUtil.getColorStyle(180);
        int cLeft = ColorUtil.getColorStyle(0);

        drawRect(buffer, matrix, x1 - 0.5f, y1, x1 + 0.5f, y2, cTop, cLeft, cLeft, cTop);
        drawRect(buffer, matrix, x1, y2 - 0.5f, x2, y2, cLeft, cBottom, cBottom, cLeft);
        drawRect(buffer, matrix, x1 - 0.5f, y1, x2, y1 + 0.5f, cBottom, cRight, cRight, cBottom);
        drawRect(buffer, matrix, x2 - 0.5f, y1, x2, y2, cRight, cTop, cTop, cRight);
    }

    private void drawRect(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2, int c1) {
        buffer.vertex(matrix, x1, y2, 0f).color(c1);
        buffer.vertex(matrix, x2, y2, 0f).color(c1);
        buffer.vertex(matrix, x2, y1, 0f).color(c1);
        buffer.vertex(matrix, x1, y1, 0f).color(c1);
    }

    private void drawRect(BufferBuilder buffer, Matrix4f matrix, float x1, float y1, float x2, float y2,
                          int c1, int c2, int c3, int c4) {
        buffer.vertex(matrix, x1, y2, 0f).color(c1);
        buffer.vertex(matrix, x2, y2, 0f).color(c2);
        buffer.vertex(matrix, x2, y1, 0f).color(c3);
        buffer.vertex(matrix, x1, y1, 0f).color(c4);
    }

    @NotNull
    private Vec3d[] getVectors(RenderTickCounter tick, @NotNull Entity ent) {
        double x = ent.prevX + (ent.getX() - ent.prevX) * tick.getTickDelta(true);
        double y = ent.prevY + (ent.getY() - ent.prevY) * tick.getTickDelta(true);
        double z = ent.prevZ + (ent.getZ() - ent.prevZ) * tick.getTickDelta(true);

        Box bb = ent.getBoundingBox();
        double dx = bb.minX - ent.getX() + x;
        double dy = bb.minY - ent.getY() + y;
        double dz = bb.minZ - ent.getZ() + z;
        double dx2 = bb.maxX - ent.getX() + x;
        double dy2 = bb.maxY - ent.getY() + y;
        double dz2 = bb.maxZ - ent.getZ() + z;

        return new Vec3d[]{
                new Vec3d(dx - 0.05, dy, dz - 0.05),
                new Vec3d(dx - 0.05, dy2 + 0.15, dz - 0.05),
                new Vec3d(dx2 + 0.05, dy, dz - 0.05),
                new Vec3d(dx2 + 0.05, dy2 + 0.15, dz - 0.05),
                new Vec3d(dx - 0.05, dy, dz2 + 0.05),
                new Vec3d(dx - 0.05, dy2 + 0.15, dz2 + 0.05),
                new Vec3d(dx2 + 0.05, dy, dz2 + 0.05),
                new Vec3d(dx2 + 0.05, dy2 + 0.15, dz2 + 0.05)
        };
    }
}
