package ru.levin.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import ru.levin.events.Event;
import ru.levin.events.impl.render.EventRender2D;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.util.color.ColorUtil;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.math.MathUtil;
import ru.levin.util.render.RenderAddon;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.shader.ShaderManager;
import ru.levin.util.vector.EntityPosition;
import ru.levin.util.vector.VectorUtil;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static ru.levin.util.render.RenderUtil.render3D.drawHoleOutline;

@SuppressWarnings("All")
@FunctionAnnotation(name = "Prediction", type = Type.Render, desc = "Рисует линию куда упадёт эндер-жемчюг")
public class Prediction extends Function {
    private final BooleanSetting box = new BooleanSetting("Рисовать бокс",false);
    private final BooleanSetting rect = new BooleanSetting("Рисовать рект под эндер-жемчюгом",false);
    private static final ItemStack ENDER_PEARL_STACK = new ItemStack(Items.ENDER_PEARL);
    private static final Color BOX_COLOR = new Color(255, 255, 255, 255);
    private static final int MAX_STEPS = 150;
    private static final float FADE_LEN = 6.0f;

    private final List<PearlPoint> pearlPoints = new ArrayList<>();

    public Prediction() {
        addSettings(box,rect);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D render2D) {
            for (PearlPoint pearlPoint : pearlPoints) {

                Vector3d projection = VectorUtil.toScreen(pearlPoint.position.x, pearlPoint.position.y - 0.3F, pearlPoint.position.z);
                if (projection == null || projection.z < 0) continue;
                double time = pearlPoint.ticks * 0.05;
                String text = String.format("%.1f сек", time);

                float fontHeight = FontUtils.durman[15].getHeight();
                float textWidth = FontUtils.durman[15].getWidth(text);

                float paddingX = 3f;

                float bgWidth = textWidth + paddingX * 2;
                float bgHeight = fontHeight + 1;

                float centerX = (float) projection.x;
                float centerY = (float) projection.y;

                float bgX = centerX - bgWidth / 2f;
                float bgY = centerY;
                RenderUtil.drawRoundedRect(render2D.getMatrixStack(), bgX, bgY, bgWidth, bgHeight, 2, 0xB2060712);
                float textX = centerX - textWidth / 2f;
                float textY = bgY + (bgHeight - fontHeight) / 2f;
                FontUtils.durman[15].drawLeftAligned(render2D.getDrawContext().getMatrices(), text, textX, textY, -1);

                float pearlSize = 11;
                float pearlX = centerX - pearlSize / 2f;
                float pearlY = bgY - pearlSize - 2f;
                if (rect.get()) {
                    RenderUtil.drawRoundedRect(render2D.getMatrixStack(), pearlX - 0.5f, pearlY - 0.2f, 12, 12, 2, 0xB2060712);
                }
                RenderAddon.renderItem(render2D.getDrawContext(), ENDER_PEARL_STACK, pearlX, pearlY, pearlSize / 16f,false);
            }
        }
        if (event instanceof EventRender3D e3d) {
            renderTrajectories(e3d);
        }
    }

    private void renderTrajectories(EventRender3D event) {
        MatrixStack stack = event.getMatrixStack();
        Vec3d cameraPos = mc.getEntityRenderDispatcher().camera.getPos();

        stack.push();
        stack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
        RenderSystem.disableDepthTest();
        RenderSystem.disableCull();
        RenderUtil.enableRender(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
        RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
        RenderSystem.lineWidth(3);

        BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
        pearlPoints.clear();

        for (Entity entity : Manager.SYNC_MANAGER.getEntities()) {
            if (entity instanceof EnderPearlEntity enderPearlEntity)
                simulatePearl(stack, buffer, enderPearlEntity);
        }

        RenderUtil.render3D.endBuilding(buffer);

        if (box.get()) {
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            for (PearlPoint pearlPoint : pearlPoints) {
                Vec3d pos = pearlPoint.position;
                Box outlineBox = new Box(pos.x - 0.15, pos.y - 0.15, pos.z - 0.15, pos.x + 0.15, pos.y + 0.15, pos.z + 0.15);
                drawHoleOutline(outlineBox, BOX_COLOR.getRGB(), 1);
            }
        }


        RenderUtil.disableRender();
        RenderSystem.enableDepthTest();
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        stack.pop();
    }

    private void simulatePearl(MatrixStack stack, BufferBuilder buffer, EnderPearlEntity pearl) {
        Vec3d motion = pearl.getVelocity();
        Vec3d pos = pearl.getPos();
        int ticks = 0;

        float dist = 0f;
        int baseRGB = ColorUtil.getColorStyle(360) & 0x00FFFFFF;

        for (int i = 0; i < MAX_STEPS; i++) {
            Vec3d prevPos = pos;
            pos = pos.add(motion);
            motion = getNextMotion(pearl, prevPos, motion);

            HitResult hitResult = mc.world.raycast(new RaycastContext(prevPos, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, pearl));
            if (hitResult.getType() == HitResult.Type.BLOCK) {
                pos = hitResult.getPos();
            }

            float segLen = (float) prevPos.distanceTo(pos);
            float a1 = MathUtil.smoothstep(0f, FADE_LEN, dist);
            float a2 = MathUtil.smoothstep(0f, FADE_LEN, dist + segLen);

            int c1 = ColorUtil.withAlpha(baseRGB, a1);
            int c2 = ColorUtil.withAlpha(baseRGB, a2);

            vertexLineGradient(stack, buffer, (float) prevPos.x, (float) prevPos.y, (float) prevPos.z, (float) pos.x, (float) pos.y, (float) pos.z, c1, c2);

            dist += segLen;

            if (hitResult.getType() == HitResult.Type.BLOCK || pos.y < -128) {
                pearlPoints.add(new PearlPoint(pos, ticks));
                break;
            }
            ticks++;
        }
    }

    private void vertexLineGradient(MatrixStack matrices, VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, int color1, int color2) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        float[] col1 = ColorUtil.rgba(color1);
        float[] col2 = ColorUtil.rgba(color2);
        Vector3f normalVec = ShaderManager.getNormal(x1, y1, z1, x2, y2, z2);

        buffer.vertex(model, x1, y1, z1).color(col1[0], col1[1], col1[2], col1[3]).normal(matrices.peek(), normalVec.x(), normalVec.y(), normalVec.z());
        buffer.vertex(model, x2, y2, z2).color(col2[0], col2[1], col2[2], col2[3]).normal(matrices.peek(), normalVec.x(), normalVec.y(), normalVec.z());
    }

    private Vec3d getNextMotion(ThrownEntity throwable, Vec3d prevPos, Vec3d motion) {
        boolean isInWater = mc.world.getBlockState(BlockPos.ofFloored(prevPos)).getFluidState().isIn(FluidTags.WATER);

        motion = motion.multiply(isInWater ? 0.8 : 0.99);

        if (!throwable.hasNoGravity()) {
            motion = motion.add(0, -0.03F, 0);
        }
        return motion;
    }

    record PearlPoint(Vec3d position, int ticks) {}
}
