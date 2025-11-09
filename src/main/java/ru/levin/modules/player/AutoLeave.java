package ru.levin.modules.player;

import net.minecraft.text.Text;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.modules.setting.TextSetting;

@SuppressWarnings("All")
@FunctionAnnotation(name = "AutoLeave", desc = "Автоматически ливает с сервера", type = Type.Player)
public class AutoLeave extends Function {

    private final ModeSetting mode = new ModeSetting("Режим",
            "Рядом игрок", "Рядом игрок", "Мало Хп");

    private final SliderSetting heal = new SliderSetting("Здоровье",
            3, 1, 20, 1, () -> mode.is("Мало Хп"));

    private final SliderSetting radius = new SliderSetting("Радиус до игрока",
            60, 20, 150, 1, () -> mode.is("Рядом игрок"));

    private final ModeSetting run = new ModeSetting("Что делать",
            "Выходить с сервера", "Выходить с сервера", "/hub", "телепортация домой");

    private final TextSetting homeName = new TextSetting("Название точки дома",
            "home", () -> run.is("телепортация домой"));

    private final BooleanSetting pvpNoLeave =
            new BooleanSetting("Не выходить если режим PVP", true,
                    () -> run.is("Выходить с сервера") || run.is("/hub"));

    private boolean triggered = false;

    public AutoLeave() {
        addSettings(mode, heal, radius, run, homeName, pvpNoLeave);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate)) return;
        if (mc.player == null || mc.world == null) return;

        if (pvpNoLeave.get() && ClientManager.playerIsPVP()) return;

        boolean shouldTrigger = false;

        if (mode.is("Мало Хп") && mc.player.getHealth() <= heal.get().floatValue()) {
            shouldTrigger = true;
        } else if (mode.is("Рядом игрок")) {
            shouldTrigger = Manager.SYNC_MANAGER.getPlayers().stream().anyMatch(other -> other != mc.player && mc.player.distanceTo(other) <= radius.get().floatValue());
        }

        if (shouldTrigger && !triggered) {
            executeAction();
            triggered = true;
        } else if (!shouldTrigger) {
            triggered = false;
        }
    }

    private void executeAction() {
        if (mc.player == null) return;

        if (run.is("Выходить с сервера")) {
            mc.player.networkHandler.getConnection().disconnect(Text.literal("AutoLeave"));
        } else if (run.is("/hub")) {
            mc.player.networkHandler.sendChatCommand("hub");
        } else if (run.is("телепортация домой")) {
            mc.player.networkHandler.sendChatCommand("home " + homeName.getValue());
        }
        toggle();
    }
}
