package ru.levin.modules.combat;

import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import ru.levin.events.Event;
import ru.levin.events.impl.player.EventAttack;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;


@SuppressWarnings("All")
@FunctionAnnotation(name = "Criticals", type = Type.Combat,desc = "при ударе без прыжка наносит критический удар")
public class Criticals extends Function {

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventAttack) {
            packet(0.01250004768372);
        }
    }
    private void packet(double y){
        if (mc.player == null || mc.world == null)
            return;
        if ((mc.player.isOnGround() || mc.player.getAbilities().flying ||  mc.player.isTouchingWater() || !mc.player.isInLava() && !mc.player.isSubmergedInWater())) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY() + y, mc.player.getZ(), false, true));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(mc.player.getX(), mc.player.getY(), mc.player.getZ(), false, true));
        }
    }
}