package ru.levin.modules.movement;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.events.impl.EventUpdate;
import ru.levin.mixin.iface.MixinLivingEntityAccessor;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.events.Event;
import ru.levin.util.player.InventoryUtil;

@SuppressWarnings("All")
@FunctionAnnotation(name = "ElytraRecast", desc = "Автоматический ререк кидание элитры", type = Type.Move)
public class ElytraRecast extends Function {
    public BooleanSetting changePitch = new BooleanSetting("ChangePitch", true);
    public SliderSetting pitchValue = new SliderSetting("PitchValue", 55f, -90f, 90f, 1,() -> changePitch.get());
    public BooleanSetting autoJump = new BooleanSetting("AutoJump", true);

    public ElytraRecast() {
        addSettings(changePitch, pitchValue, autoJump);
    }

    @Override
    public void onEvent(Event event) {
        if (mc.player == null || mc.world == null) return;
        if (event instanceof EventMotion em) {
            if (changePitch.get() && !mc.player.isGliding() && checkElytra()) {
                em.setPitch(pitchValue.get().floatValue());
            }

        }
        if (event instanceof EventUpdate) {
            onUpdate();
        }
    }

    @Override
    protected void onDisable() {
        if (!mc.options.forwardKey.isPressed()) {
            mc.options.forwardKey.setPressed(false);
        }
        if (!mc.options.jumpKey.isPressed()) {
            mc.options.jumpKey.setPressed(false);
        }
    }

    private void onUpdate() {
        if (!mc.player.isGliding() && checkElytra()) {
            if (autoJump.get()) {
                if (mc.player.isOnGround()) {
                    mc.player.jump();
                }
            }
        }

        if (!mc.player.isGliding() && mc.player.fallDistance > 0 && checkElytra()) {
            castElytra();
        }

        ((MixinLivingEntityAccessor)mc.player).setLastJumpCooldown(0);
    }

    public boolean castElytra() {
        if (checkElytra() && check()) {
            InventoryUtil.startFly();
            return true;
        }
        return false;
    }

    private boolean checkElytra() {
        ItemStack chestStack = mc.player.getEquippedStack(EquipmentSlot.CHEST);
        return chestStack.getItem() == Items.ELYTRA && isUsable(chestStack) && !mc.player.getAbilities().flying && mc.player.getVehicle() == null && !mc.player.isClimbing();
    }

    public static boolean isUsable(ItemStack stack) {
        if (stack == null) return false;
        if (stack.isEmpty()) return false;
        if (stack.getItem() != Items.ELYTRA) return false;

        int maxDamage = stack.getMaxDamage();
        int damage = stack.getDamage();
        return damage < maxDamage - 1;
    }

    private boolean check() {
        return !mc.player.isCreative() && !mc.player.isSpectator() && !mc.player.hasStatusEffect(StatusEffects.LEVITATION);
    }
}