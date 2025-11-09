package ru.levin.modules.player;

import ru.levin.modules.setting.MultiSetting;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import java.util.Arrays;

@FunctionAnnotation(name = "NoPush" ,desc  = "Убивает коллизию от разных типов", type = Type.Player)
public class NoPush extends Function {
    public MultiSetting mods = new MultiSetting(
            "Типы",
            Arrays.asList("Игроки", "Блоки"),
            new String[]{"Вода", "Игроки", "Блоки"}
    );
    public NoPush() {
        addSettings(mods);
    }

    @Override
    public void onEvent(Event event) {
    }
}
