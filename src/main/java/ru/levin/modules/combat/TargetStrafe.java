package ru.levin.modules.combat;

import ru.levin.events.Event;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.movement.Speed;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.SliderSetting;

@FunctionAnnotation(name = "TargetStrafe", type = Type.Combat,desc = "Как в нурике стрейфыы ыыы")
public class TargetStrafe extends Function {
    public final SliderSetting speedSlider = new SliderSetting("Скорость",0.095f,0.01f,1.2f,0.01f);

    public final ModeSetting ptytag = new ModeSetting("Метод притяга","Vector","Vector","Motion / Velocity");
    public final SliderSetting blocks = new SliderSetting("Дистанция притяга",7f,0.01f,12f,0.01f);
    public final SliderSetting hitbox = new SliderSetting("Хитбокс для буста",0.095f,0.01f,50.0f,0.01f);
    public final BooleanSetting predictCheck = new BooleanSetting("Предикт",true);
    public final SliderSetting predict = new SliderSetting("Предикт значение",2.5f,0.1f,4.0f,0.1f,() -> predictCheck.get());

    public final BooleanSetting predictView = new BooleanSetting("Видеть предикт",false,"Для вашего экрана вы будите прям обгонять противника");


    public TargetStrafe() {
        addSettings(speedSlider,ptytag,blocks,hitbox,predictCheck,predict,predictView);
    }

    @Override
    public void onEvent(Event event) {
    }
    @Override
    protected void onDisable() {
        if (mc.options.forwardKey.isPressed()) {
            mc.options.forwardKey.setPressed(false);
        }
        super.onDisable();
    }
    @Override
    public void onEnable() {
        Speed speed = Manager.FUNCTION_MANAGER.speed;
        if (speed.state) {
            speed.setState(false);
        }
        super.onEnable();
    }
}