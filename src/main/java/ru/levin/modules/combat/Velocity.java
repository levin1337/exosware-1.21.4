package ru.levin.modules.combat;

import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "Velocity",keywords = {"AKB","AntiKnockBack"}, type = Type.Combat, desc = "Отключает отбрасывание")
public class Velocity extends Function {
    public ModeSetting mode = new ModeSetting("Тип", "Cancel", "Cancel");

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventPacket eventPacket) {
            if (mode.is("Cancel")) {
                if (eventPacket.getPacket() instanceof EntityVelocityUpdateS2CPacket entityVelocityUpdateS2CPacket) {
                    if (entityVelocityUpdateS2CPacket.getEntityId() == mc.player.getId()) {
                        eventPacket.setCancel(true);
                    }
                }
            }
        }
    }
}