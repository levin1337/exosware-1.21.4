package ru.levin.util.render;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.*;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import org.joml.*;
import org.lwjgl.opengl.GL11;
import ru.levin.manager.IMinecraft;
import ru.levin.modules.render.HUD;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.render.providers.ResourceProvider;
import ru.levin.util.shader.ShaderManager;

import java.awt.*;

import static org.lwjgl.opengl.GL11C.GL_ONE;
import static ru.levin.util.math.MathUtil.interpolate;

@SuppressWarnings("All")
public class RenderUtil implements IMinecraft {
    private static final Supplier<SimpleFramebuffer> TEMP_FBO_SUPPLIER = Suppliers.memoize(() -> new SimpleFramebuffer(mc.getWindow().getFramebufferWidth(), mc.getWindow().getFramebufferHeight(), false));
    private static Framebuffer getMainFbo() {
        return mc.getFramebuffer();
    }
    public static boolean isHovered(int mouseX, int mouseY, double x, double y, double width, double height) {
        return mouseX > x && mouseX < x + width && mouseY > y && mouseY < y + height;
    }
    public static boolean isInRegion(double mouseX, double mouseY, double x, double y, double width, double height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    public static boolean isInRegion(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
    public static int injectAlpha(int color, int alpha) {
        alpha = MathHelper.clamp(alpha, 0, 255);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    public static Vec3d interpolatePos(float prevX, float prevY, float prevZ, float x, float y, float z) {
        final Vec3d camPos = mc.getEntityRenderDispatcher().camera.getPos();
        final double delta = IMinecraft.tickCounter().getTickDelta(true);
        return new Vec3d(interpolate(prevX, x, delta) - camPos.x, interpolate(prevY, y, delta) - camPos.y, interpolate(prevZ, z, delta) - camPos.z);
    }

    public static int applyOpacity(int color, float opacity) {
        opacity = MathHelper.clamp(opacity, 0f, 1f);
        int alpha = (int) (((color >>> 24) & 0xFF) * opacity);
        return (alpha << 24) | (color & 0x00FFFFFF);
    }

    private static void setShaderUniforms(ShaderProgram shader, float width, float height, Vector4f radius, float smoothness) {
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(radius.x, radius.y, radius.z, radius.w);
        shader.getUniform("Smoothness").set(smoothness);
    }

    private static void setShaderUniforms(ShaderProgram shader, float width, float height, float radius, float smoothness) {
        setShaderUniforms(shader, width, height, new Vector4f(radius, radius, radius, radius), smoothness);
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float rounding, int color) {
        drawRoundedRect(matrices, x, y, width, height, new Vector4f(rounding, rounding, rounding, rounding), color);
    }

    public static void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, Vector4f rounding, int color) {
        enableRender();
        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.RECTANGLE_SHADER_KEY);
        setShaderUniforms(shader, width, height, rounding, 1.0f);
        ShaderManager.vertexShader(matrices, x, y, width, height, color);
        disableRender();
    }

    public static void rectRGB(MatrixStack matrices, float x, float y, float width, float height, float rounding, int color1, int color2, int color3, int color4) {
        rectRGB(matrices, x, y, width, height, new Vector4f(rounding, rounding, rounding, rounding), color1, color2, color3, color4);
    }

    public static void rectRGB(MatrixStack matrices, float x, float y, float width, float height, Vector4f rounding, int color1, int color2, int color3, int color4) {
        enableRender();
        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.RECTANGLE_SHADER_KEY);
        setShaderUniforms(shader, width, height, rounding, 1.0f);
        ShaderManager.vertexShader(matrices, x, y, width, height, color1, color2, color3, color4);
        disableRender();
    }

    public static void drawRoundedBorder(MatrixStack matrices, float x, float y, float width, float height, float rounding, float thickness, int color) {
        drawRoundedBorder(matrices, x, y, width, height, new Vector4f(rounding, rounding, rounding, rounding), thickness, color);
    }

    public static void drawRoundedBorder(MatrixStack matrices, float x, float y, float width, float height, Vector4f rounding, float thickness, int color) {
        enableRender();
        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.RECTANGLE_BORDER_SHADER_KEY);
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(rounding.x, rounding.y, rounding.z, rounding.w);
        shader.getUniform("Thickness").set(thickness);
        shader.getUniform("Smoothness").set(1.0f);
        ShaderManager.vertexShader(matrices, x, y, width, height, color);
        disableRender();
    }
    public static void drawBlur(MatrixStack matrices, float x, float y, float width, float height, float rounding, float blurRadius, int color) {
        drawBlur(matrices, x, y, width, height, new Vector4f(rounding, rounding, rounding, rounding), blurRadius, color);
    }

    public static void drawBlur(MatrixStack matrices, float x, float y, float width, float height, Vector4f rounding, float blurRadius, int color) {
        final SimpleFramebuffer fbo = TEMP_FBO_SUPPLIER.get();
        final Framebuffer mainFbo = getMainFbo();

        if (fbo.textureWidth != mainFbo.textureWidth || fbo.textureHeight != mainFbo.textureHeight) {
            fbo.resize(mainFbo.textureWidth, mainFbo.textureHeight);
        }

        enableRender();
        fbo.beginWrite(false);
        mainFbo.draw(fbo.textureWidth, fbo.textureHeight);
        mainFbo.beginWrite(false);

        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.BLUR_SHADER_KEY);
        RenderSystem.setShaderTexture(0, fbo.getColorAttachment());

        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(rounding.x, rounding.y, rounding.z, rounding.w);
        shader.getUniform("Smoothness").set(1f);
        shader.getUniform("BlurRadius").set(blurRadius);

        ShaderManager.vertexShader(matrices, x, y, width, height, color);

        RenderSystem.setShaderTexture(0, 0);
        disableRender();
    }
    public static void drawLiquidRect(MatrixStack matrices, float x, float y, float width, float height, Vector4f rounding, float cornerSmoothness, float fresnelPower, float fresnelAlpha, float baseAlpha, boolean fresnelInvert, float fresnelMix, float distortStrength, ColorRGBA color) {
        matrices.push();
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        Framebuffer screenFBO = mc.getFramebuffer();
        int screenTexture = screenFBO.getColorAttachment();
        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.GLASS_SHADER_KEY);
        BufferBuilder builder = RenderSystem.renderThreadTesselator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        shader.getUniform("ModelViewMat").set(matrix4f);
        shader.getUniform("ProjMat").set(RenderSystem.getProjectionMatrix());
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(rounding.x, rounding.y, rounding.z, rounding.w);
        shader.getUniform("Smoothness").set(1.0f);
        shader.getUniform("CornerSmoothness").set(cornerSmoothness);
        shader.getUniform("GlobalAlpha").set(color.getAlpha() / 255f);
        shader.getUniform("FresnelPower").set(fresnelPower);
        shader.getUniform("FresnelColor").set(1f, 1f, 1f);
        shader.getUniform("FresnelAlpha").set(fresnelAlpha);
        shader.getUniform("BaseAlpha").set(baseAlpha);
        shader.getUniform("FresnelInvert").set(fresnelInvert ? 1 : 0);
        shader.getUniform("FresnelMix").set(fresnelMix);
        shader.getUniform("DistortStrength").set(distortStrength);
        RenderSystem.setShaderTexture(0, screenTexture);
        enableRender();

        float scaleX = (float) screenFBO.textureWidth / mc.getWindow().getScaledWidth();
        float scaleY = (float) screenFBO.textureHeight / mc.getWindow().getScaledHeight();

        float fx = x * scaleX;
        float fy = y * scaleY;
        float fwidth = width * scaleX;
        float fheight = height * scaleY;
        fy = screenFBO.textureHeight - fy - fheight;

        float u0 = fx / screenFBO.textureWidth;
        float v0 = fy / screenFBO.textureHeight;
        float u1 = (fx + fwidth) / screenFBO.textureWidth;
        float v1 = (fy + fheight) / screenFBO.textureHeight;
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();
        int a = color.getAlpha();
        builder.vertex(matrix4f, x, y, 0f).texture(u0, v1).color(r, g, b, a);
        builder.vertex(matrix4f, x, y + height, 0f).texture(u0, v0).color(r, g, b, a);
        builder.vertex(matrix4f, x + width, y + height, 0f).texture(u1, v0).color(r, g, b, a);
        builder.vertex(matrix4f, x + width, y, 0f).texture(u1, v1).color(r, g, b, a);
        RenderUtil.render3D.endBuilding(builder);
        RenderSystem.setShaderTexture(0, 0);
        RenderUtil.disableRender();
        RenderSystem.enableDepthTest();
        matrices.pop();
    }

    public static void drawTexture(MatrixStack matrices, Object texture, float x, float y, float width, float height, float rounding, int color) {
        enableRender();
        Identifier textureId;
        if (texture instanceof String path) {
            textureId = Identifier.of("exosware", path);
        } else if (texture instanceof Identifier id) {
            textureId = id;
        } else {
            throw new IllegalArgumentException("Texture must be Identifier or String");
        }

        int glTextureId = mc.getTextureManager().getTexture(textureId).getGlId();
        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.TEXTURE_SHADER_KEY);
        RenderSystem.setShaderTexture(0, glTextureId);
        shader.getUniform("Size").set(width, height);
        shader.getUniform("Radius").set(rounding, rounding, rounding, rounding);
        shader.getUniform("Smoothness").set(1f);

        Matrix4f mat = matrices.peek().getPositionMatrix();
        BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);

        buffer.vertex(mat, x, y, 0).texture(0f, 0f).color(color);
        buffer.vertex(mat, x + width, y, 0).texture(1f, 0f).color(color);
        buffer.vertex(mat, x + width, y + height, 0).texture(1f, 1f).color(color);
        buffer.vertex(mat, x, y + height, 0).texture(0f, 1f).color(color);

        RenderUtil.render3D.endBuilding(buffer);
        disableRender();
    }

    public static void drawCircleBorder(MatrixStack matrices, float centerX, float centerY, float diameter, float thickness, int color) {
        enableRender();
        ShaderProgram shader = RenderSystem.setShader(ResourceProvider.RECTANGLE_BORDER_SHADER_KEY);

        float radius = diameter / 2f;

        shader.getUniform("Size").set(diameter, diameter);
        shader.getUniform("Radius").set(radius, radius, radius, radius);
        shader.getUniform("Thickness").set(thickness);
        shader.getUniform("Smoothness").set(1.0f);

        ShaderManager.vertexShader(matrices, centerX - radius, centerY - radius, diameter, diameter, color);
        disableRender();
    }

    public static void drawLine(float x1, float y1, float x2, float y2, int color) {
        RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
        BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
        buffer.vertex(x1, y1, 0f).color(color);
        buffer.vertex(x2, y2, 0f).color(color);
        RenderUtil.render3D.endBuilding(buffer);
    }
    public static void drawCircle(MatrixStack matrix, float x, float y, float radius, int color) {
        drawRoundedRect(matrix, x - radius / 2f, y - radius / 2f, radius, radius, radius / 2f - 1, color);
    }
    public static void enableRender(GlStateManager.SrcFactor srcFactor, GlStateManager.DstFactor dstFactor) {
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(srcFactor, dstFactor);
    }
    public static void enableRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
    }
    public static void disableRender() {
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * 3D Рендеринг
     */
    public class render3D {
        public static final Matrix4f lastProjMat = new Matrix4f();
        public static final Matrix4f lastModMat = new Matrix4f();
        public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();

        public static void setTranslation(MatrixStack matrixStack) {
            RenderUtil.render3D.lastProjMat.set(RenderSystem.getProjectionMatrix());
            RenderUtil.render3D.lastModMat.set(RenderSystem.getModelViewMatrix());
            RenderUtil.render3D.lastWorldSpaceMatrix.set(matrixStack.peek().getPositionMatrix());
        }

        public static Vec3d worldSpaceToScreenSpace(Vec3d pos) {
            Camera camera = mc.getEntityRenderDispatcher().camera;
            int displayHeight = mc.getWindow().getHeight();
            int[] viewport = new int[4];
            GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
            Vector3f target = new Vector3f();

            double deltaX = pos.x - camera.getPos().x;
            double deltaY = pos.y - camera.getPos().y;
            double deltaZ = pos.z - camera.getPos().z;

            Vector4f transformedCoordinates = new Vector4f((float) deltaX, (float) deltaY, (float) deltaZ, 1.f).mul(lastWorldSpaceMatrix);
            Matrix4f matrixProj = new Matrix4f(lastProjMat);
            Matrix4f matrixModel = new Matrix4f(lastModMat);
            matrixProj.mul(matrixModel).project(transformedCoordinates.x(), transformedCoordinates.y(), transformedCoordinates.z(), viewport, target);

            return new Vec3d(target.x / mc.getWindow().getScaleFactor(), (displayHeight - target.y) / mc.getWindow().getScaleFactor(), target.z);
        }


        public static MatrixStack matrixFrom(double x, double y, double z) {
            MatrixStack matrices = new MatrixStack();

            Camera camera = mc.gameRenderer.getCamera();
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(camera.getPitch()));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(camera.getYaw() + 180.0F));

            matrices.translate(x - camera.getPos().x, y - camera.getPos().y, z - camera.getPos().z);

            return matrices;
        }
        public static void drawShape(BlockPos blockPos, VoxelShape shape, boolean depth, int color1, int color2, int color3, int color4) {
            Vec3d offset = Vec3d.of(blockPos);
            shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
                Box box = new Box(minX, minY, minZ, maxX, maxY, maxZ).offset(offset);
                MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);
                Box shiftedBox = box.offset(new Vec3d(-box.minX, -box.minY, -box.minZ));
                renderFillBox(matrices, shiftedBox ,depth, color1, color2, color3, color4);
            });
        }


        public static void drawHoleOutline(Box box, int color, float lineWidth) {
            GL11.glEnable(GL11.GL_POLYGON_SMOOTH);
            RenderUtil.enableRender();
            MatrixStack matrices = matrixFrom(box.minX, box.minY, box.minZ);

            BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.LINES, VertexFormats.LINES);
            RenderSystem.setShader(ShaderProgramKeys.RENDERTYPE_LINES);
            RenderSystem.lineWidth(lineWidth);

            box = box.offset(new Vec3d(box.minX, box.minY, box.minZ).negate());

            float x1 = (float) box.minX;
            float y1 = (float) box.minY;
            float y2 = (float) box.maxY;
            float z1 = (float) box.minZ;
            float x2 = (float) box.maxX;
            float z2 = (float) box.maxZ;

            ShaderManager.vertexLine(matrices, buffer, x1, y2, z1, x2, y2, z1, color);
            ShaderManager.vertexLine(matrices, buffer, x2, y2, z1, x2, y2, z2, color);
            ShaderManager.vertexLine(matrices, buffer, x2, y2, z2, x1, y2, z2, color);
            ShaderManager.vertexLine(matrices, buffer, x1, y2, z2, x1, y2, z1, color);

            ShaderManager.vertexLine(matrices, buffer, x1, y1, z1, x2, y1, z1, color);
            ShaderManager.vertexLine(matrices, buffer, x2, y1, z1, x2, y1, z2, color);
            ShaderManager.vertexLine(matrices, buffer, x2, y1, z2, x1, y1, z2, color);
            ShaderManager.vertexLine(matrices, buffer, x1, y1, z2, x1, y1, z1, color);

            ShaderManager.vertexLine(matrices, buffer, x1, y1, z1, x1, y2, z1, color);
            ShaderManager.vertexLine(matrices, buffer, x2, y1, z2, x2, y2, z2, color);
            ShaderManager.vertexLine(matrices, buffer, x1, y1, z2, x1, y2, z2, color);
            ShaderManager.vertexLine(matrices, buffer, x2, y1, z1, x2, y2, z1, color);

            endBuilding(buffer);
            RenderUtil.disableRender();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            GL11.glDisable(GL11.GL_POLYGON_SMOOTH);
        }
        private static void renderFillBox(MatrixStack stack, Box box, boolean depth, int color1, int color2, int color3, int color4) {
            BufferBuilder buffer = IMinecraft.tessellator().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
            Matrix4f matrix = stack.peek().getPositionMatrix();

            float minX = (float) box.minX;
            float minY = (float) box.minY;
            float minZ = (float) box.minZ;
            float maxX = (float) box.maxX;
            float maxY = (float) box.maxY;
            float maxZ = (float) box.maxZ;
            if (depth) {
                RenderSystem.disableDepthTest();
                RenderUtil.enableRender();
                RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE);
            }
            RenderSystem.setShader(ShaderProgramKeys.POSITION_COLOR);
            buffer.vertex(matrix, minX, minY, minZ).color(ColorUtil.getRed(color1) / 255F, ColorUtil.getGreen(color1) / 255F, ColorUtil.getBlue(color1) / 255F, ColorUtil.getAlpha(color1) / 255F);
            buffer.vertex(matrix, minX, minY, maxZ).color(ColorUtil.getRed(color2) / 255F, ColorUtil.getGreen(color2) / 255F, ColorUtil.getBlue(color2) / 255F, ColorUtil.getAlpha(color2) / 255F);
            buffer.vertex(matrix, maxX, minY, maxZ).color(ColorUtil.getRed(color3) / 255F, ColorUtil.getGreen(color3) / 255F, ColorUtil.getBlue(color3) / 255F, ColorUtil.getAlpha(color3) / 255F);
            buffer.vertex(matrix, maxX, minY, minZ).color(ColorUtil.getRed(color4) / 255F, ColorUtil.getGreen(color4) / 255F, ColorUtil.getBlue(color4) / 255F, ColorUtil.getAlpha(color4) / 255F);

            buffer.vertex(matrix, minX, maxY, minZ).color(ColorUtil.getRed(color1) / 255F, ColorUtil.getGreen(color1) / 255F, ColorUtil.getBlue(color1) / 255F, ColorUtil.getAlpha(color1) / 255F);
            buffer.vertex(matrix, maxX, maxY, minZ).color(ColorUtil.getRed(color2) / 255F, ColorUtil.getGreen(color2) / 255F, ColorUtil.getBlue(color2) / 255F, ColorUtil.getAlpha(color2) / 255F);
            buffer.vertex(matrix, maxX, maxY, maxZ).color(ColorUtil.getRed(color3) / 255F, ColorUtil.getGreen(color3) / 255F, ColorUtil.getBlue(color3) / 255F, ColorUtil.getAlpha(color3) / 255F);
            buffer.vertex(matrix, minX, maxY, maxZ).color(ColorUtil.getRed(color4) / 255F, ColorUtil.getGreen(color4) / 255F, ColorUtil.getBlue(color4) / 255F, ColorUtil.getAlpha(color4) / 255F);

            buffer.vertex(matrix, minX, minY, minZ).color(ColorUtil.getRed(color1) / 255F, ColorUtil.getGreen(color1) / 255F, ColorUtil.getBlue(color1) / 255F, ColorUtil.getAlpha(color1) / 255F);
            buffer.vertex(matrix, minX, maxY, minZ).color(ColorUtil.getRed(color2) / 255F, ColorUtil.getGreen(color2) / 255F, ColorUtil.getBlue(color2) / 255F, ColorUtil.getAlpha(color2) / 255F);
            buffer.vertex(matrix, maxX, maxY, minZ).color(ColorUtil.getRed(color3) / 255F, ColorUtil.getGreen(color3) / 255F, ColorUtil.getBlue(color3) / 255F, ColorUtil.getAlpha(color3) / 255F);
            buffer.vertex(matrix, maxX, minY, minZ).color(ColorUtil.getRed(color4) / 255F, ColorUtil.getGreen(color4) / 255F, ColorUtil.getBlue(color4) / 255F, ColorUtil.getAlpha(color4) / 255F);

            buffer.vertex(matrix, minX, minY, maxZ).color(ColorUtil.getRed(color1) / 255F, ColorUtil.getGreen(color1) / 255F, ColorUtil.getBlue(color1) / 255F, ColorUtil.getAlpha(color1) / 255F);
            buffer.vertex(matrix, maxX, minY, maxZ).color(ColorUtil.getRed(color2) / 255F, ColorUtil.getGreen(color2) / 255F, ColorUtil.getBlue(color2) / 255F, ColorUtil.getAlpha(color2) / 255F);
            buffer.vertex(matrix, maxX, maxY, maxZ).color(ColorUtil.getRed(color3) / 255F, ColorUtil.getGreen(color3) / 255F, ColorUtil.getBlue(color3) / 255F, ColorUtil.getAlpha(color3) / 255F);
            buffer.vertex(matrix, minX, maxY, maxZ).color(ColorUtil.getRed(color4) / 255F, ColorUtil.getGreen(color4) / 255F, ColorUtil.getBlue(color4) / 255F, ColorUtil.getAlpha(color4) / 255F);

            buffer.vertex(matrix, minX, minY, minZ).color(ColorUtil.getRed(color1) / 255F, ColorUtil.getGreen(color1) / 255F, ColorUtil.getBlue(color1) / 255F, ColorUtil.getAlpha(color1) / 255F);
            buffer.vertex(matrix, minX, minY, maxZ).color(ColorUtil.getRed(color2) / 255F, ColorUtil.getGreen(color2) / 255F, ColorUtil.getBlue(color2) / 255F, ColorUtil.getAlpha(color2) / 255F);
            buffer.vertex(matrix, minX, maxY, maxZ).color(ColorUtil.getRed(color3) / 255F, ColorUtil.getGreen(color3) / 255F, ColorUtil.getBlue(color3) / 255F, ColorUtil.getAlpha(color3) / 255F);
            buffer.vertex(matrix, minX, maxY, minZ).color(ColorUtil.getRed(color4) / 255F, ColorUtil.getGreen(color4) / 255F, ColorUtil.getBlue(color4) / 255F, ColorUtil.getAlpha(color4) / 255F);

            buffer.vertex(matrix, maxX, minY, minZ).color(ColorUtil.getRed(color1) / 255F, ColorUtil.getGreen(color1) / 255F, ColorUtil.getBlue(color1) / 255F, ColorUtil.getAlpha(color1) / 255F);
            buffer.vertex(matrix, maxX, minY, maxZ).color(ColorUtil.getRed(color2) / 255F, ColorUtil.getGreen(color2) / 255F, ColorUtil.getBlue(color2) / 255F, ColorUtil.getAlpha(color2) / 255F);
            buffer.vertex(matrix, maxX, maxY, maxZ).color(ColorUtil.getRed(color3) / 255F, ColorUtil.getGreen(color3) / 255F, ColorUtil.getBlue(color3) / 255F, ColorUtil.getAlpha(color3) / 255F);
            buffer.vertex(matrix, maxX, maxY, minZ).color(ColorUtil.getRed(color4) / 255F, ColorUtil.getGreen(color4) / 255F, ColorUtil.getBlue(color4) / 255F, ColorUtil.getAlpha(color4) / 255F);
            endBuilding(buffer);
            if (depth) {
                RenderUtil.disableRender();
                RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
                RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            }
        }
        public static void setFilledBoxVertexes(BufferBuilder bufferBuilder, Matrix4f m, Box box, Color c) {
            float minX = (float) (box.minX - mc.getEntityRenderDispatcher().camera.getPos().getX());
            float minY = (float) (box.minY - mc.getEntityRenderDispatcher().camera.getPos().getY());
            float minZ = (float) (box.minZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());
            float maxX = (float) (box.maxX - mc.getEntityRenderDispatcher().camera.getPos().getX());
            float maxY = (float) (box.maxY - mc.getEntityRenderDispatcher().camera.getPos().getY());
            float maxZ = (float) (box.maxZ - mc.getEntityRenderDispatcher().camera.getPos().getZ());

            bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB());
            bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB());

            bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB());
            bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB());

            bufferBuilder.vertex(m, maxX, minY, minZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB());

            bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, minY, maxZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB());
            bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB());

            bufferBuilder.vertex(m, minX, minY, minZ).color(c.getRGB());
            bufferBuilder.vertex(m, minX, minY, maxZ).color(c.getRGB());
            bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB());
            bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB());

            bufferBuilder.vertex(m, minX, maxY, minZ).color(c.getRGB());
            bufferBuilder.vertex(m, minX, maxY, maxZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, maxY, maxZ).color(c.getRGB());
            bufferBuilder.vertex(m, maxX, maxY, minZ).color(c.getRGB());
        }

        public static void endBuilding(BufferBuilder bb) {
            BuiltBuffer builtBuffer = bb.endNullable();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }
        }
    }
}
