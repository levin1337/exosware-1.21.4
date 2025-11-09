package ru.levin.modules.misc;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import ru.levin.modules.setting.*;
import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.misc.autoduel.Counter;
import ru.levin.manager.ClientManager;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@SuppressWarnings("All")
@FunctionAnnotation(name = "AutoDuel", type = Type.Misc,desc = "Автоматически кидает запрос на дуель")
public class AutoDuel extends Function {

    private final Pattern pattern = Pattern.compile("^\\w{3,16}$");

    private final ModeSetting mode = new ModeSetting(
            "Режим",
            "Шары", "Шары","Щит", "Шипы 3", "Незеритка", "Читерский рай", "Лук","Классик","Тотемы","Нодебафф"
    );
    private SliderSetting slowTime = new SliderSetting("Скорость отправки",500f,300f,1000f,100f);
    private final BooleanSetting babki = new BooleanSetting("Играть на деньги", false);
    private TextSetting money = new TextSetting("Монет", "10000", () -> babki.get());



    private double lastPosX, lastPosY, lastPosZ;

    private final List<String> sent = Lists.newArrayList();
    private final Counter counter = Counter.create();
    private final Counter counter2 = Counter.create();
    private final Counter counterChoice = Counter.create();
    private final Counter counterTo = Counter.create();

    public AutoDuel() {
        addSettings(mode,slowTime, babki, money);
    }

    @Override
    public void onEnable() {
        counter.reset();
        counter2.reset();
        counterChoice.reset();
        counterTo.reset();
        sent.clear();
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            xuesos();
        }
        if (event instanceof EventPacket eventPacket) {
            pidor(eventPacket);
        }
    }

    private void xuesos() {
        final List<String> players = getOnlinePlayers();

        double distance = Math.sqrt(Math.pow(lastPosX - mc.player.getX(), 2) + Math.pow(lastPosY - mc.player.getY(), 2) + Math.pow(lastPosZ - mc.player.getZ(), 2));

        if (distance > 500) {
            toggle();
        }

        lastPosX = mc.player.getX();
        lastPosY = mc.player.getY();
        lastPosZ = mc.player.getZ();

        if (counter2.hasReached(800L * players.size())) {
            sent.clear();
            counter2.reset();
        }

        for (final String player : players) {
            if (!sent.contains(player) && !player.equals(mc.player.getGameProfile().getName())) {
                if (counter.hasReached(slowTime.get().longValue())) {
                    if (babki.get()) {
                        mc.player.networkHandler.sendCommand("duel " + player + " " + money.getValue());
                    } else {
                        mc.player.networkHandler.sendCommand("duel " + player);
                    }
                    sent.add(player);
                    counter.reset();
                }
            }
        }

        if (mc.currentScreen != null && mc.player.currentScreenHandler instanceof ScreenHandler chest) {
            String title = mc.currentScreen.getTitle().getString();
            if (title.contains("Выбор набора (1/1)")) {
                int slotID = -1;
                if (counterChoice.hasReached(150)) {
                    if (mode.is("Щит")) slotID = 0;
                    if (mode.is("Шипы 3")) slotID = 1;
                    if (mode.is("Лук")) slotID = 2;
                    if (mode.is("Тотемы")) slotID = 3;
                    if (mode.is("Нодебафф")) slotID = 4;
                    if (mode.is("Шары")) slotID = 5;
                    if (mode.is("Классик")) slotID = 6;
                    if (mode.is("Читерский рай")) slotID = 7;
                    if (mode.is("Незеритка")) slotID = 8;

                    if (slotID >= 0) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotID, 0, SlotActionType.QUICK_MOVE, mc.player);
                    }
                    counterChoice.reset();
                }
            } else if (title.contains("Настройка поединка")) {
                if (counterTo.hasReached(150)) {
                    mc.interactionManager.clickSlot(chest.syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
                    counterTo.reset();
                }
            }
        }
    }

    private void pidor(EventPacket event) {
        if (event.isReceivePacket()) {
            Packet<?> packet = event.getPacket();

            if (packet instanceof GameMessageS2CPacket chat) {
                final String text = chat.content().toString();
                if ((text.contains("начало") && text.contains("через") && text.contains("секунд!")) || (text.equals("дуэли » во время поединка запрещено использовать команды"))) {
                    toggle();
                }
            }
        }
    }

    private List<String> getOnlinePlayers() {
        List<String> result = mc.player.networkHandler.getPlayerList().stream()
                .map(PlayerListEntry::getProfile)
                .map(GameProfile::getName)
                .filter(profileName -> pattern.matcher(profileName).matches())
                .collect(Collectors.toList());
        return result;
    }
}
