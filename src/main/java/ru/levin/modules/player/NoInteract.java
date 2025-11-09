package ru.levin.modules.player;

import ru.levin.modules.setting.BooleanSetting;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "NoInteract", desc = "Не даст вам открыть контейнер по нажатию на ПКМ", type = Type.Player)
public class NoInteract extends Function {
    public final BooleanSetting onlyAura = new BooleanSetting("Только с AttackAura",false);

    public NoInteract() {
        addSettings(onlyAura);
    }
    @Override
    public void onEvent(Event event) {

    }
}