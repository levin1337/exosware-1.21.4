package ru.levin.util.shader;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import ru.levin.manager.IMinecraft;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.render.RenderUtil;

import java.awt.*;

public class ShaderManager implements IMinecraft {
    /**
     * 1 цвет
     */
    public static void vertexShader(MatrixStack matrixStack, float x, float y, float width, float height, int color) {
        float[] rgba = ColorUtil.rgba(color);
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        BufferBuilder builder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        builder.vertex(matrix, x, y, 0).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.vertex(matrix, x, y + height, 0).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.vertex(matrix, x + width, y + height, 0).color(rgba[0], rgba[1], rgba[2], rgba[3]);
        builder.vertex(matrix, x + width, y, 0).color(rgba[0], rgba[1], rgba[2], rgba[3]);

        RenderUtil.render3D.endBuilding(builder);
    }
    /**
     * 4 цвета
     */
    public static void vertexShader(MatrixStack matrixStack, float x, float y, float width, float height, int color1, int color2, int color3, int color4) {
        Matrix4f matrix = matrixStack.peek().getPositionMatrix();
        BufferBuilder builder = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        builder.vertex(matrix, x, y,0).color(ColorUtil.getRed(color1) / 255F, ColorUtil.getGreen(color1) / 255F, ColorUtil.getBlue(color1) / 255F, ColorUtil.getAlpha(color1) / 255F);

        builder.vertex(matrix, x, y + height, 0).color(ColorUtil.getRed(color2) / 255F, ColorUtil.getGreen(color2) / 255F, ColorUtil.getBlue(color2) / 255F, ColorUtil.getAlpha(color2) / 255F);
        builder.vertex(matrix, x + width, y + height, 0).color(ColorUtil.getRed(color3) / 255F, ColorUtil.getGreen(color3) / 255F, ColorUtil.getBlue(color3) / 255F, ColorUtil.getAlpha(color3) / 255F);
        builder.vertex(matrix, x + width, y, 0).color(ColorUtil.getRed(color4) / 255F, ColorUtil.getGreen(color4) / 255F, ColorUtil.getBlue(color4) / 255F, ColorUtil.getAlpha(color4) / 255F);

        RenderUtil.render3D.endBuilding(builder);
    }
    public static void vertexLine(MatrixStack matrices, VertexConsumer buffer, float x1, float y1, float z1, float x2, float y2, float z2, int lineColor) {
        Matrix4f model = matrices.peek().getPositionMatrix();
        MatrixStack.Entry entry = matrices.peek();
        Vector3f normalVec = getNormal(x1, y1, z1, x2, y2, z2);
        buffer.vertex(model, x1, y1, z1).color(ColorUtil.getRed(lineColor) / 255F, ColorUtil.getGreen(lineColor) / 255F, ColorUtil.getBlue(lineColor) / 255F, ColorUtil.getAlpha(lineColor) / 255F).normal(entry, normalVec.x(), normalVec.y(), normalVec.z());
        buffer.vertex(model, x2, y2, z2).color(ColorUtil.getRed(lineColor) / 255F, ColorUtil.getGreen(lineColor) / 255F, ColorUtil.getBlue(lineColor) / 255F, ColorUtil.getAlpha(lineColor) / 255F).normal(entry, normalVec.x(), normalVec.y(), normalVec.z());
    }
    public static Vector3f getNormal(float x1, float y1, float z1, float x2, float y2, float z2) {
        float xNormal = x2 - x1;
        float yNormal = y2 - y1;
        float zNormal = z2 - z1;
        float normalSqrt = MathHelper.sqrt(xNormal * xNormal + yNormal * yNormal + zNormal * zNormal);

        return new Vector3f(xNormal / normalSqrt, yNormal / normalSqrt, zNormal / normalSqrt);
    }
}
