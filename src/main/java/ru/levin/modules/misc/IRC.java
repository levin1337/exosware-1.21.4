package ru.levin.modules.misc;

import ru.levin.events.Event;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "IRC", desc = "Чат между юзерами других клиентов", type = Type.Misc)
public class IRC extends Function {

    @Override
    public void onEvent(Event event) {
    }


    @Override
    protected void onDisable() {
        Manager.IRC_MANAGER.shutdown();
        super.onDisable();
    }
}