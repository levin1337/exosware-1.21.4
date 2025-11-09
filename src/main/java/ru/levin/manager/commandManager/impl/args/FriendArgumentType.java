package ru.levin.manager.commandManager.impl.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import ru.levin.manager.Manager;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class FriendArgumentType implements ArgumentType<String> {
    private static final DynamicCommandExceptionType NOT_FRIEND_EXCEPTION =
            new DynamicCommandExceptionType(name -> Text.literal("У тебя нет друга " + name));

    public static FriendArgumentType create() {
        return new FriendArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        String name = reader.readString();
        if (!Manager.FRIEND_MANAGER.isFriend(name)) {
            throw NOT_FRIEND_EXCEPTION.create(name);
        }
        return name;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> friendNames = Manager.FRIEND_MANAGER.getFriends().stream().map(f -> f.getName()).toList();
        return CommandSource.suggestMatching(friendNames, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return Manager.FRIEND_MANAGER.getFriends().stream().limit(5).map(f -> f.getName()).toList();
    }
}
