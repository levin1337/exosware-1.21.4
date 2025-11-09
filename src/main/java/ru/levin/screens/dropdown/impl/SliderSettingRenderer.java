package ru.levin.screens.dropdown.impl;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import ru.levin.manager.Manager;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.screens.dropdown.SettingRenderer;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;

import java.awt.*;
import java.util.Locale;

public class SliderSettingRenderer implements SettingRenderer<SliderSetting> {

    private static final int HEIGHT = 20;
    private static final int BAR_HEIGHT = 4;
    private static final int PADDING = 0;

    private static final float CIRCLE_RADIUS = 6f;
    private static final float CIRCLE_SCALE_MAX = 1.2f;
    private static final float CIRCLE_SCALE_MIN = 1f;
    private static final float SCALE_STEP = 0.05f;

    @Override
    public void render(DrawContext ctx, SliderSetting setting, int x, int y, int width, int height) {
        int barWidth = width - 2 * PADDING;
        int barX = x + PADDING;
        int barY = y + height - BAR_HEIGHT - 4;

        RenderUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, barWidth, BAR_HEIGHT, 1, new Color(50, 50, 50, 180).getRGB());

        double increment = setting.getIncrement();
        double rawValue = setting.get().doubleValue();
        double roundedValue = Math.round(rawValue / increment) * increment;
        double progress = (roundedValue - setting.getMin()) / (setting.getMax() - setting.getMin());
        int targetProgressWidth = (int) (barWidth * progress);

        if (setting.circlePos == -1) setting.circlePos = targetProgressWidth;
        setting.circlePos += (targetProgressWidth - setting.circlePos) * 0.2;

        RenderUtil.drawRoundedRect(ctx.getMatrices(), barX, barY, (int) setting.circlePos, BAR_HEIGHT, 1, Manager.STYLE_MANAGER.getFirstColor());

        FontUtils.durman[13].drawLeftAligned(ctx.getMatrices(), setting.getName(), x + PADDING, y + 2, Color.WHITE.getRGB());
        String valueText = formatValue(roundedValue, increment);
        int valueWidth = (int) FontUtils.durman[13].getWidth(valueText);
        FontUtils.durman[13].drawLeftAligned(ctx.getMatrices(), valueText, x + width - valueWidth - PADDING, y + 2, Color.WHITE.getRGB());
        
        if (setting.dragging) {
            setting.circleScale += SCALE_STEP;
            if (setting.circleScale > CIRCLE_SCALE_MAX) setting.circleScale = CIRCLE_SCALE_MAX;
        } else {
            setting.circleScale -= SCALE_STEP;
            if (setting.circleScale < CIRCLE_SCALE_MIN) setting.circleScale = CIRCLE_SCALE_MIN;
        }

        float circleX = barX + (float) setting.circlePos;
        float circleY = barY + BAR_HEIGHT / 2f;

        MatrixStack matrices = ctx.getMatrices();
        matrices.push();
        matrices.translate(circleX, circleY, 0);
        matrices.scale(setting.circleScale, setting.circleScale, 1f);
        matrices.translate(-circleX, -circleY, 0);

        RenderUtil.drawCircle(matrices, circleX, circleY, CIRCLE_RADIUS, Color.WHITE.getRGB());
        matrices.pop();
    }


    private String formatValue(double val, double increment) {
        if (increment >= 1) {
            return String.format(Locale.US, "%d", (long) val);
        } else if (increment >= 0.1) {
            return String.format(Locale.US, "%.1f", val);
        } else {
            return String.format(Locale.US, "%.2f", val);
        }
    }

    @Override
    public boolean mouseClicked(SliderSetting setting, double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        if (button != 0) return false;

        int barWidth = width - 2 * PADDING;
        int barX = x + PADDING;
        int barY = y + height - BAR_HEIGHT - 4;

        if (RenderUtil.isInRegion(mouseX, mouseY, barX, barY - 3, barWidth, BAR_HEIGHT + 6)) {
            updateValue(setting, mouseX, barX, barWidth);
            setting.dragging = true;
            return true;
        }
        return false;
    }


    public void mouseReleased(SliderSetting setting) {
        setting.dragging = false;
    }

    public void mouseDragged(SliderSetting setting, double mouseX, int x, int width) {
        if (!setting.dragging) return;

        int barWidth = width - 2 * PADDING;
        int barX = x + PADDING;

        updateValue(setting, mouseX, barX, barWidth);
    }

    private void updateValue(SliderSetting setting, double mouseX, int barX, int barWidth) {
        double relX = mouseX - barX;
        double percent = Math.min(Math.max(relX / barWidth, 0), 1);

        double newValue = setting.getMin() + percent * (setting.getMax() - setting.getMin());
        double increment = setting.getIncrement();
        newValue = Math.round(newValue / increment) * increment;
        newValue = Math.min(Math.max(newValue, setting.getMin()), setting.getMax());

        setting.set(newValue);
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}
