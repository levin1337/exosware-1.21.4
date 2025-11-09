package ru.levin.modules.misc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.entity.player.PlayerInventory;
import ru.levin.modules.setting.BindSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.input.EventKey;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.util.player.InventoryUtil;

import java.util.LinkedHashMap;
import java.util.Map;

@FunctionAnnotation(name = "FTHelper", desc = "Быстрое взаимодействие с предметами на FunTime", type = Type.Misc)
public class FTHelper extends Function {
    private final BindSetting trapka = new BindSetting("Кнопка трапки", 0);
    private final BindSetting disorientation = new BindSetting("Кнопка дезориентации", 0);
    private final BindSetting plast = new BindSetting("Кнопка пласта", 0);
    private final BindSetting godaura = new BindSetting("Кнопка божьей ауры", 0);
    private final BooleanSetting inventoryUse = new BooleanSetting("Использовать из инвентаря",true);

    private final Map<BindSetting, Item> binds = new LinkedHashMap<>();

    public FTHelper() {
        addSettings(trapka, disorientation, plast, godaura , inventoryUse);
        binds.put(trapka, Items.NETHERITE_SCRAP);
        binds.put(disorientation, Items.ENDER_EYE);
        binds.put(plast, Items.DRIED_KELP);
        binds.put(godaura, Items.PHANTOM_MEMBRANE);
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventKey eventKey) || mc.player == null) return;

        int pressedKey = eventKey.key;
        for (Map.Entry<BindSetting, Item> entry : binds.entrySet()) {
            if (pressedKey == entry.getKey().getKey()) {
                useItem(entry.getValue());
                return;
            }
        }
    }

    private void useItem(Item item) {
        int[] slots = findSlots(item);
        InventoryUtil.use(slots[0], slots[1],inventoryUse.get());
    }

    private int[] findSlots(Item item) {
        if (mc.player == null) return new int[]{-1, -1};
        PlayerInventory inv = mc.player.getInventory();
        int size = inv.size();
        int hotbarSlot = -1;
        int inventorySlot = -1;

        for (int i = 0; i < size; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            if (stack.getItem() == item) {
                if (i < 9) {
                    if (hotbarSlot == -1) hotbarSlot = i;
                    if (inventorySlot == -1) inventorySlot = i + 36;
                } else {
                    if (inventorySlot == -1) inventorySlot = i;
                }
                if (hotbarSlot != -1 && inventorySlot != -1) break;
            }
        }
        return new int[]{hotbarSlot, inventorySlot};
    }
}