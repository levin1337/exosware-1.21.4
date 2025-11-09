package ru.levin.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PlayerHeadItem;
import ru.levin.events.impl.move.EventEntitySpawn;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.mixin.player.MixinEntity;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.player.InventoryUtil;

import java.util.Arrays;

@SuppressWarnings("all")
@FunctionAnnotation(name = "AutoTotem", desc = "Берёт в руки тотем при определённом здоровье", type = Type.Combat)
public class AutoTotem extends Function {

    private final MultiSetting mode = new MultiSetting(
            "Брать если",
            Arrays.asList("Кристалл", "Игрок с булавой"),
            new String[]{"Кристалл", "Игрок с булавой", "Рядом крипер", "Обсидиан", "Якорь", "Падение", "Вагонетка"}
    );

    private final SliderSetting HPElytra = new SliderSetting("Брать раньше на элитрах", 5, 2, 6, 1);
    private final BooleanSetting back = new BooleanSetting("Возвращать предмет", true);
    private final BooleanSetting noBallSwitch = new BooleanSetting("Не брать если шар", false);
    private final BooleanSetting saveEnchantedtotem = new BooleanSetting("Сохранять чаренные тотемы", true);
    private final BooleanSetting absorptionCheck = new BooleanSetting("+ Золотые сердца", false);
    public final SliderSetting hp = new SliderSetting("Здоровье", 4.5f, 2.0f, 20.0f, 0.1f);

    private final SliderSetting crystalDistance = new SliderSetting("До кристалла", 4, 2, 6, 1, () -> mode.get("Кристалл"));
    private final SliderSetting anchorDistance = new SliderSetting("До якоря", 4, 2, 6, 1, () -> mode.get("Якорь"));
    private final SliderSetting minecartDistance = new SliderSetting("До Вагонетки", 4, 2, 8, 1, () -> mode.get("Вагонетка"));
    private final SliderSetting obsidianDistance = new SliderSetting("До Обсидиана", 4, 2, 8, 1, () -> mode.get("Обсидиан"));

    private int item = -1;

    public AutoTotem() {
        addSettings(mode, hp, HPElytra, back, noBallSwitch, saveEnchantedtotem, absorptionCheck,
                crystalDistance, anchorDistance, minecartDistance, obsidianDistance);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventEntitySpawn spawnEvent) {
            Entity e = spawnEvent.getEntity();
            if (mode.get("Кристалл") && e instanceof EndCrystalEntity) {
                if (mc.player != null && e.distanceTo(mc.player) <= crystalDistance.get().floatValue()) {
                    forceTotem();
                }
            }

            if (mode.get("Вагонетка") && e instanceof TntMinecartEntity) {
                if (mc.player != null && e.distanceTo(mc.player) <= minecartDistance.get().floatValue()) {
                    forceTotem();
                }
            }
        }
        if (event instanceof EventUpdate) {
            int slot = getTotemSlot();
            ItemStack offhand = mc.player.getOffHandStack();
            boolean hasTotemInHand = offhand.getItem() == Items.TOTEM_OF_UNDYING;

            if (condition()) {
                if (slot == -1) return;
                if (saveEnchantedtotem.get() && offhand.getItem() == Items.TOTEM_OF_UNDYING && offhand.hasEnchantments()) {
                    ItemStack candidate = mc.player.getInventory().getStack(slot);
                    if (candidate.getItem() == Items.TOTEM_OF_UNDYING && !candidate.hasEnchantments()) {
                        InventoryUtil.swapSlotsUniversal(slot, 40, false, true);
                        item = slot;
                        return;
                    }
                }

                if (!hasTotemInHand) {
                    InventoryUtil.swapSlotsUniversal(slot, 40, false, true);
                    if (item == -1) {
                        item = slot;
                    }
                }
            } else {
                if (item != -1 && back.get()) {
                    InventoryUtil.swapSlotsUniversal(item, 40, false, true);
                    item = -1;
                }
            }
        }
    }
    private void forceTotem() {
        int slot = getTotemSlot();
        if (slot == -1) return;

        ItemStack offhand = mc.player.getOffHandStack();
        if (offhand.getItem() != Items.TOTEM_OF_UNDYING) {
            InventoryUtil.swapSlotsUniversal(slot, 40, false, true);
            item = slot;
        }
    }


    private int getTotemSlot() {
        ItemStack offhand = mc.player.getOffHandStack();

        if (saveEnchantedtotem.get()) {
            if (offhand.getItem() == Items.TOTEM_OF_UNDYING && offhand.hasEnchantments()) {
                int normalTotem = findTotem(false);
                if (normalTotem != -1) return normalTotem;
                return -1;
            }

            int normalTotem = findTotem(false);
            if (normalTotem != -1) return normalTotem;

            int enchantedTotem = findTotem(true);
            if (enchantedTotem != -1) return enchantedTotem;
            return -1;
        }

        return InventoryUtil.getItemSlot(Items.TOTEM_OF_UNDYING);
    }

    private int findTotem(boolean enchanted) {
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
                boolean hasEnchant = stack.hasEnchantments();
                if (enchanted == hasEnchant) return i;
            }
        }
        return -1;
    }

    private boolean condition() {
        final float absorption = absorptionCheck.get() && mc.player.hasStatusEffect(StatusEffects.ABSORPTION)
                ? mc.player.getAbsorptionAmount() : 0.0f;

        if (mc.player.getHealth() + absorption <= hp.get().floatValue()) return true;
        if (!isBall()) {
            if (crystal()) return true;
            if (anchor()) return true;
            if (macePlayer()) return true;
            if (creeper()) return true;
            if (obsidian()) return true;
        }

        return checkHPElytra() || checkFall();
    }

    private boolean checkFall() {
        if (!mode.get("Падение")) return false;
        if (mc.player.isGliding()) return false;
        return mc.player.fallDistance > 10.0f;
    }

    private boolean checkHPElytra() {
        return ((ItemStack) mc.player.getInventory().armor.get(2)).getItem() == Items.ELYTRA &&
                mc.player.getHealth() <= hp.get().floatValue() + HPElytra.get().floatValue();
    }

    private boolean isBall() {
        if (mode.get("Якорь") && mc.player.fallDistance > 5.0f) return false;
        return noBallSwitch.get() && mc.player.getOffHandStack().getItem() instanceof PlayerHeadItem;
    }

    private boolean anchor() {
        if (!mode.get("Якорь")) return false;
        return InventoryUtil.TotemUtil.getBlock((float) anchorDistance.get().floatValue(), Blocks.RESPAWN_ANCHOR) != null;
    }

    private boolean obsidian() {
        if (!mode.get("Обсидиан")) return false;
        return InventoryUtil.TotemUtil.getBlock((float) obsidianDistance.get().floatValue(), Blocks.OBSIDIAN) != null;
    }

    private boolean creeper() {
        if (!mode.get("Рядом крипер")) return false;

        for (Entity entity : Manager.SYNC_MANAGER.getEntities()) {
            if (entity instanceof CreeperEntity creeper && mc.player.distanceTo(creeper) < 5.0f) {
                if (creeper.getClientFuseTime(0f) > 0f) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean macePlayer() {
        if (!mode.get("Игрок с булавой")) return false;

        for (PlayerEntity player : Manager.SYNC_MANAGER.getPlayers()) {
            if (player == mc.player) continue;

            boolean hasMace = player.getMainHandStack().getItem() == Items.MACE;
            double dy = player.getY() - mc.player.getY();
            double yVel = player.getVelocity().y;
            double distance = player.distanceTo(mc.player);
            boolean isAbove = dy > 1.5;
            boolean isInAir = !player.isOnGround() && !player.isTouchingWater() && !player.isClimbing();
            boolean fallingOrInAir = (yVel < -0.1 || yVel > 0.1) && isInAir;

            if (hasMace && isAbove && fallingOrInAir && distance < 24) {
                return true;
            }
        }
        return false;
    }

    private boolean crystal() {
        boolean checkCrystal = mode.get("Кристалл");
        boolean checkMinecart = mode.get("Вагонетка");

        if (!checkCrystal && !checkMinecart) return false;
        for (Entity entity : Manager.SYNC_MANAGER.getEntities()) {
            if (checkCrystal && entity instanceof EndCrystalEntity && mc.player.distanceTo(entity) < crystalDistance.get().floatValue()) {
                return true;
            }
            if (checkMinecart && entity instanceof TntMinecartEntity && mc.player.distanceTo(entity) < minecartDistance.get().floatValue()) {
                return true;
            }
        }
        return false;
    }

    private void reload() {
        item = -1;
    }

    @Override
    protected void onEnable() {
        reload();
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        reload();
        super.onDisable();
    }
}
