package ru.levin.modules.player;

import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.player.TimerUtil;

import java.util.List;

@FunctionAnnotation(name = "ChestStealer", desc = "", type = Type.Player)
public class ChestStealer extends Function {

    private final ModeSetting mode = new ModeSetting("Тип", "Обычный", "Обычный", "Умный");
    private final SliderSetting stealDelay = new SliderSetting("Задержка", 120f, 0f, 1000f, 1f);

    private final TimerUtil timer = new TimerUtil();

    private static final List<String> BLOCKED_TITLES = List.of(
            "Аукцион", "Warp", "Варпы", "Меню", "Выбор набора", "Кейсы", "Магазин"
    );

    public ChestStealer() {
        addSettings(mode, stealDelay);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate) || !(mc.currentScreen instanceof GenericContainerScreen container)) return;

        String title = container.getTitle().getString().toLowerCase();
        for (String blocked : BLOCKED_TITLES) {
            if (title.contains(blocked.toLowerCase())) return;
        }

        var handler = container.getScreenHandler();
        int chestSize = handler.getRows() * 9;
        boolean instant = stealDelay.get().floatValue() == 0;

        for (int i = 0; i < chestSize; i++) {
            var stack = handler.getSlot(i).getStack();
            if (stack.isEmpty() || stack.getItem() == Items.AIR) continue;

            if (mode.is("Умный") && !Manager.CHESTSTEALER_MANAGER.isAllowed(stack.getItem())) continue;

            if (instant || timer.hasTimeElapsed(stealDelay.get().longValue())) {
                click(handler.syncId, i);
                if (!instant) timer.reset();
                if (!instant) break;
            }
        }
    }

    private void click(int id, int slot) {
        mc.interactionManager.clickSlot(id, slot, 0, SlotActionType.QUICK_MOVE, mc.player);
    }
}
