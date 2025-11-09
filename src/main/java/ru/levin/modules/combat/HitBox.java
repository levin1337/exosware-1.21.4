package ru.levin.modules.combat;

import ru.levin.modules.setting.SliderSetting;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "HitBox", type = Type.Combat, desc = "Позволяет увеличивать хит-бокс игроков")
public class HitBox extends Function {

    public SliderSetting size = new SliderSetting("Размер", 0.4f, 0.1f, 5.5f, 0.1f);

    public HitBox() {
        addSettings(size);
    }
    @Override
    public void onEvent(Event event) {
    }

}