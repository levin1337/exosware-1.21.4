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
import ru.levin.modules.Function;
import ru.levin.modules.setting.BindBooleanSetting;
import ru.levin.modules.setting.BindSetting;
import ru.levin.modules.setting.Setting;
import ru.levin.util.KeyMappings;

import java.util.concurrent.CompletableFuture;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class BindCommand extends Command {

    public BindCommand() {
        super("bind");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> root) {
        root.then(literal("add")
                .then(arg("module", StringArgumentType.word())
                        .suggests(this::suggestModules)
                        .then(arg("key", StringArgumentType.word())
                                .suggests(this::suggestKeys)
                                .executes(ctx -> {
                                    String moduleName = StringArgumentType.getString(ctx, "module");
                                    String keyName = StringArgumentType.getString(ctx, "key").toUpperCase();
                                    addKeyBinding(moduleName, keyName);
                                    return SINGLE_SUCCESS;
                                }))));

        root.then(literal("remove")
                .then(arg("module", StringArgumentType.word())
                        .suggests(this::suggestModules)
                        .then(arg("key", StringArgumentType.word())
                                .suggests(this::suggestKeys)
                                .executes(ctx -> {
                                    String moduleName = StringArgumentType.getString(ctx, "module");
                                    String keyName = StringArgumentType.getString(ctx, "key").toUpperCase();
                                    removeKeyBinding(moduleName, keyName);
                                    return SINGLE_SUCCESS;
                                }))));

        root.then(literal("list").executes(ctx -> {
            listBoundKeys();
            return SINGLE_SUCCESS;
        }));

        root.then(literal("clear").executes(ctx -> {
            clearAllBindings();
            return SINGLE_SUCCESS;
        }));
    }

    private CompletableFuture<Suggestions> suggestModules(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        Manager.FUNCTION_MANAGER.getFunctions().forEach(f -> builder.suggest(f.name));
        return builder.buildFuture();
    }

    private CompletableFuture<Suggestions> suggestKeys(CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        KeyMappings.getAllKeys().forEach(builder::suggest);
        return builder.buildFuture();
    }

    private void listBoundKeys() {
        ClientManager.message(Formatting.GRAY + "Список всех модулей с клавишами:");
        for (Function f : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (f.bind == 0) continue;
            String keyName = KeyMappings.keyMappings(f.bind);
            ClientManager.message(f.name + " [" + Formatting.GRAY + keyName + Formatting.RESET + "]");
        }
    }

    private void clearAllBindings() {
        for (Function f : Manager.FUNCTION_MANAGER.getFunctions()) {
            if (f == Manager.FUNCTION_MANAGER.clickGUI) continue;
            f.bind = 0;
            for (Setting setting : f.getSettings()) {
                if (setting instanceof BindBooleanSetting bindBooleanSetting) bindBooleanSetting.setKey(0);
                if (setting instanceof BindSetting bindSetting) bindSetting.setKey(-1);
            }
        }
        ClientManager.message(Formatting.GREEN + "Все клавиши были отвязаны (кроме ClickGUI)");
    }

    private void addKeyBinding(String moduleName, String keyName) {
        Function module = Manager.FUNCTION_MANAGER.get(moduleName);
        int key = KeyMappings.keyCode(keyName);

        if (module == null) {
            ClientManager.message("Модуль " + moduleName + " не найден!");
            return;
        }
        if (key == -1) {
            ClientManager.message("Клавиша " + keyName + " не найдена!");
            return;
        }

        module.bind = key;
        ClientManager.message("Клавиша " + Formatting.GRAY + keyName + Formatting.WHITE + " была привязана к модулю " + Formatting.GRAY + module.name);
    }

    private void removeKeyBinding(String moduleName, String keyName) {
        Function module = Manager.FUNCTION_MANAGER.get(moduleName);
        if (module == null) {
            ClientManager.message("Модуль " + moduleName + " не найден!");
            return;
        }
        module.bind = 0;
        ClientManager.message("Клавиша " + Formatting.GRAY + keyName + Formatting.WHITE + " была отвязана от модуля " + Formatting.GRAY + module.name);
    }
}
