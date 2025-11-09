package ru.levin.modules.movement;

import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.move.EventNoSlow;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.ModeSetting;

@FunctionAnnotation(name = "NoSlow", desc = "Предотвращает замедление при использовании предметов", type = Type.Move)
public class NoSlow extends Function {

    private final ModeSetting mode = new ModeSetting("Режим", "Grim", "Grim","ReallyWorld","LonyGrief");

    private int ticks;

    public NoSlow() {
        addSettings(mode);
    }
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (mode.is("ReallyWorld")) {
                if (!mc.player.isGliding()) {
                    if (mc.player.isUsingItem()) {
                        ticks++;
                    } else {
                        ticks = 0;
                    }
                }
            }
        }
        if (event instanceof EventNoSlow eventNoSlow) {
            if (mode.is("Grim")) {
                eventNoSlow.setCancel(true);
            } else if (mode.is("ReallyWorld")) {
                if (ticks == 1 || ticks == 2) {
                    eventNoSlow.setCancel(true);
                }
                if (ticks >= 2) {
                    ticks = 0;
                }
                if (ticks == 0) {
                    eventNoSlow.setCancel(false);
                }
            }
            if (mode.is("LonyGrief")) {
                Hand active = mc.player.getActiveHand();
                if (active != null) {
                    Hand opposite = (active == Hand.MAIN_HAND) ? Hand.OFF_HAND : Hand.MAIN_HAND;
                    mc.player.networkHandler.sendPacket(new PlayerInteractItemC2SPacket(opposite, 0, mc.player.getYaw(), mc.player.getPitch()));
                    eventNoSlow.setCancel(true);
                }
            }
        }
    }
}
