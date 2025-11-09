package ru.levin.screens.dropdown.impl;

import net.minecraft.client.gui.DrawContext;
import ru.levin.manager.ClientManager;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.modules.setting.BindBooleanSetting;
import ru.levin.screens.dropdown.DescriptionRenderQueue;
import ru.levin.screens.dropdown.SettingRenderer;
import ru.levin.util.color.ColorUtil;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.render.Scissor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BindBooleanSettingRenderer implements SettingRenderer<BindBooleanSetting>, IMinecraft {

    private static final int HEIGHT = 16;
    private static final int SWITCH_WIDTH = 22;
    private static final int SWITCH_HEIGHT = 12;

    private final Map<BindBooleanSetting, Float> toggleProgressMap = new HashMap<>();
    private final Map<BindBooleanSetting, Float> scrollOffsetMap = new HashMap<>();
    private final Map<BindBooleanSetting, Float> bindBoxProgressMap = new HashMap<>();

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void render(DrawContext ctx, BindBooleanSetting setting, int x, int y, int width, int height) {
        float progress = toggleProgressMap.getOrDefault(setting, setting.get() ? 1f : 0f);
        float target = setting.get() ? 1f : 0f;
        progress += (target - progress) * 0.15f;
        toggleProgressMap.put(setting, progress);

        double mouseX = mc.mouse.getX() / mc.getWindow().getScaleFactor();
        double mouseY = mc.mouse.getY() / mc.getWindow().getScaleFactor();

        if (setting.expanded) {
            float bindBoxProgress = bindBoxProgressMap.getOrDefault(setting, 0f);
            bindBoxProgress += (1f - bindBoxProgress) * 0.2f;
            if (bindBoxProgress > 0.99f) bindBoxProgress = 1f;
            bindBoxProgressMap.put(setting, bindBoxProgress);

            long now = System.currentTimeMillis();
            if (now - setting.lastDotUpdate >= 400) {
                setting.dotState = (setting.dotState + 1) % 4;
                setting.lastDotUpdate = now;
            }
            String dots = setting.isListeningForBind() ? ".".repeat(setting.dotState) : "";
            String displayText = setting.isListeningForBind() ? "Binding" + dots : (setting.getBindKey() != -1 ? ClientManager.getKey(setting.getBindKey()) : "NONE" + dots);

            float textWidth = FontUtils.durman[13].getWidth(displayText);
            int bindBoxWidth = (int) (10 + textWidth);
            int bindBoxHeight = 14;
            int bindBoxTargetX = x - 2;
            int bindBoxY = y + (HEIGHT - bindBoxHeight) / 2;

            int bindBoxX = (int) (bindBoxTargetX - (bindBoxWidth + 10) * (1 - bindBoxProgress));
            RenderUtil.drawRoundedRect(ctx.getMatrices(), bindBoxX, bindBoxY, bindBoxWidth, bindBoxHeight, 3, new Color(40, 40, 40, 200).getRGB());

            float maxTextWidth = bindBoxWidth - 10;
            boolean isTextHovered = RenderUtil.isInRegion(mouseX, mouseY, bindBoxX, bindBoxY, bindBoxWidth, bindBoxHeight);
            float offset = scrollOffsetMap.getOrDefault(setting, 0f);
            float overflow = textWidth - maxTextWidth;
            if (isTextHovered && overflow > 0) {
                offset = Math.min(offset + 0.1f, overflow);
            } else {
                offset = Math.max(offset - 0.1f, 0);
            }
            scrollOffsetMap.put(setting, offset);

            FontUtils.durman[13].drawLeftAligned(ctx.getMatrices(), displayText, bindBoxX + 5 - offset, bindBoxY + (bindBoxHeight - FontUtils.durman[13].getHeight()) / 2f, Color.WHITE.getRGB());

            int closeSize = 12;
            int paddingRightClose = 8;
            int closeX = x + width - closeSize - paddingRightClose + 10;
            int closeY = bindBoxY + (bindBoxHeight - closeSize) / 2 - 1;
            RenderUtil.drawTexture(ctx.getMatrices(), "images/gui/fl.png", closeX, closeY, closeSize, closeSize, 0, Color.RED.getRGB());

            return;
        } else {
            float bindBoxProgress = bindBoxProgressMap.getOrDefault(setting, 0f);
            bindBoxProgress += (0f - bindBoxProgress) * 0.2f;
            if (bindBoxProgress < 0.01f) bindBoxProgress = 0f;
            bindBoxProgressMap.put(setting, bindBoxProgress);
        }

        int switchX = x + width - SWITCH_WIDTH + 4;
        int switchY = y + (HEIGHT - SWITCH_HEIGHT) / 2 - 2;
        Color offColor = new Color(50, 50, 50, 200);
        int onColor = Manager.STYLE_MANAGER.getFirstColor();
        int bgColor = ColorUtil.interpolateColor(offColor.getRGB(), onColor, progress);

        RenderUtil.drawRoundedRect(ctx.getMatrices(), switchX, switchY, SWITCH_WIDTH, SWITCH_HEIGHT, 5, bgColor);
        int knobRadius = 8;
        float knobX = switchX + 3 + (SWITCH_WIDTH - knobRadius - 5) * progress;
        float knobY = switchY + (SWITCH_HEIGHT - knobRadius) / 2f;
        RenderUtil.drawCircle(ctx.getMatrices(), knobX + knobRadius / 2f, knobY + knobRadius / 2f, knobRadius, Color.WHITE.getRGB());

        int textX = x;
        int textY = (int) (y + (HEIGHT - FontUtils.durman[13].getHeight()) / 2) - 2;
        String text = setting.getName();
        float textWidth = FontUtils.durman[13].getWidth(text);
        float maxTextWidth = switchX - textX - 15;
        float overflow = textWidth - maxTextWidth;
        boolean shouldScroll = RenderUtil.isInRegion(mouseX, mouseY, x, y, width, height) && overflow > 0;
        float offset = scrollOffsetMap.getOrDefault(setting, 0f);
        if (shouldScroll) offset = Math.min(offset + 0.5f, overflow);
        else offset = Math.max(offset - 0.5f, 0);
        scrollOffsetMap.put(setting, offset);

        Scissor.push();
        Scissor.setFromComponentCoordinates(textX, textY - 1, maxTextWidth, FontUtils.durman[13].getHeight() + 2);
        FontUtils.durman[13].drawLeftAligned(ctx.getMatrices(), text, textX - offset, textY, Color.WHITE.getRGB());
        Scissor.pop();

        FontUtils.iconsWex[24].centeredDraw(ctx.getMatrices(), "H", x + width - 25, y + HEIGHT / 2f - 6, Color.WHITE.getRGB());


        double scale = mc.getWindow().getScaleFactor();
        double mX = mc.mouse.getX() / scale;
        double mY = mc.mouse.getY() / scale;
        boolean isHovered = mX >= switchX && mX <= switchX + SWITCH_WIDTH && mY >= switchY && mY <= switchY + SWITCH_HEIGHT;
        if (isHovered && setting.getDesc() != null && !setting.getDesc().isEmpty()) {
            DescriptionRenderQueue.add(setting.getDesc(), (float) mX + 6, (float) mY + 6);
        }

    }

    @Override
    public boolean mouseClicked(BindBooleanSetting setting, double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        if (button != 0) return false;

        int switchX = x + width - SWITCH_WIDTH + 4;
        int switchY = y + (HEIGHT - SWITCH_HEIGHT) / 2 - 2;

        if (!setting.expanded && RenderUtil.isInRegion(mouseX, mouseY, switchX, switchY, SWITCH_WIDTH, SWITCH_HEIGHT)) {
            setting.set(!setting.get());
            return true;
        }

        int gearSize = 16;
        int gearX = x + width - 25 - gearSize / 2;
        int gearY = y + (HEIGHT / 2) - gearSize / 2 - 2;
        if (RenderUtil.isInRegion(mouseX, mouseY, gearX, gearY, gearSize, gearSize)) {
            setting.expanded = true;
            setting.setListeningForBind(false);
            return true;
        }

        if (setting.expanded) {
            float bindBoxProgress = bindBoxProgressMap.getOrDefault(setting, 1f);
            float textWidth = FontUtils.durman[13].getWidth(setting.isListeningForBind() ? "Binding" : (setting.getBindKey() != -1 ? ClientManager.getKey(setting.getBindKey()) : "NONE"));
            int bindBoxWidth = (int) (10 + textWidth);
            int bindBoxHeight = 14;
            int bindBoxTargetX = x - 2;
            int bindBoxX = (int) (bindBoxTargetX - (bindBoxWidth + 10) * (1 - bindBoxProgress));
            int bindBoxY = y + (HEIGHT - bindBoxHeight) / 2 - 2;

            if (RenderUtil.isInRegion(mouseX, mouseY, bindBoxX, bindBoxY, bindBoxWidth, bindBoxHeight)) {
                setting.setListeningForBind(true);
                return true;
            }

            int closeSize = 12;
            int paddingRightClose = 8;
            int closeX = x + width - closeSize - paddingRightClose + 10;
            int closeY = bindBoxY + (bindBoxHeight - closeSize) / 2 - 2;
            if (RenderUtil.isInRegion(mouseX, mouseY, closeX, closeY, closeSize, closeSize)) {
                setting.expanded = false;
                setting.setListeningForBind(false);
                return true;
            }
        }

        return false;
    }

}
