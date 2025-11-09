package ru.levin.modules.player;

import ru.levin.events.Event;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "NoRayTrace",keywords = {"NoEntityTrace"}, desc = "Убирает хитбокс энтити", type = Type.Player)
public class NoRayTrace extends Function {

    @Override
    public void onEvent(Event event) {
    }
}