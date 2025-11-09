package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.manager.commandManager.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;
public class DragCommand extends Command {

    public DragCommand() {
        super("drag");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> root) {
        root.then(literal("reset")
                .executes(ctx -> {
                    Manager.DRAG_MANAGER.reset();
                    ClientManager.message(Formatting.GREEN + "Позиции элементов на экране были сброшены");
                    return SINGLE_SUCCESS;
                }));

        root.then(literal("save")
                .executes(ctx -> {
                    Manager.DRAG_MANAGER.save();
                    ClientManager.message(Formatting.GREEN + "Элементы на экране были сохранены");
                    return SINGLE_SUCCESS;
                }));

    }
}
