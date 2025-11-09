package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.manager.commandManager.Command;
import ru.levin.manager.commandManager.impl.args.FriendArgumentType;
import ru.levin.manager.commandManager.impl.args.PlayerArgumentType;
import ru.levin.manager.friendManager.FriendManager;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class FriendCommand extends Command {

    public FriendCommand() {
        super("friend");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
                .then(arg("player", PlayerArgumentType.create())
                        .executes(context -> {
                            String name = context.getArgument("player", String.class);
                            if (Manager.FRIEND_MANAGER.isFriend(name)) {
                                ClientManager.message("Уже добавлен " + name);
                                return SINGLE_SUCCESS;
                            }
                            Manager.FRIEND_MANAGER.addFriend(name);
                            ClientManager.message(name + " добавлен в друзья");
                            return SINGLE_SUCCESS;
                        })));

        builder.then(literal("remove")
                .then(arg("player", FriendArgumentType.create())
                        .executes(context -> {
                            String name = context.getArgument("player", String.class);
                            Manager.FRIEND_MANAGER.removeFriend(name);
                            ClientManager.message(name + " удален из друзей");
                            return SINGLE_SUCCESS;
                        })));

        builder.then(literal("clear").executes(context -> {
            clearFriendList();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("list")
                .executes(context -> {
                    if (Manager.FRIEND_MANAGER.getFriends().isEmpty()) {
                        ClientManager.message("Список друзей пуст");
                    } else {
                        String friendsList = Manager.FRIEND_MANAGER.getFriends().stream()
                                .map(f -> f.getName())
                                .reduce((a, b) -> a + ", " + b)
                                .orElse("");
                        ClientManager.message("Друзья: " + friendsList);
                    }
                    return SINGLE_SUCCESS;
                }));
    }

    private void clearFriendList() {
        FriendManager friendManager = Manager.FRIEND_MANAGER;
        if (friendManager.getFriends().isEmpty()) {
            ClientManager.message("Список друзей пуст");
            return;
        }

        friendManager.clearFriends();
        ClientManager.message("Список друзей успешно очищен");
    }
}
