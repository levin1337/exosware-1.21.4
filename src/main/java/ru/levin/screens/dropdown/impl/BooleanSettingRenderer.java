package ru.levin.screens.dropdown.impl;

import net.minecraft.client.gui.DrawContext;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.screens.dropdown.DescriptionRenderQueue;
import ru.levin.screens.dropdown.SettingRenderer;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.render.Scissor;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class BooleanSettingRenderer implements SettingRenderer<BooleanSetting>, IMinecraft {

    private static final int HEIGHT = 16, WIDTH = 22, SWITCH_HEIGHT = 12, KNOB_RADIUS = 8;
    private final Map<BooleanSetting, Float> toggleMap = new HashMap<>(), scrollMap = new HashMap<>();

    @Override
    public int getHeight() {
        return HEIGHT;
    }

    @Override
    public void render(DrawContext ctx, BooleanSetting setting, int x, int y, int width, int height) {
        float progress = toggleMap.getOrDefault(setting, setting.get() ? 1f : 0f);
        progress += ((setting.get() ? 1f : 0f) - progress) * 0.15f;
        toggleMap.put(setting, progress);

        int switchX = x + width - WIDTH + 4;
        int switchY = y + (HEIGHT - SWITCH_HEIGHT) / 2 - 2;

        Color offColor = new Color(50, 50, 50, 200);
        int onColor = Manager.STYLE_MANAGER.getFirstColor();
        int bgColor = ColorUtil.interpolateColor(offColor.getRGB(), onColor, progress);

        RenderUtil.drawRoundedRect(ctx.getMatrices(), switchX, switchY, WIDTH, SWITCH_HEIGHT, 5, bgColor);
        RenderUtil.drawCircle(ctx.getMatrices(), switchX + 3 + (WIDTH - KNOB_RADIUS - 5) * progress + KNOB_RADIUS / 2f, switchY + (SWITCH_HEIGHT - KNOB_RADIUS) / 2f + KNOB_RADIUS / 2f, KNOB_RADIUS, Color.WHITE.getRGB());

        var font = FontUtils.durman[13];
        String text = setting.getName();
        int textY = (int) (y + (HEIGHT - font.getHeight()) / 2) - 2;
        float maxTextWidth = switchX - x - 4;
        float textWidth = font.getWidth(text);

        double scale = mc.getWindow().getScaleFactor();
        double mX = mc.mouse.getX() / scale;
        double mY = mc.mouse.getY() / scale;

        float overflow = textWidth - maxTextWidth;
        float offset = scrollMap.getOrDefault(setting, 0f);

        boolean textHovered = RenderUtil.isHovered((int) mX, (int) mY, x, textY - 1, maxTextWidth, font.getHeight() + 2);

        if (textHovered && overflow > 0) {
            offset = Math.min(offset + 0.5f, overflow);
        } else {
            offset = Math.max(offset - 0.5f, 0);
        }
        scrollMap.put(setting, offset);

        Scissor.push();
        Scissor.setFromComponentCoordinates(x, textY - 1, maxTextWidth, font.getHeight() + 2);
        font.drawLeftAligned(ctx.getMatrices(), text, x - offset, textY, Color.WHITE.getRGB());
        Scissor.pop();


        boolean isHovered = mX >= switchX && mX <= switchX + WIDTH && mY >= switchY && mY <= switchY + SWITCH_HEIGHT;
        if (isHovered && setting.getDesc() != null && !setting.getDesc().isEmpty()) {
            DescriptionRenderQueue.add(setting.getDesc(), (float) mX + 6, (float) mY + 6);
        }
    }

    @Override
    public boolean mouseClicked(BooleanSetting setting, double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        if (button != 0) return false;
        int switchX = x + width - WIDTH + 4;
        int switchY = y + (HEIGHT - SWITCH_HEIGHT) / 2 - 2;
        if (mouseX >= switchX && mouseX <= switchX + WIDTH && mouseY >= switchY && mouseY <= switchY + SWITCH_HEIGHT) {
            setting.set(!setting.get());
            return true;
        }
        return false;
    }
}
