package ru.levin.mixin.chat;


import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestions;
import net.minecraft.client.gui.screen.ChatInputSuggestor;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.command.CommandSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.manager.commandManager.CommandManager;


import java.util.concurrent.CompletableFuture;

@Mixin(ChatInputSuggestor.class)
public abstract class MixinChatInputSuggestor {
    @Final @Shadow TextFieldWidget textField;
    @Shadow boolean completingSuggestions;
    @Shadow private ParseResults<CommandSource> parse;
    @Shadow private CompletableFuture<Suggestions> pendingSuggestions;
    @Shadow private ChatInputSuggestor.SuggestionWindow window;

    @Shadow protected abstract void showCommandSuggestions();

    @Inject(method = "refresh", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/StringReader;canRead()Z", remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    public void refreshHook(CallbackInfo ci, String string, StringReader reader) {
        CommandManager commandManager = Manager.COMMAND_MANAGER;
        if (!ClientManager.legitMode) {
            if (reader.canRead(commandManager.getPrefix().length()) && reader.getString().startsWith(commandManager.getPrefix(), reader.getCursor())) {
                reader.setCursor(reader.getCursor() + 1);
                if (parse == null)
                    parse = commandManager.getDispatcher().parse(reader, commandManager.getSource());
                final int cursor = textField.getCursor();
                if (cursor >= 1 && (window == null || !completingSuggestions)) {
                    pendingSuggestions = commandManager.getDispatcher().getCompletionSuggestions(parse, cursor);
                    pendingSuggestions.thenRun(() -> {
                        if (pendingSuggestions.isDone()) showCommandSuggestions();
                    });
                }

                ci.cancel();
            }
        }
    }
}