package ru.levin.manager.dragManager;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;
import ru.levin.events.impl.player.EventAttack;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.modules.Function;
import ru.levin.util.render.RenderUtil;

import static ru.levin.manager.IMinecraft.mc;

public class Dragging {
    private static final int SNAP_DISTANCE = 10;
    private static final float FADE_SPEED = 0.01f;
    private static final float DRAG_SMOOTHNESS = 0.1f;

    @Expose
    @SerializedName("x")
    private float xPos;
    @Expose
    @SerializedName("y")
    private float yPos;

    public float initialXVal;
    public float initialYVal;

    private float startX, startY;
    private boolean dragging;

    private float width, height;
    private int defaultX;
    private int defaultY;

    @Expose
    @SerializedName("name")
    private final String name;
    private final Function function;

    private float borderAlpha = 0f;
    private float borderScale = 0f;
    private float guideAlpha = 0f;
    private float tipAlpha = 0f;
    public float targetX, targetY;

    private long lastTime = System.currentTimeMillis();

    public Dragging(Function function, String name, float initialXVal, float initialYVal) {
        this.function = function;
        this.name = name;
        this.xPos = initialXVal;
        this.yPos = initialYVal;
        this.initialXVal = initialXVal;
        this.initialYVal = initialYVal;

        this.targetX = initialXVal;
        this.targetY = initialYVal;

        this.defaultX = (int) initialXVal;
        this.defaultY = (int) initialYVal;

        DragManager.draggables.put(name, this);
    }

    public int getDefaultX() { return defaultX; }
    public int getDefaultY() { return defaultY; }
    public float getX() { return xPos; }
    public void setX(float x) { this.xPos = x; this.targetX = x; }
    public float getY() { return yPos; }
    public void setY(float y) { this.yPos = y; this.targetY = y; }
    public float getWidth() { return width; }
    public void setWidth(float width) { this.width = width; }
    public float getHeight() { return height; }
    public void setHeight(float height) { this.height = height; }
    public String getName() { return name; }
    public Function getModule() { return function; }

    public void onDraw(double mouseX, double mouseY, Window window) {
        double scaledMouseX = mouseX;
        double scaledMouseY = mouseY;

        long windowHandle = mc.getWindow().getHandle();
        boolean isShiftDown = GLFW.glfwGetKey(windowHandle, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS;

        if (dragging) {
            float newX = (float) (scaledMouseX - startX);
            float newY = (float) (scaledMouseY - startY);

            float screenWidth = window.getScaledWidth();
            float screenHeight = window.getScaledHeight();
            float screenCenterX = screenWidth / 2f;
            float screenCenterY = screenHeight / 2f;

            if (isShiftDown) {
                if (Math.abs(newX + width / 2 - screenCenterX) <= SNAP_DISTANCE) newX = screenCenterX - width / 2;
                if (Math.abs(newY + height / 2 - screenCenterY) <= SNAP_DISTANCE) newY = screenCenterY - height / 2;

                for (Dragging other : DragManager.draggables.values()) {
                    if (other == this) continue;
                    float otherCenterX = other.getX() + other.width / 2;
                    float otherCenterY = other.getY() + other.height / 2;

                    if (Math.abs((newX + width / 2) - otherCenterX) <= SNAP_DISTANCE) newX = otherCenterX - width / 2;
                    if (Math.abs((newY + height / 2) - otherCenterY) <= SNAP_DISTANCE) newY = otherCenterY - height / 2;
                }
            }

            targetX = Math.max(0, Math.min(newX, screenWidth - width));
            targetY = Math.max(0, Math.min(newY, screenHeight - height));
        }

        xPos += (targetX - xPos) * DRAG_SMOOTHNESS;
        yPos += (targetY - yPos) * DRAG_SMOOTHNESS;

        borderAlpha = dragging ? Math.min(borderAlpha + FADE_SPEED, 1f) : Math.max(borderAlpha - FADE_SPEED, 0f);

        float targetExpand = dragging ? 4f : 0f;
        borderScale += (targetExpand - borderScale) * 0.1f;
        if (borderAlpha > 0f) {
            int alphaColor = ((int)(borderAlpha * 255) << 24) | 0xFFFFFF;

            float drawX = xPos - borderScale / 2f;
            float drawY = yPos - borderScale / 2f;
            float drawWidth = width + borderScale;
            float drawHeight = height + borderScale;

            RenderUtil.drawRoundedBorder(new MatrixStack(), drawX, drawY, drawWidth, drawHeight, 3f, 0.01f, alphaColor);
        }

        float targetTipAlpha = dragging ? 1f : 0f;
        tipAlpha += (targetTipAlpha - tipAlpha) * 0.1f;
        tipAlpha = MathHelper.clamp(tipAlpha, 0f, 1f);
        if (tipAlpha > 0f) {
            int color = ((int) (tipAlpha * 255) << 24) | 0xFFFFFF;
            MatrixStack matrix = new MatrixStack();
            String tip = "Shift - чтобы прилепить к сетке";
            FontUtils.durman[12].centeredDraw(matrix, tip, xPos + width / 2f, yPos + height + 4f, color);
        }
    }

    public void renderGuides(Window window) {
        long now = System.currentTimeMillis();
        float delta = (now - lastTime) / 16.666f;
        lastTime = now;
        float targetAlpha = dragging ? 1f : 0f;
        guideAlpha += (targetAlpha - guideAlpha) * 1.2f * delta;
        guideAlpha = MathHelper.clamp(guideAlpha, 0f, 1f);
        if (guideAlpha <= 0f) return;

        float screenWidth = window.getScaledWidth();
        float screenHeight = window.getScaledHeight();
        float screenCenterX = screenWidth / 2f;
        float screenCenterY = screenHeight / 2f;
        int alphaColor = ((int) (guideAlpha * 255) << 24) | 0xFFFFFF;

        RenderUtil.drawLine(screenCenterX, 0, screenCenterX, screenHeight, alphaColor);
        RenderUtil.drawLine(0, screenCenterY, screenWidth, screenCenterY, alphaColor);

        for (Dragging other : DragManager.draggables.values()) {
            if (other == this) continue;
            float otherCenterX = other.getX() + other.width / 2;
            float otherCenterY = other.getY() + other.height / 2;
            if (Math.abs((xPos + width / 2) - otherCenterX) < SNAP_DISTANCE) {
                RenderUtil.drawLine(otherCenterX, 0, otherCenterX, screenHeight, alphaColor);
            }
            if (Math.abs((yPos + height / 2) - otherCenterY) < SNAP_DISTANCE) {
                RenderUtil.drawLine(0, otherCenterY, screenWidth, otherCenterY, alphaColor);
            }
        }
    }

    public boolean onClick(double mouseX, double mouseY, int button) {
        if (!(mc.currentScreen instanceof ChatScreen)) return false;

        float scaledMouseX = (float) mouseX;
        float scaledMouseY = (float) mouseY;

        if (button == 0 && RenderUtil.isInRegion((int) scaledMouseX, (int) scaledMouseY, xPos, yPos, width, height)) {
            dragging = true;
            startX = scaledMouseX - xPos;
            startY = scaledMouseY - yPos;
            return true;
        }
        return false;
    }

    public void onRelease(int button) {
        if (button == 0) dragging = false;
    }
}
