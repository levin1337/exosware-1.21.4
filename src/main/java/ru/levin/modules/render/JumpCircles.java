package ru.levin.modules.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.ShaderProgramKeys;
import net.minecraft.client.render.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import ru.levin.manager.IMinecraft;
import ru.levin.modules.setting.*;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.animations.Animation;
import ru.levin.util.animations.impl.EaseBackIn;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.math.MathUtil;
import ru.levin.util.player.TimerUtil;
import ru.levin.util.render.RenderAddon;
import ru.levin.util.render.RenderUtil;
import java.util.*;

@SuppressWarnings("All")
@FunctionAnnotation(name = "JumpCircles", type = Type.Render, desc = "Красивые круги при прыжке")
public class JumpCircles extends Function {

    private final ModeSetting circleType = new ModeSetting("Тип круга", "Type-2", "Type-1", "Type-2", "Type-3");
    private final SliderSetting rotateSpeed = new SliderSetting("Скорость", 0.1f, 0.1f, 5f, 0.1f);
    private final SliderSetting circleScale = new SliderSetting("Размер", 0.7f, 0.6f, 5f, 0.1f);
    private final MultiSetting targets = new MultiSetting(
            "Отображать у",
            Arrays.asList("Друзей", "Меня"),
            new String[]{"Игроков", "Друзей", "Меня"}
    );

    private final Identifier CIRCLE_TEXTURE = Identifier.of("exosware", "images/circles/circle.png");
    private final Identifier CIRCLE2_TEXTURE = Identifier.of("exosware", "images/circles/circle2.png");
    private final Identifier CIRCLE3_TEXTURE = Identifier.of("exosware", "images/circles/circle3.png");
    public JumpCircles() {
        addSettings(circleType, targets, rotateSpeed, circleScale);
    }

    private final List<Circle> circles = new ArrayList<>();
    private final Map<PlayerEntity, Boolean> wasOnGround = new HashMap<>();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            circles.removeIf(c -> c.timer.getTime() > 8000);
            for (PlayerEntity player : Manager.SYNC_MANAGER.getPlayers()) {
                if (player == null || player.isRemoved()) continue;

                boolean isFriend = Manager.FRIEND_MANAGER != null && player.getName() != null && Manager.FRIEND_MANAGER.isFriend(player.getName().getString());
                boolean isMe = player == mc.player;

                boolean showPlayers = targets.get("Игроков");
                boolean showFriends = targets.get("Друзей");
                boolean showMe = targets.get("Меня");

                boolean shouldTrack = (isMe && showMe) || (!isMe && isFriend && showFriends) || (!isMe && !isFriend && showPlayers);

                if (!shouldTrack) {
                    wasOnGround.remove(player);
                    continue;
                }

                boolean previouslyOnGround = wasOnGround.getOrDefault(player, true);
                boolean currentlyOnGround = player.isOnGround();

                if (previouslyOnGround && !currentlyOnGround) {
                    Circle circle = new Circle(new Vec3d(player.getX(), Math.floor(player.getY()) + 0.001f, player.getZ()), new TimerUtil(), new EaseBackIn(400, 1, 1.3f));
                    circle.animation.setDirection(Direction.AxisDirection.POSITIVE);
                    circles.add(circle);
                }

                wasOnGround.put(player, currentlyOnGround);
            }
        }

        if (event instanceof EventRender3D render3D) {
            renderCircles(render3D);
        }
    }

    private Identifier texture = null;

    private void renderCircles(EventRender3D eventRender3D) {
        Collections.reverse(circles);
        eventRender3D.getMatrixStack().push();
        RenderUtil.enableRender(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        switch (circleType.get()) {
            case "Type-1" -> texture = CIRCLE_TEXTURE;
            case "Type-2" -> texture = CIRCLE2_TEXTURE;
            case "Type-3" -> texture = CIRCLE3_TEXTURE;
        }

        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR);
        BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        for (Circle c : circles) {
            float elapsed = (float) c.timer.getTime();
            float alphaFade = MathUtil.clamp(1f - (elapsed / 8000f), 0f, 1f);
            float animScale = (float) c.animation.getOutput();

            eventRender3D.getMatrixStack().push();
            eventRender3D.getMatrixStack().translate(c.pos().x - mc.getEntityRenderDispatcher().camera.getPos().getX(), c.pos().y - mc.getEntityRenderDispatcher().camera.getPos().getY(), c.pos().z - mc.getEntityRenderDispatcher().camera.getPos().getZ());
            eventRender3D.getMatrixStack().multiply(RotationAxis.POSITIVE_X.rotationDegrees(90));
            eventRender3D.getMatrixStack().multiply(RotationAxis.POSITIVE_Z.rotationDegrees((elapsed / 50f) * rotateSpeed.get().floatValue()));

            RenderAddon.sizeAnimation(eventRender3D.getMatrixStack(), 0, 0, animScale * circleScale.get().floatValue());

            float size = 1f;
            Matrix4f matrix = eventRender3D.getMatrixStack().peek().getPositionMatrix();

            buffer.vertex(matrix, -size, size, 0).texture(0, 1).color(RenderUtil.applyOpacity(ColorUtil.getColorStyle(270), alphaFade));
            buffer.vertex(matrix, size, size, 0).texture(1, 1).color(RenderUtil.applyOpacity(ColorUtil.getColorStyle(0), alphaFade));
            buffer.vertex(matrix, size, -size, 0).texture(1, 0).color(RenderUtil.applyOpacity(ColorUtil.getColorStyle(180), alphaFade));
            buffer.vertex(matrix, -size, -size, 0).texture(0, 0).color(RenderUtil.applyOpacity(ColorUtil.getColorStyle(90), alphaFade));

            eventRender3D.getMatrixStack().pop();
        }

        RenderUtil.render3D.endBuilding(buffer);
        RenderSystem.depthMask(true);
        RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();

        eventRender3D.getMatrixStack().pop();
        Collections.reverse(circles);
    }

    record Circle(Vec3d pos, TimerUtil timer, Animation animation) {}
}
