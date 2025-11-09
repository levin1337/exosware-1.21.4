package ru.levin.util.vector;

import net.minecraft.client.render.Camera;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import ru.levin.manager.IMinecraft;
import ru.levin.mixin.iface.GameRendererAccessor;

import java.lang.Math;

public class VectorUtil implements IMinecraft {
    private static final float MIN_DISTANCE_SQ = 1.0f * 1.0f;

    public static Matrix4f previousProjectionMatrix = new Matrix4f();
    public static MatrixStack.Entry lastWorldSpaceMatrix = new MatrixStack().peek();

    public static Vector3d toScreen(Vector3d vec) {
        return toScreen(vec.x, vec.y, vec.z);
    }

    public static Vector3d toScreen(double x, double y, double z) {
        if (lastWorldSpaceMatrix == null || previousProjectionMatrix == null) {
            return new Vector3d(-1, -1, -1);
        }


        Vector3f vector3f = new Vector3f((float) x, (float) y, (float) z);
        int[] viewport = new int[4];
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);

        Vector4f vector4f = new Vector4f(vector3f.x, vector3f.y, vector3f.z, 1.0f).mul(lastWorldSpaceMatrix.getPositionMatrix());

        if (vector4f.z > 0) return new Vector3d(-1, -1, -1);

        Vector3f target = new Vector3f();
        new Matrix4f(previousProjectionMatrix).project(vector4f.x, vector4f.y, vector4f.z, viewport, target);

        double scale = mc.getWindow().getScaleFactor();
        return new Vector3d(target.x / scale, (mc.getWindow().getHeight() - target.y) / scale, target.z);
    }

    public static Vector2f project(double x, double y, double z) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        if (camera == null) return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);

        Vec3d camPos = camera.getPos();
        Vector3f pos = new Vector3f((float) (x - camPos.x), (float) (y - camPos.y), (float) (z - camPos.z));

        Quaternionf rotation = new Quaternionf(mc.getEntityRenderDispatcher().getRotation()).conjugate();
        pos.rotate(rotation);

        if (mc.options.getBobView().getValue() && mc.getCameraEntity() instanceof PlayerEntity player) {
            applyViewBobbing(player, pos);
        }

        double fov = ((GameRendererAccessor) mc.gameRenderer).invokeGetFov(camera, mc.getRenderTickCounter().getTickDelta(true), true);
        return calculateScreenPosition(pos, fov);
    }

    private static void applyViewBobbing(PlayerEntity player, Vector3f pos) {
        float delta = mc.getRenderTickCounter().getTickDelta(true);
        float speed = MathHelper.lerp(delta, player.prevStrideDistance, player.strideDistance);
        float bob = player.upwardSpeed - player.sidewaysSpeed;

        if (bob != 0) {
            float roll = MathHelper.sin((float) (bob * Math.PI)) * speed * 3f;
            pos.rotateZ(roll * ((float) Math.PI / 180f));

            float pitch = Math.abs(MathHelper.cos((float) (bob * Math.PI - 0.2f)) * speed) * 5f;
            pos.rotateX(pitch * ((float) Math.PI / 180f));

        }

        pos.add(MathHelper.sin((float) (bob * Math.PI)) * speed * 0.5f, -Math.abs(MathHelper.cos((float) (bob * Math.PI)) * speed), 0f);
    }

    private static Vector2f calculateScreenPosition(Vector3f pos, double fov) {
        if (pos.z >= 0) return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
        Window w = mc.getWindow();
        float halfW = w.getScaledWidth() * 0.5f;
        float halfH = w.getScaledHeight() * 0.5f;
        float factor = (float) (halfH / Math.tan(Math.toRadians(fov) * 0.5)) / -pos.z;

        return new Vector2f(halfW + pos.x * factor, halfH - pos.y * factor);
    }
    public static Vec3d getInterpolatedPos(Entity entity, float tickDelta) {
        return new Vec3d(MathHelper.lerp((double)tickDelta, (double)entity.prevX, (double)entity.getX()), MathHelper.lerp((double)tickDelta, (double)entity.prevY, (double)entity.getY()), MathHelper.lerp((double)tickDelta, (double)entity.prevZ, (double)entity.getZ()));
    }

    public static Vec3d getInterpolatedPos(Vec3d prev, Vec3d pos, float tickDelta) {
        return new Vec3d(MathHelper.lerp((double)tickDelta, (double)prev.x, (double)pos.getX()), MathHelper.lerp((double)tickDelta, (double)prev.y, (double)pos.getY()), MathHelper.lerp((double)tickDelta, (double)prev.z, (double)pos.getZ()));
    }
}
