package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.manager.commandManager.Command;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class StaffCommand extends Command {

    public StaffCommand() {
        super("staff");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> root) {
        root.then(literal("add")
                .then(arg("name", StringArgumentType.word())
                        .suggests(this::suggestOnlinePlayers)
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            if (Manager.STAFF_MANAGER.getStaffNames().contains(name)) {
                                ClientManager.message(Formatting.RED + "Этот игрок уже в Staff List!");
                            } else {
                                Manager.STAFF_MANAGER.addStaff(name);
                                ClientManager.message(Formatting.GREEN + "Ник " + Formatting.WHITE + name + Formatting.GREEN + " добавлен в Staff List");
                            }
                            return SINGLE_SUCCESS;
                        })));

        root.then(literal("remove")
                .then(arg("name", StringArgumentType.word())
                        .suggests(this::suggestExistingStaff)
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            if (Manager.STAFF_MANAGER.getStaffNames().contains(name)) {
                                Manager.STAFF_MANAGER.removeStaff(name);
                                ClientManager.message(Formatting.GREEN + "Ник " + Formatting.WHITE + name + Formatting.GREEN + " удален из Staff List");
                            } else {
                                ClientManager.message(Formatting.RED + "Этого игрока нет в Staff List!");
                            }
                            return SINGLE_SUCCESS;
                        })));

        root.then(literal("clear")
                .executes(ctx -> {
                    if (Manager.STAFF_MANAGER.getStaffNames().isEmpty()) {
                        ClientManager.message(Formatting.RED + "Staff List пуст!");
                    } else {
                        Manager.STAFF_MANAGER.clearStaffs();
                        ClientManager.message(Formatting.GREEN + "Staff List очищен");
                    }
                    return SINGLE_SUCCESS;
                }));

        root.then(literal("list")
                .executes(ctx -> {
                    ClientManager.message(Formatting.GRAY + "Список Staff:");
                    for (String name : Manager.STAFF_MANAGER.getStaffNames()) {
                        ClientManager.message(Formatting.WHITE + name);
                    }
                    return SINGLE_SUCCESS;
                }));

        root.then(literal("reload")
                .executes(ctx -> {
                    Manager.STAFF_MANAGER.reload();
                    ClientManager.message(Formatting.GREEN + "Список был перезагружен");
                    return SINGLE_SUCCESS;
                }));
    }

    private CompletableFuture<Suggestions> suggestExistingStaff(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        List<String> staffNames = Manager.STAFF_MANAGER.getStaffNames();
        return CommandSource.suggestMatching(staffNames, builder);
    }

    private CompletableFuture<Suggestions> suggestOnlinePlayers(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        if (mc.player.networkHandler == null) return builder.buildFuture();
        List<String> playerNames = mc.player.networkHandler
                .getPlayerList()
                .stream()
                .map(p -> p.getProfile().getName())
                .collect(Collectors.toList());
        return CommandSource.suggestMatching(playerNames, builder);
    }
}
