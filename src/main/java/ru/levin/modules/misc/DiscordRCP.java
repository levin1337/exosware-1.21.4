package ru.levin.modules.misc;

import ru.levin.com.discord.DiscordEventHandlers;
import ru.levin.com.discord.DiscordRPC;
import ru.levin.com.discord.DiscordRichPresence;
import ru.levin.events.Event;
import ru.levin.events.impl.EventUpdate;
import ru.levin.events.impl.render.EventRender2D;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "DiscordRPC", desc = "Активность в дискорде", type = Type.Misc)
public class DiscordRCP extends Function {
    private final DiscordRPC rpc = DiscordRPC.INSTANCE;
    private volatile boolean started = false;
    private Thread thread;
    private final DiscordRichPresence presence = new DiscordRichPresence();

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventUpdate) {
            startRpc();
        }
    }

    public synchronized void startRpc() {
        if (started) return;
        started = true;
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        rpc.Discord_Initialize("1384873696375603281", handlers, true, "");
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.largeImageText = "https://t.me/exosware";

        updatePresenceFields();

        rpc.Discord_UpdatePresence(presence);

        thread = new Thread(() -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    rpc.Discord_RunCallbacks();

                    updatePresenceFields();

                    rpc.Discord_UpdatePresence(presence);

                    Thread.sleep(2000L);
                }
            } catch (InterruptedException ignored) {
            }
        }, "TH-RPC-Handler");
        thread.setDaemon(true);
        thread.start();
    }

    private void updatePresenceFields() {
        presence.details = "User: " + Manager.USER_PROFILE.getName();
        presence.state = "Role: " + Manager.USER_PROFILE.getRole();

        presence.button_label_1 = "Купить";
        presence.button_url_1 = "https://exosware.ru";
        presence.button_label_2 = "Телеграмм";
        presence.button_url_2 = "https://t.me/exosware";

        presence.largeImageKey = "https://api.exosware.ru/api/loader/discord.gif";
    }

    @Override
    public void onDisable() {
        started = false;
        if (thread != null && thread.isAlive()) {
            thread.interrupt();
        }
        rpc.Discord_Shutdown();
    }
}
