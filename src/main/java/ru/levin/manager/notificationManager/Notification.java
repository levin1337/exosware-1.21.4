package ru.levin.manager.notificationManager;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Direction;
import ru.levin.manager.IMinecraft;
import ru.levin.util.animations.Animation;
import ru.levin.util.animations.impl.DecelerateAnimation;
import ru.levin.util.color.ColorUtil;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;

import java.awt.*;

public class Notification implements IMinecraft {
    @Getter
    @Setter
    private float x, y;

    @Getter
    private String name;

    @Getter
    private String desc;

    @Getter
    private NotificationType type;

    @Getter
    private long time = System.currentTimeMillis();

    public Animation animation = new DecelerateAnimation(500, 1, Direction.AxisDirection.POSITIVE);
    public Animation animationy = new DecelerateAnimation(500, 1, Direction.AxisDirection.POSITIVE);

    float alpha;
    int times;

    private float width;

    public Notification(NotificationType type, String name, String desc, int time) {
        this.type = type;
        this.name = name;
        this.desc = desc;
        this.times = time;
    }

    public float draw(DrawContext context) {
        float widthName = FontUtils.durman[13].getWidth(name);
        float widthDesc = FontUtils.durman[12].getWidth(desc);
        width = Math.max(widthName, widthDesc) + 40;

        RenderUtil.drawRoundedRect(context.getMatrices(), x, y, width, 24, 4, ColorUtil.reAlphaInt(ColorUtil.interpolateColor(new Color(17, 15, 28, 255).getRGB(), new Color(17, 15, 28, 255).getRGB(), 0.05F), (int) (170 * alpha)));
        RenderUtil.drawRoundedRect(context.getMatrices(), x + 22, y + 3, 1, 18, 0, ColorUtil.reAlphaInt(ColorUtil.interpolateColor(new Color(255, 255, 255, 255).getRGB(), new Color(255, 255, 255, 255).getRGB(), 0.05F), (int) (170 * alpha)));

        type.renderIcon(context.getMatrices(), x + 3.5f, y + 4, ColorUtil.rgba(255, 255, 255, (int) (240 * alpha)));

        FontUtils.durman[13].drawLeftAligned(context.getMatrices(), name, x + 28, y + 3, ColorUtil.rgba(255, 255, 255, (int) (240 * alpha)));
        FontUtils.durman[12].drawLeftAligned(context.getMatrices(), desc, x + 28, y + 13, ColorUtil.rgba(210, 210, 210, (int) (240 * alpha)));

        return 24;
    }


    public float getWidth() {
        return width;
    }
}
