package ru.levin.modules.player;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.mixin.iface.MinecraftClientAccessor;
import ru.levin.mixin.iface.MixinLivingEntityAccessor;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;

@FunctionAnnotation(name = "NoDelay" ,desc  = "Убирает задержку предметам", type = Type.Player)
public class NoDelay extends Function {

    private final BooleanSetting jump = new BooleanSetting("Прыжок",true);
    private final BooleanSetting xp = new BooleanSetting("Пузырёк опыта",true);
    private final BooleanSetting crystal = new BooleanSetting("Кристаллы",true);
    private final BooleanSetting place = new BooleanSetting("ПКМ",false);

    public NoDelay() {
        addSettings(jump,xp,crystal,place);
    }


    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (jump.get()) {
                ((MixinLivingEntityAccessor) mc.player).setLastJumpCooldown(0);
            }
            if (check(mc.player.getMainHandStack().getItem()))
                ((MinecraftClientAccessor) mc).setUseCooldown(0);
        }
    }
    private boolean check(Item item) {
        return (item instanceof BlockItem && place.get()) || (item == Items.END_CRYSTAL && crystal.get()) || (item == Items.EXPERIENCE_BOTTLE && xp.get());
    }
}
