package ru.levin.modules.movement;

import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.KeepAliveC2SPacket;
import net.minecraft.util.math.Box;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.move.NetworkUtils;
import ru.levin.util.render.RenderUtil;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

@FunctionAnnotation(name = "Blink", desc = "Задерживает пакеты отправленные на сервер", type = Type.Move)
public class Blink extends Function {
    private final SliderSetting maxTicks = new SliderSetting("Макс. тики", 20f, 1f, 50f, 1f);
    private final CopyOnWriteArrayList<Packet<?>> packetBuffer = new CopyOnWriteArrayList<>();

    private Box playerBoundingBox;
    private int currentTick = 0;

    public Blink() {
        addSettings(maxTicks);
    }

    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) {
            toggle();
            return;
        }

        if (event instanceof EventPacket packetEvent) {
            Packet<?> packet = packetEvent.getPacket();
            if (packetEvent.isSendPacket() && !(packet instanceof KeepAliveC2SPacket)) {
                packetBuffer.add(packet);
                packetEvent.setCancel(true);
            }
        }

        if (event instanceof EventUpdate) {
            currentTick++;
            if (currentTick >= maxTicks.get().intValue()) {
                send();
                currentTick = 0;
            }
        }

        if (event instanceof EventRender3D) {
            if (playerBoundingBox != null) {
                RenderUtil.render3D.drawHoleOutline(playerBoundingBox, Color.WHITE.getRGB(), 2f);
            }
        }
    }

    private void send() {
        if (mc.player == null || mc.world == null || packetBuffer.isEmpty()) return;
        for (Packet<?> packet : packetBuffer) {
            NetworkUtils.sendSilentPacket(packet);
        }
        packetBuffer.clear();
        playerBoundingBox = mc.player.getBoundingBox();
    }

    @Override
    public void onDisable() {
        send();
        playerBoundingBox = null;
        currentTick = 0;
    }
}
