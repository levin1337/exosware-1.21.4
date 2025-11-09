package ru.levin.modules.combat;

import net.minecraft.block.Blocks;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Hand;
import ru.levin.mixin.iface.ClientWorldAccessor;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.util.player.TimerUtil;

import java.util.Arrays;
import java.util.Optional;

@SuppressWarnings("All")
@FunctionAnnotation(name = "AutoPotion",keywords = "AutoBuff", type = Type.Combat,desc = "Автоматически кидает бафы под себя")
public class AutoPotion extends Function {
    private final BooleanSetting autoOff = new BooleanSetting("Авто отключение", false);

    public MultiSetting potions = new MultiSetting(
            "Бросать",
            Arrays.asList("Силу", "Скорость", "Огнестойкость"),
            new String[]{"Силу", "Скорость", "Огнестойкость"}
    );
    public final TimerUtil timer = new TimerUtil();
    private boolean spoofed = false;
    public boolean isActivePotion;
    private float rotprev;
    private int selectedSlot = -1;
    private final float pose = 90;

    public AutoPotion() {
        addSettings(potions,autoOff);
    }
    private boolean isEatingFood() {
        return mc.player.isUsingItem() && !mc.player.getActiveItem().isOf(Items.SHIELD)
                && !mc.player.getActiveItem().isOf(Items.BOW)
                && !mc.player.getActiveItem().isOf(Items.TRIDENT);
    }

    private enum PotionType {
        STRENGTH(5, StatusEffects.STRENGTH, "Силу"),
        SPEED(1, StatusEffects.SPEED, "Скорость"),
        FIRE_RESISTANCE(12, StatusEffects.FIRE_RESISTANCE, "Огнестойкость");

        final int id;
        final RegistryEntry<StatusEffect> effect;
        final String settingName;

        PotionType(int id, RegistryEntry<StatusEffect> effect, String settingName) {
            this.id = id;
            this.effect = effect;
            this.settingName = settingName;
        }

        public boolean isEnabled(AutoPotion module) {
            return module.potions.get(this.settingName);
        }
    }

    private int findPotionSlot(PotionType type) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);

            if (stack.getItem() == Items.SPLASH_POTION) {
                Optional<PotionContentsComponent> potionComponent = Optional.ofNullable(stack.getComponents().get(DataComponentTypes.POTION_CONTENTS));
                if (potionComponent.isPresent()) {
                    Iterable<StatusEffectInstance> effects = potionComponent.get().getEffects();

                    for (StatusEffectInstance effect : effects) {
                        if (effect.getEffectType() == type.effect) {
                            return i;
                        }
                    }
                }
            }
        }
        return -1;
    }

    private boolean hasEffect(RegistryEntry<StatusEffect> effect) {
        return mc.player.hasStatusEffect(effect);
    }

    private boolean canBuff(PotionType type) {
        if (hasEffect(type.effect)) return false;
        return type.isEnabled(this) && findPotionSlot(type) != -1;
    }

    private boolean canBuff() {
        return !isEatingFood() && (canBuff(PotionType.STRENGTH) ||
                canBuff(PotionType.SPEED) ||
                canBuff(PotionType.FIRE_RESISTANCE)) &&
                mc.player.isOnGround() &&
                timer.hasTimeElapsed(500);
    }

    private boolean shouldUsePotion() {
        return true;
    }

    private boolean isActive() {
        return isActivePotion || (canBuff(PotionType.STRENGTH) || (canBuff(PotionType.SPEED) || (canBuff(PotionType.FIRE_RESISTANCE))));
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventMotion eventAfterRotate) {
            if (shouldThrow()) {
                rotprev = mc.player.getPitch();
                eventAfterRotate.setPitch(pose);
                spoofed = true;
                isActivePotion = true;
            }
        }

        if (event instanceof EventUpdate) {
            if (isActivePotion && !shouldThrow()) {
                isActivePotion = false;
                if (autoOff.get()) this.toggle();
            }

            if (shouldThrow() && spoofed) {
                throwPotion(PotionType.STRENGTH);
                throwPotion(PotionType.SPEED);
                throwPotion(PotionType.FIRE_RESISTANCE);

                mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(mc.player.getInventory().selectedSlot));

                mc.player.setPitch(rotprev);
                timer.reset();
                spoofed = false;
                isActivePotion = false;

                if (autoOff.get()) this.toggle();
            }
        }
    }

    private boolean shouldThrow() {
        return isActive() &&
                canBuff() &&
                mc.world.getBlockState(mc.player.getBlockPos().down()).getBlock() != Blocks.AIR;
    }

    private void throwPotion(PotionType type) {
        if (!type.isEnabled(this) || hasEffect(type.effect)) return;

        int slot = findPotionSlot(type);
        if (slot == -1) return;

        selectedSlot = mc.player.getInventory().selectedSlot;
        mc.player.networkHandler.sendPacket(new UpdateSelectedSlotC2SPacket(slot));

        float yaw = mc.player.getYaw();
        float pitch = mc.player.getPitch();

        sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(Hand.MAIN_HAND, id, yaw, pitch));
    }

    private void sendSequencedPacket(SequencedPacketCreator packetCreator) {
        if (mc.player.networkHandler == null || mc.world == null) return;
        try (PendingUpdateManager pendingUpdateManager = ((ClientWorldAccessor) mc.world).getPendingUpdateManager().incrementSequence()) {
            int sequence = pendingUpdateManager.getSequence();
            mc.player.networkHandler.sendPacket(packetCreator.predict(sequence));
        }
    }
}