package ru.levin.manager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.session.Session;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import ru.levin.ExosWare;
import ru.levin.manager.themeManager.StyleManager;
import ru.levin.mixin.iface.BossBarHudAccessor;
import ru.levin.mixin.iface.MinecraftClientAccessor;
import ru.levin.util.KeyMappings;
import ru.levin.util.color.ColorUtil;
import ru.levin.util.math.MathUtil;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("All")
public class ClientManager implements IMinecraft {
    public static boolean legitMode = false;
    private static float fps = 0;
    public static float TICK_TIMER = 1f;

    public static float getTPS() {
        return Math.round(Manager.SYNC_MANAGER.tps * 10) / 10F;
    }

    public static int getFps() {
        final MinecraftClient client = mc;
        final int currentFps = (client != null) ? client.getCurrentFps() : 0;
        fps = MathUtil.fast(fps, currentFps, 6);
        return Math.round(fps);
    }
    public static String getBps(Entity entity) {
        if (mc == null || mc.player == null) return "0.00";
        double dx = entity.getX() - entity.prevX;
        double dz = entity.getZ() - entity.prevZ;
        return String.format(Locale.ROOT, "%.2f", Math.hypot(dx, dz) * 20.0D);
    }

    public static String getPing() {
        final MinecraftClient client = mc;
        if (client == null || client.player == null || client.getNetworkHandler() == null) {
            return "N/A";
        }
        var entry = client.getNetworkHandler().getPlayerListEntry(client.player.getUuid());
        if (entry == null) {
            return "N/A";
        }
        return Integer.toString(entry.getLatency());
    }

    public static float[] getHealthFromScoreboard(LivingEntity target) {
        float currentHealth = target.getHealth();
        float maxHealth = target.getMaxHealth();

        if (target instanceof PlayerEntity player) {
            Scoreboard scoreboard = player.getScoreboard();
            ScoreboardObjective found = null;

            for (ScoreboardObjective objective : scoreboard.getObjectives()) {
                if (objective.getDisplayName().getString().contains("Здоровья")) {
                    found = objective;
                    break;
                }
            }

            if (found != null) {
                currentHealth = scoreboard.getOrCreateScore(player, found).getScore();
                maxHealth = 20;
            }
        }
        return new float[]{currentHealth, maxHealth};
    }

    public static String getKey(int keyCode) {
        return KeyMappings.keyMappings(keyCode);
    }

    public static boolean playerIsPVP() {
        if (mc == null || mc.inGameHud == null) return false;

        BossBarHud bossOverlayGui = mc.inGameHud.getBossBarHud();
        Map<UUID, ClientBossBar> bossBars = ((BossBarHudAccessor) bossOverlayGui).getBossBars();

        for (ClientBossBar bossInfo : bossBars.values()) {
            String nameStrLower = bossInfo.getName().getString().toLowerCase(Locale.ROOT);
            if (nameStrLower.contains("pvp") || nameStrLower.contains("пвп")) {
                return true;
            }
        }
        return false;
    }

    public static void loginAccount(String name) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        Session session = new Session(name, uuid, "invalid_token", Optional.empty(), Optional.empty(), Session.AccountType.MOJANG);
        MinecraftClient client = MinecraftClient.getInstance();
        ((MinecraftClientAccessor) client).setSession(session);
    }

    public static void message(String string) {
        if (mc == null || mc.player == null || mc.world == null || mc.inGameHud == null) return;

        StyleManager theme = Manager.STYLE_MANAGER;
        int start = theme.getFirstColor();
        int end = theme.getSecondColor();

        mc.inGameHud.getChatHud().addMessage(applyGradient(string, start, end));
    }

    public static String gradient(String message, int first, int end) {
        if (message == null || message.isEmpty()) return "";

        final int length = message.length();
        final StringBuilder result = new StringBuilder(length * 9);

        final float inv = (length <= 1) ? 0f : 1f / (length - 1);
        for (int i = 0; i < length; i++) {
            float progress = (length == 1) ? 0.5f : (i * inv);
            int color = ColorUtil.interpolateColor(first, end, progress) & 0xFFFFFF;

            String hex = Integer.toHexString(color);
            if (hex.length() < 6) {
                hex = "000000".substring(hex.length()) + hex;
            }

            result.append('§').append('#').append(hex).append(message.charAt(i));
        }
        return result.toString();
    }

    private static Text applyGradient(String string, int startColor, int endColor) {
        MutableText component = Text.empty();
        final String name = ExosWare.getInstance().name;
        final int length = name.length();
        final float inv = (length <= 1) ? 0f : 1f / (length - 1);

        for (int i = 0; i < length; i++) {
            int rgb = ColorUtil.blendColors(startColor, endColor, (length == 1) ? 0.5f : (i * inv)) & 0xFFFFFF;
            component.append(Text.literal(String.valueOf(name.charAt(i))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb)).withBold(true)));
        }

        int gray = (java.awt.Color.GRAY.getRGB()) & 0xFFFFFF;
        component.append(Text.literal(" ➭ ").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(gray)).withBold(true)));
        component.append(Text.literal(string).setStyle(Style.EMPTY.withFormatting(Formatting.RESET)));

        return component;
    }
}