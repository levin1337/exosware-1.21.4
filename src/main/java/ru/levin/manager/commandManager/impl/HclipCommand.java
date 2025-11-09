package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import ru.levin.manager.ClientManager;
import ru.levin.manager.commandManager.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class HclipCommand extends Command {

    public HclipCommand() {
        super("hclip");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(arg("offset", DoubleArgumentType.doubleArg())
                .executes(context -> {
                    double offset = DoubleArgumentType.getDouble(context, "offset");
                    teleport(offset);
                    return SINGLE_SUCCESS;
                }));
    }

    private void teleport(double offset) {
        if (offset == 0) {
            ClientManager.message(Formatting.RED + "Смещение не может быть 0!");
            return;
        }

        Vec3d look = mc.player.getRotationVec(1.0F).normalize();
        double x = mc.player.getX() + look.x * offset;
        double z = mc.player.getZ() + look.z * offset;
        double y = mc.player.getY();

        for (int i = 0; i < 19; i++) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            mc.player.getX(), y, mc.player.getZ(),
                            false, true
                    )
            );
        }

        for (int i = 0; i < 19; i++) {
            mc.player.networkHandler.sendPacket(
                    new PlayerMoveC2SPacket.PositionAndOnGround(
                            x, y, z,
                            false, true
                    )
            );
        }

        mc.player.setPosition(x, y, z);

        ClientManager.message(Formatting.GREEN + "Вы телепортировались на " + Formatting.RED + offset + Formatting.WHITE + " блоков вперёд.");
    }
}
