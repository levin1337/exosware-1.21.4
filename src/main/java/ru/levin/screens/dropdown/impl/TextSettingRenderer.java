package ru.levin.screens.dropdown.impl;

import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import ru.levin.modules.setting.TextSetting;
import ru.levin.screens.dropdown.SettingRenderer;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.render.Scissor;

import java.awt.*;

public class TextSettingRenderer implements SettingRenderer<TextSetting> {

    private static final int PADDING = 0;
    private static final int HORIZONTAL_PADDING = 6;
    private static final int VERTICAL_PADDING = 3;
    private static final int FIELD_RADIUS = 3;
    private static final int MIN_FIELD_WIDTH = 60;
    private static final int MAX_FIELD_WIDTH = 105;

    private long lastBlinkTime = 0;
    private float scrollOffset = 0;

    @Override
    public void render(DrawContext ctx, TextSetting setting, int x, int y, int width, int height) {
        int fontSizeName = 13;
        int fontSizeField = 13;

        int nameHeight = (int) FontUtils.durman[fontSizeName].getHeight();
        FontUtils.durman[fontSizeName].drawLeftAligned(ctx.getMatrices(), setting.getName(), x + PADDING, y, Color.WHITE.getRGB());

        String value = setting.getValue();
        int textWidth = (int) FontUtils.durman[fontSizeField].getWidth(value) + HORIZONTAL_PADDING * 2;

        int fieldWidth = Math.max(MIN_FIELD_WIDTH, Math.min(textWidth, MAX_FIELD_WIDTH));
        int fieldHeight = (int) (FontUtils.durman[fontSizeField].getHeight() + VERTICAL_PADDING * 2);
        int fieldX = x + PADDING;
        int fieldY = y + nameHeight + 3;

        int bgColor = setting.isFocused() ? new Color(60, 60, 60, 200).getRGB() : new Color(40, 40, 40, 200).getRGB();
        RenderUtil.drawRoundedRect(ctx.getMatrices(), fieldX, fieldY, fieldWidth, fieldHeight - 1, FIELD_RADIUS, bgColor);

        float targetScroll = 0f;
        int cursorX = (int) FontUtils.durman[fontSizeField].getWidth(value.substring(0, setting.getCursorPosition()));
        if (cursorX + HORIZONTAL_PADDING > fieldWidth) {
            targetScroll = fieldWidth - (cursorX + HORIZONTAL_PADDING);
        } else if (cursorX + HORIZONTAL_PADDING < 0) {
            targetScroll = -cursorX + HORIZONTAL_PADDING;
        }
        scrollOffset += (targetScroll - scrollOffset) * 0.2f;

        Scissor.push();
        Scissor.setFromComponentCoordinates(fieldX + HORIZONTAL_PADDING, fieldY, fieldWidth - HORIZONTAL_PADDING * 2, fieldHeight);
        FontUtils.durman[fontSizeField].drawLeftAligned(ctx.getMatrices(), value, fieldX + HORIZONTAL_PADDING + scrollOffset, fieldY + VERTICAL_PADDING - 0.6f, Color.WHITE.getRGB());
        Scissor.pop();

        if (setting.isFocused()) {
            long now = System.currentTimeMillis();
            if (now - lastBlinkTime > 500) {
                setting.cursorVisible = !setting.cursorVisible;
                lastBlinkTime = now;
            }
            if (setting.cursorVisible) {
                float cursorPos = FontUtils.durman[fontSizeField].getWidth(value.substring(0, setting.getCursorPosition()));
                RenderUtil.drawRoundedRect(ctx.getMatrices(), fieldX + HORIZONTAL_PADDING + cursorPos + scrollOffset, fieldY + VERTICAL_PADDING, 1, FontUtils.durman[fontSizeField].getHeight(), 0, Color.WHITE.getRGB());
            }
        }
    }

    @Override
    public boolean mouseClicked(TextSetting setting, double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        if (button != 0) return false;
        int fontSizeField = 13;
        int nameHeight = (int) FontUtils.durman[fontSizeField].getHeight();
        int fieldY = y + nameHeight + 2;
        String value = setting.getValue();
        int fieldWidth = Math.max(MIN_FIELD_WIDTH, Math.min((int) FontUtils.durman[fontSizeField].getWidth(value) + HORIZONTAL_PADDING * 2, MAX_FIELD_WIDTH));
        int fieldHeight = (int) (FontUtils.durman[fontSizeField].getHeight() + VERTICAL_PADDING * 2);
        int fieldX = x + PADDING;

        boolean inside = mouseX >= fieldX && mouseX <= fieldX + fieldWidth && mouseY >= fieldY && mouseY <= fieldY + fieldHeight;
        setting.setFocused(inside);
        if (inside) setting.setCursorPosition(value.length());
        return inside;
    }

    @Override
    public boolean keyPressed(TextSetting setting, int keyCode, int scanCode, int modifiers) {
        if (!setting.isFocused()) return false;
        String value = setting.getValue();
        if (keyCode == GLFW.GLFW_KEY_ENTER) setting.setFocused(false);
        else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && setting.getCursorPosition() > 0) {
            value = value.substring(0, setting.getCursorPosition() - 1) + value.substring(setting.getCursorPosition());
            setting.setCursorPosition(setting.getCursorPosition() - 1);
            setting.setValue(value);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_DELETE && setting.getCursorPosition() < value.length()) {
            value = value.substring(0, setting.getCursorPosition()) + value.substring(setting.getCursorPosition() + 1);
            setting.setValue(value);
            return true;
        } else if (keyCode == GLFW.GLFW_KEY_LEFT) setting.setCursorPosition(Math.max(0, setting.getCursorPosition() - 1));
        else if (keyCode == GLFW.GLFW_KEY_RIGHT) setting.setCursorPosition(Math.min(value.length(), setting.getCursorPosition() + 1));
        else if (keyCode == GLFW.GLFW_KEY_ESCAPE) setting.setFocused(false);
        return true;
    }

    @Override
    public boolean charTyped(TextSetting setting, char c, int modifiers) {
        if (!setting.isFocused() || Character.isISOControl(c)) return false;
        String value = setting.getValue();
        value = value.substring(0, setting.getCursorPosition()) + c + value.substring(setting.getCursorPosition());
        setting.setCursorPosition(setting.getCursorPosition() + 1);
        setting.setValue(value);
        return true;
    }

    @Override
    public int getHeight() {
        int fontSizeName = 13;
        int nameHeight = (int) FontUtils.durman[fontSizeName].getHeight();
        int fieldHeight = (int) (FontUtils.durman[13].getHeight() + VERTICAL_PADDING * 3.5f);
        return nameHeight + 2 + fieldHeight;
    }
}
