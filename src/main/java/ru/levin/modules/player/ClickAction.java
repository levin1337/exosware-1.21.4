package ru.levin.modules.player;

import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.ModeSetting;

@FunctionAnnotation(
        name = "ClickAction",
        keywords = "SwapAction",
        desc = "Ставит свапы под выбранный тип сервера (чтобы не забанило)",
        type = Type.Player
)
public class ClickAction extends Function {

    public final ModeSetting type =
            new ModeSetting("Тип", "ReallyWorld", "ReallyWorld", "FunTime", "HollyWorld");

    @Override
    public void onEvent(Event event) {
    }

    public final boolean nonBatch() {
        return type.is("ReallyWorld");
    }

    public final boolean batch() {
        return type.is("FunTime") || type.is("HollyWorld");
    }
}
