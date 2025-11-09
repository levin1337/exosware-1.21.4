package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.manager.commandManager.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ChestStealerCommand extends Command {

    public ChestStealerCommand() {
        super("cheststealer");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> root) {
        root.then(literal("add")
                .then(arg("item", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (Item item : Registries.ITEM) {
                                String id = Registries.ITEM.getId(item).toString();
                                builder.suggest(id.replace("minecraft:", ""));
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String input = StringArgumentType.getString(ctx, "item");
                            String itemName = input.contains(":") ? input : "minecraft:" + input;

                            if (Manager.CHESTSTEALER_MANAGER.addItem(itemName)) {
                                ClientManager.message(Formatting.GREEN + "Добавлен предмет " + itemName + " в список ChestStealer");
                            } else {
                                ClientManager.message(Formatting.RED + "Не удалось добавить предмет: " + itemName);
                            }
                            return SINGLE_SUCCESS;
                        })));

        root.then(literal("remove")
                .then(arg("item", StringArgumentType.word())
                        .suggests((ctx, builder) -> {
                            for (Item item : Manager.CHESTSTEALER_MANAGER.getWhitelist()) {
                                String id = Registries.ITEM.getId(item).toString();
                                builder.suggest(id.replace("minecraft:", ""));
                            }
                            return builder.buildFuture();
                        })
                        .executes(ctx -> {
                            String input = StringArgumentType.getString(ctx, "item");
                            String itemName = input.contains(":") ? input : "minecraft:" + input;

                            if (Manager.CHESTSTEALER_MANAGER.removeItem(itemName)) {
                                ClientManager.message(Formatting.GREEN + "Удалён предмет " + itemName + " из списка ChestStealer");
                            } else {
                                ClientManager.message(Formatting.RED + "Не удалось удалить предмет: " + itemName);
                            }
                            return SINGLE_SUCCESS;
                        })));

        root.then(literal("list")
                .executes(ctx -> {
                    if (Manager.CHESTSTEALER_MANAGER.getWhitelist().isEmpty()) {
                        ClientManager.message(Formatting.RED + "Добавленных предметов нет!");
                    } else {
                        ClientManager.message(Formatting.GREEN + "Список whitelisted предметов:");
                        for (Item item : Manager.CHESTSTEALER_MANAGER.getWhitelist()) {
                            ClientManager.message(Formatting.GRAY + "- " + Formatting.WHITE + Registries.ITEM.getId(item).toString());
                        }
                    }
                    return SINGLE_SUCCESS;
                }));
    }

}
