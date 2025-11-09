package ru.levin.modules.misc;

import org.lwjgl.glfw.GLFW;
import ru.levin.modules.setting.BindSetting;
import ru.levin.events.Event;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.screens.unhook.UnHookScreen;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.DosFileAttributeView;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@FunctionAnnotation(name = "UnHook",keywords = {"SelfDestruct"}, desc = "Отключение чита для прохождения проверки", type = Type.Misc)
public class UnHook extends Function {
    public static BindSetting unHookKey = new BindSetting("Кнопка возврата", GLFW.GLFW_KEY_INSERT);
    public static final List<Function> functionsToBack = new CopyOnWriteArrayList<>();
    public UnHook() {
        addSettings(unHookKey);
    }


    @Override
    public void onEvent(Event event) {
    }

    @Override
    protected void onEnable() {
        mc.setScreen(new UnHookScreen());
        super.onEnable();
    }

    public void onUnhook() {
        functionsToBack.clear();
        for (int i = 0; i < Manager.FUNCTION_MANAGER.getFunctions().size(); i++) {
            Function function = Manager.FUNCTION_MANAGER.getFunctions().get(i);
            if (function.state && function != this) {
                functionsToBack.add(function);
                function.setState(false);
            }
        }
        File folder = new File("C:\\ExosWare");

        if (folder.exists()) {
            try {
                Path folderPathObj = folder.toPath();
                DosFileAttributeView attributes = Files.getFileAttributeView(folderPathObj, DosFileAttributeView.class);
                attributes.setHidden(true);
            } catch (IOException e) {
            }

        }
        toggle();
    }
}
