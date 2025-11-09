package ru.levin.manager.notificationManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import ru.levin.manager.IMinecraft;
import ru.levin.util.math.MathUtil;

import java.util.concurrent.CopyOnWriteArrayList;

public class NotificationManager implements IMinecraft {

    private final CopyOnWriteArrayList<Notification> notifications = new CopyOnWriteArrayList<>();

    public void add(NotificationType type, String name, String desc, int time) {
        notifications.add(new Notification(type, name, desc, time));
    }

    public void draw(DrawContext context) {
        int yoffset = 0;

        for (Notification notification : notifications) {
            long timePassed = System.currentTimeMillis() - notification.getTime();
            long totalDuration = notification.times * 1000L;

            if (timePassed > totalDuration - 222) {
                notification.animation.setDirection(Direction.AxisDirection.NEGATIVE);
            }

            notification.alpha = (float) notification.animation.getOutput();

            if (timePassed > totalDuration) {
                notification.animationy.setDirection(Direction.AxisDirection.NEGATIVE);
            }

            if (notification.animationy.finished(Direction.AxisDirection.NEGATIVE)) {
                notifications.remove(notification);
                continue;
            }

            float fixedRightPadding = 8f;
            float width = notification.getWidth();
            float baseX = mc.getWindow().getScaledWidth() - width - fixedRightPadding;
            float baseY = mc.getWindow().getScaledHeight() - 30;

            notification.animationy.setEndPoint(yoffset * 1.1f);
            notification.animationy.setDuration(300);

            float y;
            if (notification.animationy.getDirection() == Direction.AxisDirection.NEGATIVE) {
                y = baseY - (float)(notification.draw(context) * notification.animationy.getOutput());
            } else {
                y = baseY - notification.draw(context) * yoffset * 1.1f;
            }

            float x = baseX;
            if (notification.animation.getDirection() == Direction.AxisDirection.NEGATIVE) {
                double output = notification.animation.getOutput();
                x = baseX + (float)(width * (1.0 - output));
            }

            notification.setX(x);
            if (y <= notification.getY()) {
                notification.setY(y);
            } else {
                notification.setY(MathUtil.fast(notification.getY(), y, 10));
            }

            yoffset++;
        }
    }

}
