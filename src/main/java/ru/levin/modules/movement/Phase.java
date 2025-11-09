package ru.levin.modules.movement;

import net.minecraft.block.BlockState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.move.NetworkUtils;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@SuppressWarnings("All")
@FunctionAnnotation(name = "Phase", desc = "Позволяет ходить в блоках по x,z на ReallyWorld", type = Type.Move)
public class Phase extends Function {
    private final List<Packet<?>> bufferedPackets = new CopyOnWriteArrayList<>();

    private boolean semiPacketSent;
    private boolean skipReleaseOnDisable;

    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) {
            toggle();
        }

        if (event instanceof EventPacket ep) {
            if (ep.isSendPacket()) {
                Packet<?> packet = ep.getPacket();
                if (packet instanceof PlayerMoveC2SPacket) {
                    bufferedPackets.add(packet);
                    ep.setCancel(true);
                }
            }
        }

        if (event instanceof EventUpdate) {
            mc.player.setVelocity(mc.player.getVelocity().x, 0.0, mc.player.getVelocity().z);

            Box box = mc.player.getBoundingBox().expand(0.001D);

            int minX = MathHelper.floor(box.minX);
            int minY = MathHelper.floor(box.minY);
            int minZ = MathHelper.floor(box.minZ);
            int maxX = MathHelper.floor(box.maxX);
            int maxY = MathHelper.floor(box.maxY);
            int maxZ = MathHelper.floor(box.maxZ);

            long totalStates = 0;
            long solidStates = 0;

            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        BlockPos pos = new BlockPos(x, y, z);
                        BlockState state = mc.world.getBlockState(pos);

                        totalStates++;
                        if (state.isSolid()) {
                            solidStates++;
                        }
                    }
                }
            }

            boolean noSolidInAABB = solidStates == 0;
            boolean semiInsideBlock = solidStates > 0 && solidStates < totalStates;

            if (!semiPacketSent && semiInsideBlock) {
                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();
                float yaw = mc.player.getYaw();
                float pitch = mc.player.getPitch();
                boolean onGround = mc.player.isOnGround();

                for (int i = 0; i < 2; i++) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround, false));
                }
                semiPacketSent = true;
                return;
            }

            if (semiPacketSent && noSolidInAABB) {
                skipReleaseOnDisable = true;
                toggle();
            }
        }
    }

    @Override
    public void onDisable() {
        if (!skipReleaseOnDisable && semiPacketSent) {
            double x = mc.player.getX();
            double y = mc.player.getY();
            double z = mc.player.getZ();
            float yaw = mc.player.getYaw();
            float pitch = mc.player.getPitch();
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x - 5000, y, z - 5000, yaw, pitch, false,false));
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, mc.player.isOnGround(),false));
        }

        if (mc.player != null && mc.player.networkHandler != null && !bufferedPackets.isEmpty()) {
            for (Packet<?> packet : bufferedPackets) {
                NetworkUtils.sendSilentPacket(packet);
            }
            bufferedPackets.clear();
        }

        super.onDisable();
    }

    @Override
    public void onEnable() {
        bufferedPackets.clear();
        semiPacketSent = false;
        skipReleaseOnDisable = false;
        super.onEnable();
    }
}
