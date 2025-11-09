package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import ru.levin.manager.ClientManager;
import ru.levin.manager.commandManager.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class VclipCommand extends Command {

    public VclipCommand() {
        super("vclip");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("up").executes(context -> {
            double offset = findFreeSpace(true);
            teleport(offset);
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("down").executes(context -> {
            double offset = findFreeSpace(false);
            teleport(offset);
            return SINGLE_SUCCESS;
        }));

        builder.then(arg("offset", DoubleArgumentType.doubleArg())
                .executes(context -> {
                    double offset = DoubleArgumentType.getDouble(context, "offset");
                    teleport(offset);
                    return SINGLE_SUCCESS;
                }));
    }

    private void teleport(double offset) {
        if (offset == 0) {
            ClientManager.message(Formatting.RED + "Свободное место не найдено!");
            return;
        }

        double startY = mc.player.getY();
        double endY = startY + offset;

        for (int i = 0; i < 19; i++) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            mc.player.getX(), startY, mc.player.getZ(),
                            false, true
                    )
            );
        }

        for (int i = 0; i < 19; i++) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            mc.player.getX(), endY, mc.player.getZ(),
                            false, true
                    )
            );
        }

        mc.player.setPosition(mc.player.getX(), endY, mc.player.getZ());
        ClientManager.message(Formatting.GREEN + "Вы телепортировались на " + (offset > 0 ? "вверх" : "вниз") +
                " (" + (int) offset + " блоков).");
    }

    private double findFreeSpace(boolean up) {
        BlockPos start = mc.player.getBlockPos();
        int step = up ? 1 : -1;

        for (int i = 1; i < 256; i++) {
            BlockPos checkPos = start.add(0, i * step, 0);
            BlockPos checkPosAbove = checkPos.up();

            if (mc.world.isAir(checkPos) && mc.world.isAir(checkPosAbove)) {
                return i * step;
            }
        }
        return 0;
    }
}
