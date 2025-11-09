package ru.levin.manager;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.MathHelper;
import ru.levin.events.Event;
import ru.levin.events.impl.move.EventMotion;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.render.EventPlayerRender;
import ru.levin.mixin.iface.ClientPlayerEntityAccessor;
import ru.levin.modules.combat.rotation.RotationController;

import java.util.Collections;
import java.util.List;

public class SyncManager implements IMinecraft {
    private volatile List<Entity> cachedEntities = Collections.emptyList();
    private volatile List<AbstractClientPlayerEntity> cachedPlayers = Collections.emptyList();
    private volatile List<ItemStack> cachedItems = Collections.emptyList();

    private float visualBodyYaw, visualHeadYaw, visualHeadPitch;
    private float visualPrevBodyYaw, visualPrevHeadYaw, visualPrevHeadPitch;

    public float tps = 20;
    private long lastTime = System.nanoTime();
    private final float NANOS_IN_SECOND = 1_000_000_000F;

    public List<Entity> getEntities() {
        return cachedEntities;
    }

    public List<ItemStack> getItems() {
        return cachedItems;
    }

    public List<AbstractClientPlayerEntity> getPlayers() {
        return cachedPlayers;
    }

    public float getBodyYaw() {
        var player = mc.player;
        if (player == null) return visualBodyYaw;

        double dx = player.getX() - player.prevX;
        double dz = player.getZ() - player.prevZ;

        float offset = visualBodyYaw;
        if ((dx * dx + dz * dz) > 0.0025f) {
            offset = (float) (MathHelper.atan2(dz, dx) * (180f / Math.PI) - 90.0f);
        }

        var accessor = (ClientPlayerEntityAccessor) player;
        float lastYaw = accessor.getLastYaw();

        if (player.handSwingProgress > 0.0f) {
            offset = lastYaw;
        }

        float deltaBodyYaw = MathHelper.clamp(MathHelper.wrapDegrees(lastYaw - (visualBodyYaw + MathHelper.wrapDegrees(offset - visualBodyYaw) * 0.3f)), -45.0f, 75.0f);

        return (deltaBodyYaw > 50f ? deltaBodyYaw * 0.2f : 0) + lastYaw - deltaBodyYaw;
    }

    public void onEvent(Event event) {
        if (mc.world == null || mc.player == null) return;
        switch (event) {
            case EventUpdate ignored -> {
                Manager.ROTATION.onUpdate();
                cachedEntities = ImmutableList.copyOf(mc.world.getEntities());
                cachedPlayers = ImmutableList.copyOf(mc.world.getPlayers());
                cachedItems = ImmutableList.copyOf(mc.player.getAllArmorItems());
            }

            case EventMotion em -> {
                Manager.ROTATION.onUpdate();
                RotationController rc = Manager.ROTATION;
                rc.updateIfFree(em.getYaw(), em.getPitch());
                if (rc.isControlling()) {
                    em.setYaw(rc.getYaw());
                    em.setPitch(rc.getPitch());
                }
                visualPrevHeadYaw = visualHeadYaw;
                visualPrevHeadPitch = visualHeadPitch;
                visualHeadYaw = em.getYaw();
                visualHeadPitch = em.getPitch();

                visualPrevBodyYaw = visualBodyYaw;
                visualBodyYaw = getBodyYaw();
            }

            case EventPlayerRender epr -> {
                epr.setPrevYaw(visualPrevHeadYaw);
                epr.setPrevPitch(visualPrevHeadPitch);
                epr.setPrevBodyYaw(visualPrevBodyYaw);

                epr.setYaw(visualHeadYaw);
                epr.setPitch(visualHeadPitch);
                epr.setBodyYaw(visualBodyYaw);
            }
            case EventPacket ep -> {
                if (ep.getPacket() instanceof WorldTimeUpdateS2CPacket) {
                    long now = System.nanoTime();
                    long delay = now - lastTime;
                    lastTime = now;
                    if (delay <= 0L) return;

                    float currentTPS = 20F * (NANOS_IN_SECOND / delay);
                    currentTPS = MathHelper.clamp(currentTPS, 0F, 20F);

                    tps += (currentTPS - tps) * 0.05F;
                }
            }
            default -> {}
        }
    }
}
