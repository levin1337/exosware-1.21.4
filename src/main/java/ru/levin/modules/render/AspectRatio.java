package ru.levin.modules.render;


import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.SliderSetting;

@FunctionAnnotation(name = "AspectRatio" ,desc  = "Позволяет изменять соотношение сторон экрана", type = Type.Render)
public class AspectRatio extends Function {
    public final ModeSetting mods = new ModeSetting("Режим","16:9","4:3","16:9","1:1","16:10","Кастомный");
    public final SliderSetting slider = new SliderSetting("Соотношение", 1.8f, 0.1f, 5.0f,0.1f,() -> mods.is("Кастомный"));

    public AspectRatio() {
        addSettings(mods,slider);
    }

    @Override
    public void onEvent(Event event) {
    }

}
