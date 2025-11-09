package ru.levin.util.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.manager.IMinecraft;
import ru.levin.util.render.providers.ResourceProvider;

import java.util.*;

@SuppressWarnings("All")
public class RenderAddon implements IMinecraft {
    private static AbstractClientPlayerEntity fakePlayer;
    public static void renderFakePlayer(Vec3d defensivePos, boolean fakelags, boolean isAccumulatingPackets, EventRender3D render) {
        if (defensivePos == null || !fakelags || !isAccumulatingPackets)
            return;

        if (fakePlayer == null) {
            createFakePlayer();
        }

        double x = defensivePos.x;
        double y = defensivePos.y;
        double z = defensivePos.z;
        fakePlayer.updatePosition(x, y, z);
        fakePlayer.prevX = x;
        fakePlayer.prevY = y;
        fakePlayer.prevZ = z;
        fakePlayer.lastRenderX = x;
        fakePlayer.lastRenderY = y;
        fakePlayer.lastRenderZ = z;

        fakePlayer.setYaw(mc.player.getYaw());
        fakePlayer.setPitch(mc.player.getPitch());
        fakePlayer.headYaw = mc.player.headYaw;
        fakePlayer.bodyYaw = mc.player.bodyYaw;

        var dispatcher = mc.getEntityRenderDispatcher();
        var rawRenderer = dispatcher.getRenderer(fakePlayer);
        @SuppressWarnings("unchecked")
        var renderer = (EntityRenderer<AbstractClientPlayerEntity, EntityRenderState>) rawRenderer;

        var state = renderer.createRenderState();
        renderer.updateRenderState(fakePlayer, state, render.getDeltatick().getTickDelta(true));

        var matrices = render.getMatrixStack();
        var vertexConsumers = mc.getBufferBuilders().getEntityVertexConsumers();

        matrices.push();
        matrices.translate(state.x - dispatcher.camera.getPos().x, state.y - dispatcher.camera.getPos().y, state.z - dispatcher.camera.getPos().z);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.35f);

        renderer.render(state, matrices, vertexConsumers, 15728880);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();

        vertexConsumers.draw();
        matrices.pop();
    }

    public static void sizeAnimation(MatrixStack matrix, double width, double height, double scale) {
        matrix.translate(width, height, 0);
        matrix.scale((float) scale, (float) scale, (float) scale);
        matrix.translate(-width, -height, 0);
    }

    public static void renderItem(DrawContext drawContext, ItemStack item, float x, float y, float size, boolean stackOverlay) {
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(x, y, 0);
        drawContext.getMatrices().scale(size, size, 1);
        drawContext.drawItem(item, 0, 0);
        if (stackOverlay) {
            drawContext.drawStackOverlay(mc.textRenderer, item, 0, 0);
        }
        drawContext.getMatrices().pop();
    }

    public static void renderPlayerItems(DrawContext e, float x, float y, LivingEntity player, float scale, float offset) {
        List<ItemStack> stacks = new ArrayList<>();
        stacks.add(player.getMainHandStack());
        player.getArmorItems().forEach(stacks::add);
        stacks.add(player.getOffHandStack());
        stacks.removeIf(i -> i.getItem() instanceof AirBlockItem || i.isEmpty());

        float offset2 = 0;
        for (ItemStack stack : stacks) {
            e.getMatrices().push();
            e.getMatrices().translate(x + offset2, y, 0);
            e.getMatrices().scale(scale, scale, 1.0f);
            e.drawItem(stack, 0, 0, 7, 0);

            e.drawStackOverlay(mc.textRenderer, stack, 0, 0);

            e.getMatrices().pop();

            offset2 += offset;
        }
    }
    public static void drawHead(MatrixStack matrix, Entity entity, float x, float y, float size, float round) {
        if (!(entity instanceof LivingEntity living)) return;
        int color = 0xFFFFFFFF;
        if (living.hurtTime > 0) {
            float hurtPercent = living.hurtTime / (float) living.maxHurtTime;
            int red = 255;
            int green = (int) (255 * (1.0f - hurtPercent));
            int blue = (int) (255 * (1.0f - hurtPercent));
            color = (255 << 24) | (red << 16) | (green << 8) | blue;
        }
        Identifier texture = null;

        if (entity instanceof PlayerEntity player) {
            PlayerListEntry entry = Optional.ofNullable(mc.getNetworkHandler()).map(handler -> handler.getPlayerListEntry(player.getUuid())).orElse(null);
            if (entry != null) {
                texture = entry.getSkinTextures().texture();
            }
        }

        if (texture == null) {
            EntityRenderer<? super LivingEntity, ?> baseRenderer = mc.getEntityRenderDispatcher().getRenderer(living);
            if (baseRenderer instanceof LivingEntityRenderer<?, ?, ?>) {
                @SuppressWarnings("unchecked")
                LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?> renderer = (LivingEntityRenderer<LivingEntity, LivingEntityRenderState, ?>) baseRenderer;
                LivingEntityRenderState state = renderer.getAndUpdateRenderState(living, IMinecraft.tickCounter().getTickDelta(true));

                texture = renderer.getTexture(state);
            }
        }

        if (texture != null) {
            drawHeadInternal(matrix, texture, x, y, size, round, color);
        }
    }


    private static void drawHeadInternal(MatrixStack matrix, Identifier texture, float x, float y, float size, float rounding, int color) {
        RenderUtil.enableRender();

        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.TEXTURE_SHADER_KEY);
        RenderSystem.setShaderTexture(0, texture);

        shader.getUniform("Size").set(size, size);
        shader.getUniform("Radius").set(rounding, rounding, rounding, rounding);
        shader.getUniform("Smoothness").set(1.0f);

        float u1 = 8f / 64f; float v1 = 8f / 64f;
        float u2 = 16f / 64f; float v2 = 16f / 64f;


        var matrix4f = matrix.peek().getPositionMatrix();
        BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        buffer.vertex(matrix4f, x, y, 0).texture(u1, v1).color(color);
        buffer.vertex(matrix4f, x + size, y, 0).texture(u2, v1).color(color);
        buffer.vertex(matrix4f, x + size, y + size, 0).texture(u2, v2).color(color);
        buffer.vertex(matrix4f, x, y + size, 0).texture(u1, v2).color(color);
        RenderUtil.render3D.endBuilding(buffer);

        RenderUtil.disableRender();
    }


    public static void drawStaffHead(MatrixStack matrix, Identifier texture, float x, float y, float size, float round) {
        if (texture != null) {
            drawHeadInternal(matrix, texture, x, y, size, round, 0xFFFFFFFF);
        }
    }
    private static void createFakePlayer() {
        if (mc.player == null || mc.world == null) return;

        fakePlayer = new AbstractClientPlayerEntity(mc.world, mc.player.getGameProfile()) {
            @Override
            public boolean isSpectator() {
                return false;
            }

            @Override
            public boolean isCreative() {
                return false;
            }
        };

        fakePlayer.copyFrom(mc.player);
        fakePlayer.setPos(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        fakePlayer.headYaw = mc.player.headYaw;
        fakePlayer.bodyYaw = mc.player.bodyYaw;
    }
}
