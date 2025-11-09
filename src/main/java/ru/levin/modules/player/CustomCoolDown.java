package ru.levin.modules.player;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.levin.manager.ClientManager;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("All")
@FunctionAnnotation(name = "CustomCoolDown", type = Type.Player, desc = "Автоматически выставляет кулдавн предметам")
public class CustomCoolDown extends Function {

    private final MultiSetting items = new MultiSetting(
            "Задержка",
            Arrays.asList("Золотое яблоко", "Плод хоруса", "Эндер-жемчюг"),
            new String[]{"Золотое яблоко", "Плод хоруса", "Эндер-жемчюг"}
    );

    private final SliderSetting appleTime = new SliderSetting("Кд золотого яблока", 4.6F, 1.0F, 16.0F, 0.1F, () -> items.get("Золотое яблоко"));
    private final SliderSetting pearlTime = new SliderSetting("Кд эндер-жемчюгов", 13.5F, 1.0F, 16.0F, 0.1F, () -> items.get("Эндер-жемчюг"));
    private final SliderSetting horusTime = new SliderSetting("Кд хоруса", 3.5F, 1.0F, 16.0F, 0.1F, () -> items.get("Плод хоруса"));

    public BooleanSetting PVPonly = new BooleanSetting("Только в PVP режиме", false);

    public final Map<Item, Long> lastUseMap = new HashMap<>();

    public CustomCoolDown() {
        addSettings(items, appleTime, pearlTime, horusTime, PVPonly);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventUpdate) || mc.player == null) return;
        if (PVPonly.get()) {
            if (!ClientManager.playerIsPVP()) {
                return;
            }
        }

        Iterator<Map.Entry<Item, Long>> iterator = lastUseMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Item, Long> entry = iterator.next();
            float delay = getCooldownForItem(entry.getKey()) * 1000F;
            if (System.currentTimeMillis() - entry.getValue() >= delay) {
                iterator.remove();
            }
        }

        if (mc.player.isUsingItem()) {
            ItemStack activeStack = mc.player.getActiveItem();
            Item item = activeStack.getItem();

            if (isItemEnabled(item) && lastUseMap.containsKey(item)) {
                mc.player.clearActiveItem();
            }
        }
    }


    public float getCooldownForItem(Item item) {
        if (item == Items.GOLDEN_APPLE) return ((Number) appleTime.get()).floatValue();
        if (item == Items.ENDER_PEARL) return ((Number) pearlTime.get()).floatValue();
        if (item == Items.CHORUS_FRUIT) return ((Number) horusTime.get()).floatValue();
        return 0;
    }

    public boolean isItemEnabled(Item item) {
        if (item == Items.GOLDEN_APPLE) return items.get("Золотое яблоко");
        if (item == Items.ENDER_PEARL) return items.get("Эндер-жемчюг");
        if (item == Items.CHORUS_FRUIT) return items.get("Плод хоруса");
        return false;
    }

    public void setCooldown(Item item) {
        lastUseMap.put(item, System.currentTimeMillis());
    }

}