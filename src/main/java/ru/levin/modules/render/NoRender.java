package ru.levin.modules.render;

import ru.levin.modules.setting.MultiSetting;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

import java.util.Arrays;

@FunctionAnnotation(name = "NoRender", type = Type.Render, desc = "Убирает разные типы на экране")
public class NoRender extends Function {
    public MultiSetting mods = new MultiSetting(
            "Убрать",
            Arrays.asList("Тряска камеры", "Огонь на экране", "Вода на экране","Удушье","Плохие эффекты"),
            new String[]{"Тряска камеры", "Огонь на экране", "Вода на экране", "Удушье", "Скорборд","Плохие эффекты"}
    );

    public NoRender() {
        addSettings(mods);
    }
    @Override
    public void onEvent(Event event) {

    }
}