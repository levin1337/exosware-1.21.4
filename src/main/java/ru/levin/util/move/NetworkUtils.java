package ru.levin.util.move;

import lombok.experimental.UtilityClass;
import net.minecraft.network.packet.Packet;
import ru.levin.manager.IMinecraft;

@UtilityClass
public class NetworkUtils implements IMinecraft {
    private boolean sendingSilent = false;
    public void sendSilentPacket(Packet<?> packet) {
        try {
            sendingSilent = true;
            mc.player.networkHandler.sendPacket(packet);
        } finally {
            sendingSilent = false;
        }
    }

    public void sendPacket(Packet<?> packet) {
        mc.player.networkHandler.sendPacket(packet);
    }

    public boolean isSendingSilent() {
        return sendingSilent;
    }
}
