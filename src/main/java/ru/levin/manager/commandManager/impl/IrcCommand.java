package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.command.CommandSource;
import ru.levin.manager.Manager;
import ru.levin.manager.commandManager.Command;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class IrcCommand extends Command {

    public IrcCommand() {
        super("irc");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {

        RequiredArgumentBuilder<CommandSource, String> ignoreArg =
                RequiredArgumentBuilder.<CommandSource, String>argument("nick", StringArgumentType.word())
                        .executes(context -> {
                            String nick = StringArgumentType.getString(context, "nick");
                            Manager.IRC_MANAGER.ignoreNick(nick);
                            Manager.IRC_MANAGER.messageClient(nick + " добавлен в игнор");
                            return SINGLE_SUCCESS;
                        });
        builder.then(literal("ignore").then(ignoreArg));

        RequiredArgumentBuilder<CommandSource, String> unignoreArg =
                RequiredArgumentBuilder.<CommandSource, String>argument("nick", StringArgumentType.word())
                        .executes(context -> {
                            String nick = StringArgumentType.getString(context, "nick");
                            Manager.IRC_MANAGER.unignoreNick(nick);
                            Manager.IRC_MANAGER.messageClient(nick + " удалён из игнора");
                            return SINGLE_SUCCESS;
                        });
        builder.then(literal("unignore").then(unignoreArg));

        builder.then(literal("ignorelist")
                .executes(context -> {
                    if (Manager.IRC_MANAGER.getIgnoredNicks().isEmpty()) {
                        Manager.IRC_MANAGER.messageClient("Список игнора пуст");
                    } else {
                        Manager.IRC_MANAGER.messageClient("Игнор-лист: " + String.join(", ", Manager.IRC_MANAGER.getIgnoredNicks()));
                    }
                    return SINGLE_SUCCESS;
                }));

        RequiredArgumentBuilder<CommandSource, String> msgArg =
                RequiredArgumentBuilder.<CommandSource, String>argument("message", StringArgumentType.greedyString())
                        .executes(context -> {
                            String message = StringArgumentType.getString(context, "message");
                            if (message.matches("(https?://|www\\.)\\S+")) {
                                Manager.IRC_MANAGER.messageClient("Ваше сообщение содержит ссылку.");
                                return SINGLE_SUCCESS;
                            }
                            if (Manager.FUNCTION_MANAGER.irc.state) {
                                Manager.IRC_MANAGER.messageHost(message);
                            } else {
                                Manager.IRC_MANAGER.messageClient("Пожалуйста включите модуль IRC");
                            }
                            return SINGLE_SUCCESS;
                        });
        builder.then(msgArg);
    }

}
