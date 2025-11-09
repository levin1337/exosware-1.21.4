package ru.levin.mixin.util;

import net.minecraft.client.gui.screen.pack.PackScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.levin.manager.ClientManager;
import ru.levin.manager.commandManager.impl.UnHookCommand;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(PackScreen.class)
public abstract class MixinPackScreen {

    @Shadow
    @Final
    @Mutable
    private Path file;

    @Inject(method = "init", at = @At("HEAD"))
    private void onInit(CallbackInfo ci) {
        if (ClientManager.legitMode) {
            try {
                File customFile = UnHookCommand.CUSTOM_PATH_FILE;
                if (customFile.exists()) {
                    String content = Files.readString(customFile.toPath()).trim();

                    if (!content.isEmpty()) {
                        Path customPath = Path.of(content);
                        if (Files.exists(customPath)) {
                            this.file = customPath;
                            System.out.println("Легит режим: Путь к ресурспакам заменён на " + customPath);
                        } else {
                            System.err.println("Путь из файла не существует: " + customPath);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Ошибка при подмене пути к ресурспакам: " + e.getMessage());
            }
        }
    }
}
