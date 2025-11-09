package ru.levin.modules.player;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DeathScreen;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Formatting;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.EventUpdate;
import ru.levin.manager.ClientManager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.player.TimerUtil;

@SuppressWarnings("All")
@FunctionAnnotation(name = "GodMode", type = Type.Player, desc = "ГодМод а катлаван ебаная паста")
public class GodMode extends Function {
    TimerUtil timerUtil = new TimerUtil();
    private long lastClickTime = 0;
    private boolean firstClick = true;

    public GodMode() {
        addSettings();
    }

    public void onEvent(Event event) {
        if (event instanceof EventPacket eventPacket) {
            if (eventPacket.getPacket() instanceof GameMessageS2CPacket packet) {
                String message = packet.content().getString();

                if (message.contains("Вы не можете телепортироваться в PVP режиме") || message.contains("Вы успешно телепортированы на варп farm!")) {
                    eventPacket.setCancel(true);
                }
            }
        }
        if (event instanceof EventUpdate) {
            if (timerUtil.hasTimeElapsed(8000)) {
                ClientManager.message(Formatting.WHITE + "При включенном GodMode" + Formatting.RED + " НЕЛЬЗЯ " + Formatting.WHITE + "использовать любые свапы");
            }
            timerUtil.reset();
            if (mc.currentScreen instanceof DeathScreen) {
                toggle();
            }
        }

        if (event instanceof EventUpdate) {
            if (ClientManager.playerIsPVP()) {
                long currentTime = System.currentTimeMillis();

                if (currentTime - lastClickTime >= 35) {
                    lastClickTime = currentTime;

                    MinecraftClient.getInstance().execute(() -> mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 1, 1, SlotActionType.PICKUP, mc.player));
                }

            }
        }
    }

    @Override
    public void onEnable() {
        firstClick = true;
        lastClickTime = System.currentTimeMillis();
        if (!ClientManager.playerIsPVP()) {
            mc.player.networkHandler.sendCommand("warp");

            new Thread(() -> {
                try {
                    Thread.sleep(500);
                    MinecraftClient.getInstance().execute(() -> mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 21, 1, SlotActionType.PICKUP, mc.player));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

            new Thread(() -> {
                try {
                    Thread.sleep(700);
                    MinecraftClient.getInstance().execute(() -> {
                        mc.currentScreen = null;
                        mc.mouse.lockCursor();
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}