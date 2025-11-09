package ru.levin.modules.combat;


import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "SuperBow", type = Type.Combat,desc = "Увеличивает силу урона от лука")
public class SuperBow extends Function {

    private final SliderSetting power = new SliderSetting("Сила", 30, 1, 200, 1);

    public SuperBow() {
        addSettings(power);
    }

    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) return;

        if (event instanceof EventPacket e) {
            if (e.getPacket() instanceof PlayerActionC2SPacket p) {
                if (p.getAction() == PlayerActionC2SPacket.Action.RELEASE_USE_ITEM) {
                    mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player,ClientCommandC2SPacket.Mode.START_SPRINTING));
                    for (int i = 0; i < power.get().intValue(); i++) {
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() - 0.000000001, mc.player.getZ(), true,true));
                        mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + 0.000000001, mc.player.getZ(), true,false));

                    }
                }
            }
        }
    }
}
