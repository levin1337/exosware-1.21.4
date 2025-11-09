package ru.levin.modules.player;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.ingame.SignEditScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.util.player.TimerUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@FunctionAnnotation(name = "GuiWalk",keywords = {"InventoryMove","GuiMove"}, type = Type.Player)
public class GuiWalk extends Function {
    public final ModeSetting bypass = new ModeSetting("Тип","Обычный","Обычный","FunTime");

    private final Queue<ClickSlotC2SPacket> packetQueue = new ConcurrentLinkedQueue<>();
    private boolean wasInventoryOpen = false;
    private final TimerUtil timer = new TimerUtil();

    public GuiWalk() {
        addSettings(bypass);
    }

    @Override
    public void onEvent(Event event) {
        List<KeyBinding> keyBindings = new ArrayList<>(Arrays.asList(
                mc.options.forwardKey,
                mc.options.backKey,
                mc.options.leftKey,
                mc.options.rightKey,
                mc.options.jumpKey
        ));

        if (mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof SignEditScreen) {
            for (KeyBinding keyBinding : keyBindings) {
                keyBinding.setPressed(false);
            }
            return;
        }
        if (bypass.is("FunTime") && !packetQueue.isEmpty()) {
            if (!timer.hasTimeElapsed(100)) {
                for (KeyBinding keyBinding : keyBindings) {
                    keyBinding.setPressed(false);
                }
                return;
            }
        }

        keyBindings.forEach(this::updateKeyBinding);

        boolean isInventoryOpen = mc.currentScreen instanceof InventoryScreen;

        if (bypass.is("FunTime")) {
            if (isInventoryOpen) {
                wasInventoryOpen = true;
            } else if (wasInventoryOpen) {
                sendQueuedPackets();
                wasInventoryOpen = false;
                timer.reset();
            }
        } else {
            packetQueue.clear();
        }
    }

    private void updateKeyBinding(KeyBinding keyBinding) {
        long handle = mc.getWindow().getHandle();
        int code = keyBinding.getDefaultKey().getCode();
        keyBinding.setPressed(InputUtil.isKeyPressed(handle, code));
    }

    public void queuePacket(ClickSlotC2SPacket packet) {
        if (bypass.is("FunTime") ) {
            packetQueue.add(packet);
        } else if (mc.getNetworkHandler() != null) {
            mc.getNetworkHandler().sendPacket(packet);
        }
    }

    private void sendQueuedPackets() {
        new Thread(() -> {
            try {
                Thread.sleep(80);
                while (!packetQueue.isEmpty()) {
                    ClickSlotC2SPacket packet = packetQueue.poll();
                    if (packet != null && mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(packet);
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
