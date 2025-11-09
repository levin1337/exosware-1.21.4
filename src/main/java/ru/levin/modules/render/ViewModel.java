package ru.levin.modules.render;


import ru.levin.modules.setting.SliderSetting;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "ViewModel",desc  = "Изменение позиции рук", type = Type.Render)
public class ViewModel extends Function {
    public final SliderSetting right_x = new SliderSetting("RightX", 0.6F, -2f, 2f, 0.1F);
    public final SliderSetting right_y = new SliderSetting("RightY", -0.6F, -2f, 2f, 0.1F);
    public final SliderSetting right_z = new SliderSetting("RightZ", -0.8F, -2f, 2f, 0.1F);
    public final SliderSetting left_x = new SliderSetting("LeftX", 0.0F, -2f, 2f, 0.1F);
    public final SliderSetting left_y = new SliderSetting("LeftY", 0.0F, -2f, 2f, 0.1F);
    public final SliderSetting left_z = new SliderSetting("LeftZ", 0.0F, -2f, 2f, 0.1F);

    public ViewModel() {
        addSettings(right_x,right_y,right_z,left_x,left_y,left_z);
    }

    @Override
    public void onEvent(Event event) {

    }
}