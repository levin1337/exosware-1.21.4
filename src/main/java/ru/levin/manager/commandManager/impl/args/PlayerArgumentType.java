package ru.levin.manager.commandManager.impl.args;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import ru.levin.manager.IMinecraft;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PlayerArgumentType implements ArgumentType<String>, IMinecraft {

    public static PlayerArgumentType create() {
        return new PlayerArgumentType();
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        return reader.readString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> onlinePlayers = mc.player.networkHandler
                .getPlayerList().stream()
                .map(entry -> entry.getProfile().getName())
                .toList();
        return CommandSource.suggestMatching(onlinePlayers, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return List.of("Steve", "Alex", "Notch", "levinPaster");
    }
}
