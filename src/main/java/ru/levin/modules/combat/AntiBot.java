package ru.levin.modules.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import ru.levin.manager.Manager;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("All")
@FunctionAnnotation(name = "AntiBot", desc = "Убирает бота от античита", type = Type.Combat)
public class AntiBot extends Function {
    private final BooleanSetting removeWorld = new BooleanSetting("Удалить из мира", false);
    private final List<Entity> bots = new ArrayList<>();

    public AntiBot() {
        addSettings(removeWorld);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            detectBots();
        }
    }

    private void detectBots() {
        for (PlayerEntity entity : Manager.SYNC_MANAGER.getPlayers()) {
            if (entity == mc.player) continue;

            if (isBotCandidate(entity)) {
                if (!bots.contains(entity)) {
                    bots.add(entity);
                    if (removeWorld.get()) {
                        mc.world.removeEntity(entity.getId(), Entity.RemovalReason.KILLED);
                    }
                }
            } else {
                bots.remove(entity);
            }
        }
    }

    private boolean isBotCandidate(PlayerEntity entity) {
        ItemStack boots = entity.getInventory().armor.get(0);
        ItemStack leggings = entity.getInventory().armor.get(1);
        ItemStack chestplate = entity.getInventory().armor.get(2);
        ItemStack helmet = entity.getInventory().armor.get(3);

        boolean fullArmor = !boots.isEmpty() && !leggings.isEmpty() && !chestplate.isEmpty() && !helmet.isEmpty();
        boolean enchantable = boots.isEnchantable() && leggings.isEnchantable() && chestplate.isEnchantable() && helmet.isEnchantable();

        boolean validArmorTypes =
                boots.getItem() == Items.LEATHER_BOOTS || leggings.getItem() == Items.LEATHER_LEGGINGS
                        || chestplate.getItem() == Items.LEATHER_CHESTPLATE || helmet.getItem() == Items.LEATHER_HELMET
                        || boots.getItem() == Items.IRON_BOOTS || leggings.getItem() == Items.IRON_LEGGINGS
                        || chestplate.getItem() == Items.IRON_CHESTPLATE || helmet.getItem() == Items.IRON_HELMET;

        boolean offhandEmpty = entity.getOffHandStack().isEmpty();

        boolean mainHandNotEmpty = !entity.getMainHandStack().isEmpty();

        boolean armorNotDamaged = !boots.isDamaged() && !leggings.isDamaged() && !chestplate.isDamaged() && !helmet.isDamaged();

        boolean foodFull = entity.getHungerManager().getFoodLevel() == 20;

        return fullArmor && enchantable && validArmorTypes && offhandEmpty && mainHandNotEmpty && armorNotDamaged && foodFull;
    }

    public boolean check(LivingEntity entity) {
        return entity instanceof PlayerEntity && bots.contains(entity);
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        bots.clear();
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        bots.clear();
    }
}
