package ru.levin.screens.dropdown.impl;

import net.minecraft.client.gui.DrawContext;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.screens.dropdown.SettingRenderer;
import ru.levin.util.color.ColorUtil;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.render.Scissor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ModeSettingRenderer implements SettingRenderer<ModeSetting>, IMinecraft {

    private static final int TITLE_HEIGHT = 14;
    private static final int MODS_Y_OFFSET = 1;
    private static final int BOX_HEIGHT = 12;
    private static final int LINE_SPACING = 2;
    private static final int PADDING_HORIZONTAL = 3;
    private static final int START_X_OFFSET = 0;

    private final Map<String, Float> scrollOffsets = new HashMap<>();
    private final Map<String, Float> hoverProgress = new HashMap<>();

    public int getHeight(ModeSetting setting, int width) {
        int lineHeight = BOX_HEIGHT + LINE_SPACING;

        int currentX = START_X_OFFSET;
        int lines = 1;

        for (String mode : setting.getModes()) {
            int textWidth = (int) FontUtils.durman[13].getWidth(mode);
            int boxWidth = textWidth + PADDING_HORIZONTAL * 2;

            if (currentX + boxWidth > width) {
                currentX = START_X_OFFSET;
                lines++;
            }

            currentX += boxWidth + 4;
        }

        return TITLE_HEIGHT + lines * lineHeight;
    }

    @Override
    public int getHeight() {
        return TITLE_HEIGHT + BOX_HEIGHT + LINE_SPACING;
    }

    @Override
    public void render(DrawContext ctx, ModeSetting setting, int x, int y, int width, int height) {
        double mouseX = mc.mouse.getX() / mc.getWindow().getScaleFactor();
        double mouseY = mc.mouse.getY() / mc.getWindow().getScaleFactor();

        FontUtils.durman[13].drawLeftAligned(ctx.getMatrices(), setting.getName(), x, y, Color.WHITE.getRGB());

        int startX = x + START_X_OFFSET;
        int spacing = 4;
        int lineHeight = BOX_HEIGHT + LINE_SPACING;

        int currentX = startX;
        int currentY = y + TITLE_HEIGHT - MODS_Y_OFFSET;

        for (String mode : setting.getModes()) {
            int textWidth = (int) FontUtils.durman[13].getWidth(mode);
            int boxWidth = textWidth + PADDING_HORIZONTAL * 2;

            if (currentX + boxWidth > x + width) {
                currentX = startX;
                currentY += lineHeight;
            }

            boolean selected = mode.equals(setting.get());
            boolean hovered = RenderUtil.isInRegion(mouseX, mouseY, currentX, currentY, boxWidth, BOX_HEIGHT);

            float hoverProg = hoverProgress.getOrDefault(mode, 0f);
            if (selected) {
                hoverProg = 1f;
            } else {
                hoverProg += (hovered ? 1f : 0f - hoverProg) * 0.08f;
            }
            hoverProgress.put(mode, hoverProg);

            Color baseColor = new Color(30, 30, 30, 180);
            int selectedColor = Manager.STYLE_MANAGER.getFirstColor();

            int bgColor;
            if (selected) {
                bgColor = selectedColor;
            } else {
                bgColor = ColorUtil.interpolateColor(baseColor.getRGB(), baseColor.getRGB(), hoverProg);
            }

            Color baseTextColor = new Color(200, 200, 200);
            Color hoverTextColor = Color.WHITE;
            int textColor = ColorUtil.interpolateColor(baseTextColor.getRGB(), hoverTextColor.getRGB(), hoverProg);

            RenderUtil.drawRoundedRect(ctx.getMatrices(), currentX, currentY, boxWidth, BOX_HEIGHT, 1, bgColor);

            float maxTextWidth = boxWidth - PADDING_HORIZONTAL * 2;
            float overflow = textWidth - maxTextWidth;

            float offset = scrollOffsets.getOrDefault(mode, 0f);
            if (hovered && overflow > 0) {
                offset = Math.min(offset + 0.5f, overflow);
            } else {
                offset = Math.max(offset - 0.5f, 0);
            }
            scrollOffsets.put(mode, offset);

            Scissor.push();
            Scissor.setFromComponentCoordinates(currentX + PADDING_HORIZONTAL, currentY, maxTextWidth, BOX_HEIGHT);
            FontUtils.durman[13].drawLeftAligned(ctx.getMatrices(), mode, currentX + PADDING_HORIZONTAL - offset, currentY + (BOX_HEIGHT - FontUtils.durman[13].getHeight()) / 2f, textColor);
            Scissor.pop();

            currentX += boxWidth + spacing;
        }
    }

    @Override
    public boolean mouseClicked(ModeSetting setting, double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        if (button != 0) return false;

        int startX = x + START_X_OFFSET;
        int spacing = 4;
        int lineHeight = BOX_HEIGHT + LINE_SPACING;

        int currentX = startX;
        int currentY = y + TITLE_HEIGHT - MODS_Y_OFFSET;

        for (String mode : setting.getModes()) {
            int textWidth = (int) FontUtils.durman[13].getWidth(mode);
            int boxWidth = textWidth + PADDING_HORIZONTAL * 2;

            if (currentX + boxWidth > x + width) {
                currentX = startX;
                currentY += lineHeight;
            }

            if (RenderUtil.isInRegion((int) mouseX, (int) mouseY, currentX, currentY, boxWidth, BOX_HEIGHT)) {
                setting.set(mode);
                return true;
            }

            currentX += boxWidth + spacing;
        }

        return false;
    }
}
