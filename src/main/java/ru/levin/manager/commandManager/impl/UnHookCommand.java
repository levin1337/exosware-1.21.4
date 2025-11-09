package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import ru.levin.manager.ClientManager;
import ru.levin.manager.commandManager.Command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class UnHookCommand extends Command {

    public static final File CUSTOM_PATH_FILE = new File(mc.runDirectory, "files\\modules\\UnHook.ew");

    public UnHookCommand() {
        super("unhook");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {

        builder.then(literal("path")
                .then(arg("folder", StringArgumentType.greedyString())
                        .suggests(this::suggestFolders)
                        .executes(context -> {
                            String path = StringArgumentType.getString(context, "folder");
                            savePath(path);
                            return SINGLE_SUCCESS;
                        })));
    }

    private CompletableFuture<Suggestions> suggestFolders(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining();
        File base = new File(input.isEmpty() ? "." : input).getParentFile();
        if (base != null && base.exists() && base.isDirectory()) {
            File[] files = base.listFiles(File::isDirectory);
            if (files != null) {
                for (File f : files) {
                    builder.suggest(f.getAbsolutePath());
                }
            }
        }
        return builder.buildFuture();
    }

    private void savePath(String path) {
        try {
            if (!CUSTOM_PATH_FILE.exists()) {
                CUSTOM_PATH_FILE.getParentFile().mkdirs();
                CUSTOM_PATH_FILE.createNewFile();
            }

            try (FileWriter writer = new FileWriter(CUSTOM_PATH_FILE, false)) {
                writer.write(path.trim());
                ClientManager.message(Formatting.GREEN + "Путь сохранён: " + Formatting.WHITE + path);
            }

        } catch (IOException e) {
            ClientManager.message(Formatting.RED + "Ошибка при сохранении пути: " + e.getMessage());
        }
    }
}
