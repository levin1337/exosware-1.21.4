package ru.levin.modules.misc;

import ru.levin.events.Event;
import ru.levin.events.impl.EventPacket;
import ru.levin.events.impl.EventUpdate;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.ModeSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.player.TimerUtil;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.text.Text;

@FunctionAnnotation(name = "AutoDuelBot", type = Type.Misc,desc = "Тест")
public class AutoDuelBot extends Function {
    private final ModeSetting chat = new ModeSetting("Чат", "Локальный", "Локальный", "Глобальный");
    private final SliderSetting fromMoney = new SliderSetting("От сколько монет", 1000, 1000, 1000000, 1000);
    private final SliderSetting beforeMoney = new SliderSetting("До сколько монет", 1000000, 1000, 1000000, 1000);
    private final SliderSetting messageDelay = new SliderSetting("Задержка сообщения", 5000L, 3000L, 30000L, 1000L);

    private final TimerUtil timerUtil = new TimerUtil();

    private String lastNick = null;
    private int lastBet = -1;


    public AutoDuelBot() {
        addSettings(chat, fromMoney, beforeMoney, messageDelay);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            if (timerUtil.hasTimeElapsed(messageDelay.get().longValue())) {
                String msg = "all Кидайте дуель от " + fromMoney.get().intValue() + " до " + beforeMoney.get().intValue() + " монет";
                if (chat.is("Глобальный")) {
                    msg = "! " + msg;
                }
                mc.player.networkHandler.sendChatMessage(msg);
                timerUtil.reset();
            }


            if (lastNick != null && lastBet >= 0) {
                if (lastBet >= fromMoney.get().intValue() && lastBet <= beforeMoney.get().intValue()) {
                    mc.player.networkHandler.sendChatMessage("/duel accept " + lastNick);
                }
                lastNick = null;
                lastBet = -1;
            }

        } else if (event instanceof EventPacket eventPacket) {
            if (eventPacket.getPacket() instanceof GameMessageS2CPacket) {
                Text message = ((GameMessageS2CPacket) eventPacket.getPacket()).content();
                String text = message.getString();


                if (text.startsWith("➝ Ник: ")) {
                    lastNick = text.replace("➝ Ник: ", "").trim();
                } else if (text.startsWith("➝ Ставка: ")) {
                    try {
                        lastBet = Integer.parseInt(text.replace("➝ Ставка: ", "").trim());
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }
}
