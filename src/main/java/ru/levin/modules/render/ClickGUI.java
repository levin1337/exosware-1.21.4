package ru.levin.modules.render;

import org.lwjgl.glfw.GLFW;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.modules.setting.SliderSetting;

import java.awt.*;
import java.util.Arrays;

@FunctionAnnotation(name = "ClickGUI" ,desc  = "Управление/Кастомизация GUI", type = Type.Render, key = GLFW.GLFW_KEY_RIGHT_SHIFT)
public class ClickGUI extends Function {
    public final ModeSetting colorGUI = new ModeSetting("Тема","Светло-чёрная","Светло-чёрная","Тёмная");

    public final SliderSetting alpha = new SliderSetting("Прозрачность",200f,120f,255f,1f);
    public final BooleanSetting blur = new BooleanSetting("Размытие",true);
    public final MultiSetting blurSetting = new MultiSetting(
            () -> blur.get(),
            "Элементы",
            Arrays.asList("Поиск","Панели"),
            new String[]{"Поиск","Темы", "Панели", "Описание","Создание темы"}
    );
    public final BooleanSetting strike = new BooleanSetting("Обводка для модулей",true);
    public final BooleanSetting filling = new BooleanSetting("Заливка для модулей",true);
    public final SliderSetting rounding = new SliderSetting("Закругление",4,0,6,1,() -> strike.get() || filling.get());
    public final SliderSetting alphaModules = new SliderSetting("Прозрачность модулей",20f,10f,40f,1f,() -> strike.get() || filling.get());

    public ClickGUI() {
        addSettings(colorGUI,alpha,blur,blurSetting,strike,filling,rounding,alphaModules);
    }

    public Color getGuiColor() {
        switch (colorGUI.get()) {
            case "Тёмная":
                return new Color(0, 0, 0, alpha.get().intValue());
            case "Светло-чёрная":
            default:
                return new Color(17, 15, 28, alpha.get().intValue());
        }
    }

    @Override
    public void onEvent(Event event) {}

    @Override
    public void onEnable() {
        setState(false);
        super.onEnable();
    }
}