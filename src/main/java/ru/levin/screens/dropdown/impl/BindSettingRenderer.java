package ru.levin.screens.dropdown.impl;

import net.minecraft.client.gui.DrawContext;
import ru.levin.manager.ClientManager;
import ru.levin.manager.IMinecraft;
import ru.levin.modules.setting.BindSetting;
import ru.levin.screens.dropdown.SettingRenderer;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.render.Scissor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BindSettingRenderer implements SettingRenderer<BindSetting>, IMinecraft {

    private static final int HEIGHT = 15;
    private static final int BUTTON_WIDTH = 12;
    private static final int BUTTON_HEIGHT = 12;
    private static final int BUTTON_RADIUS = 3;
    private static final int PADDING = 0;

    private final Map<BindSetting, Float> nameScrollOffsetMap = new HashMap<>();

    @Override
    public void render(DrawContext ctx, BindSetting setting, int x, int y, int width, int height) {
        String displayText;
        if (setting.isBinding()) {
            long dots = (System.currentTimeMillis() / 400) % 4;
            displayText = "Binding" + ".".repeat((int) dots);
        } else {
            displayText = (setting.getKey() != -1) ? ClientManager.getKey(setting.getKey()) : "NONE";
        }

        int fontSizeName = 13;
        int fontSizeButton = 13;

        int textWidthButton = (int) FontUtils.durman[fontSizeButton].getWidth(displayText);
        int paddingButton = 10;

        int buttonWidth = Math.max(BUTTON_WIDTH, textWidthButton + paddingButton);
        int buttonHeight = BUTTON_HEIGHT;

        int buttonX = x + width - buttonWidth - PADDING;
        int buttonY = y + (HEIGHT - buttonHeight) / 2;

        int nameAreaX = x + PADDING;
        int nameAreaWidth = buttonX - nameAreaX - 4;

        String name = setting.getName();
        float nameTextWidth = FontUtils.durman[fontSizeName].getWidth(name);

        double mouseX = mc.mouse.getX() / mc.getWindow().getScaleFactor();
        double mouseY = mc.mouse.getY() / mc.getWindow().getScaleFactor();

        boolean hoveredNameArea = RenderUtil.isInRegion(mouseX, mouseY, nameAreaX, y, nameAreaWidth, HEIGHT);

        float scrollOffset = nameScrollOffsetMap.getOrDefault(setting, 0f);
        float overflow = nameTextWidth - nameAreaWidth;

        if (hoveredNameArea && overflow > 0) {
            scrollOffset = Math.min(scrollOffset + 1f, overflow);
        } else {
            scrollOffset = Math.max(scrollOffset - 1f, 0);
        }
        nameScrollOffsetMap.put(setting, scrollOffset);

        Scissor.push();
        Scissor.setFromComponentCoordinates(nameAreaX, y + (HEIGHT - FontUtils.durman[fontSizeName].getHeight()) / 2 - 1, nameAreaWidth, FontUtils.durman[fontSizeName].getHeight() + 2);
        FontUtils.durman[fontSizeName].drawLeftAligned(ctx.getMatrices(), name, nameAreaX - scrollOffset, y + (HEIGHT - FontUtils.durman[fontSizeName].getHeight()) / 2, Color.WHITE.getRGB());
        Scissor.pop();

        RenderUtil.drawRoundedRect(ctx.getMatrices(), buttonX, buttonY, buttonWidth, buttonHeight, BUTTON_RADIUS, new Color(40, 40, 40, 200).getRGB());

        FontUtils.durman[fontSizeButton].centeredDraw(ctx.getMatrices(), displayText, buttonX + buttonWidth / 2f, buttonY + (buttonHeight - FontUtils.durman[fontSizeButton].getHeight()) / 2f, Color.WHITE.getRGB());
    }


    @Override
    public boolean mouseClicked(BindSetting setting, double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        if (button != 0) return false;

        String displayText = (setting.isBinding()) ? "Binding" : (setting.getKey() != -1 ? ClientManager.getKey(setting.getKey()) : "NONE");
        int fontSizeButton = 13;
        int textWidthButton = (int) FontUtils.durman[fontSizeButton].getWidth(displayText);
        int paddingButton = 10;
        int buttonWidth = Math.max(BUTTON_WIDTH, textWidthButton + paddingButton);
        int buttonHeight = BUTTON_HEIGHT;

        int buttonX = x + width - buttonWidth - PADDING;
        int buttonY = y + (HEIGHT - buttonHeight) / 2;

        if (RenderUtil.isInRegion(mouseX, mouseY, buttonX, buttonY, buttonWidth, buttonHeight)) {
            setting.setBinding(!setting.isBinding());
            return true;
        }
        return false;
    }

    @Override
    public int getHeight() {
        return HEIGHT;
    }
}
