package ru.levin.modules.misc;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.setting.BindSetting;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.input.EventKey;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.player.InventoryUtil;
import ru.levin.util.player.TimerUtil;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("All")
@FunctionAnnotation(name = "HWHelper", desc = "Быстрое взаимодействие с предметами на HollyWorld", type = Type.Misc)
public class HWHelper extends Function {

    private final BindSetting trapka = new BindSetting("Кнопка трапки", 0);
    private final BindSetting trapkaBax = new BindSetting("Кнопка взрывной трапки", 0);
    private final BindSetting stan = new BindSetting("Кнопка стана", 0);
    private final BindSetting snow = new BindSetting("Кнопка кома снега", 0);
    private final BindSetting babax = new BindSetting("Кнопка взрывной штучки", 0);

    private final BooleanSetting bypass = new BooleanSetting("Обход", true, "Замедляет вас при свапе");
    private final BooleanSetting inventoryUse = new BooleanSetting("Использовать из инвентаря", true);

    private final TimerUtil timer = new TimerUtil();
    private boolean bypassActive = false;
    private boolean awaitingSwap = false;

    private int hotbarSlot = -1;
    private int invSlot = -1;

    private final Map<BindSetting, Item> binds = new LinkedHashMap<>();

    public HWHelper() {
        addSettings(trapka, trapkaBax, stan, snow, babax, bypass, inventoryUse);
        binds.put(trapka, Items.POPPED_CHORUS_FRUIT);
        binds.put(trapkaBax, Items.PRISMARINE_SHARD);
        binds.put(stan, Items.NETHER_STAR);
        binds.put(snow, Items.SNOWBALL);
        binds.put(babax, Items.FIRE_CHARGE);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventKey eventKey) {
            handleKey(eventKey.key);
        }

        if (event instanceof EventUpdate) {
            handleBypass();
        }
    }

    private void handleKey(int pressedKey) {
        for (Map.Entry<BindSetting, Item> entry : binds.entrySet()) {
            if (pressedKey == entry.getKey().getKey()) {
                int[] slots = findSlots(entry.getValue());

                if (bypass.get()) {
                    timer.reset();
                    bypassActive = true;
                    awaitingSwap = true;
                    hotbarSlot = slots[0];
                    invSlot = slots[1];
                } else {
                    InventoryUtil.use(slots[0], slots[1], inventoryUse.get());
                }
                return;
            }
        }
    }

    private void handleBypass() {
        if (!bypassActive) return;

        setMovementKeys(false);

        if (awaitingSwap && timer.hasTimeElapsed(90)) {
            awaitingSwap = false;
            if (hotbarSlot != -1 || invSlot != -1) {
                InventoryUtil.use(hotbarSlot, invSlot, inventoryUse.get());
            }
        }

        if (timer.hasTimeElapsed(150)) {
            bypassActive = false;
            awaitingSwap = false;
            setMovementKeys(true);
        }
    }

    private int[] findSlots(Item item) {
        if (mc.player == null) return new int[]{-1, -1};

        int hotbarSlot = -1;
        int inventorySlot = -1;

        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty() || stack.getItem() != item) continue;

            if (i < 9) hotbarSlot = i;
            else inventorySlot = i;

            if (hotbarSlot != -1 && inventorySlot != -1) break;
        }
        return new int[]{hotbarSlot, inventorySlot};
    }

    private void setMovementKeys(boolean restore) {
        KeyBinding[] keys = {
                mc.options.forwardKey,
                mc.options.backKey,
                mc.options.leftKey,
                mc.options.rightKey,
                mc.options.sprintKey
        };

        for (KeyBinding key : keys) {
            if (restore) {
                updateKeyBinding(key);
            } else {
                key.setPressed(false);
            }
        }
    }

    private void updateKeyBinding(KeyBinding keyMapping) {
        keyMapping.setPressed(InputUtil.isKeyPressed(mc.getWindow().getHandle(), keyMapping.getDefaultKey().getCode()));
    }
}
