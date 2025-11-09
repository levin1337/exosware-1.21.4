package ru.levin.events;

import ru.levin.manager.ClientManager;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;

public class Event implements IMinecraft {

    public boolean isCancel;

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        this.isCancel = cancel;
    }

    public static void call(final Event event) {
        if (mc.player == null || mc.world == null || event.isCancel()) {
            return;
        }
        if (!ClientManager.legitMode) {
            for (final Function module : Manager.FUNCTION_MANAGER.getFunctions()) {
                if (module.isState()) {
                    module.onEvent(event);
                }
            }
            Manager.SYNC_MANAGER.onEvent(event);
        }
    }
}