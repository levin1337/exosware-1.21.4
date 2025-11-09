package ru.levin.manager.ircManager;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.*;
import ru.levin.manager.Manager;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static ru.levin.manager.IMinecraft.mc;

public class IrcManager {

    private final String cheatName = "ExosWare";
    private final String secretKey = "levinAntiKotopishka";

    private volatile Socket socket;
    private volatile BufferedReader in;
    private volatile BufferedWriter out;

    public volatile String nickname;
    public static final List<String> MESSAGES = new CopyOnWriteArrayList<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "IRC-Scheduler");
        t.setDaemon(true);
        return t;
    });

    private final ExecutorService readerService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "IRC-Reader");
        t.setDaemon(true);
        return t;
    });

    private final ExecutorService writerService = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "IRC-Writer");
        t.setDaemon(true);
        return t;
    });

    private final Object connectLock = new Object();
    private volatile boolean connecting = false;

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final long RETRY_DELAY_MS = 15_000L;

    private final Set<String> ignoredNicks = ConcurrentHashMap.newKeySet();

    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    public IrcManager() {}

    public void connect(String username) {
        this.nickname = username;
        scheduleConnect(0);
        startWriter();
    }

    private void scheduleConnect(long delayMillis) {
        scheduler.schedule(this::tryConnect, delayMillis, TimeUnit.MILLISECONDS);
    }

    private void tryConnect() {
        synchronized (connectLock) {
            if (connecting) return;
            connecting = true;
        }

        try {
            closeConnection();

            Socket s = new Socket();
            s.connect(new InetSocketAddress("11.1.1", 3025), CONNECT_TIMEOUT_MS);
            s.setTcpNoDelay(true);
            s.setKeepAlive(true);

            BufferedReader newIn = new BufferedReader(new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8));
            BufferedWriter newOut = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), StandardCharsets.UTF_8));

            String nonce = newIn.readLine();
            if (nonce == null || nonce.isEmpty()) throw new IOException("Не удалось получить nonce");

            String hmac = hmacSha256(secretKey, nonce + nickname);
            newOut.write(cheatName + ":" + nickname + ":" + hmac + "\n");
            newOut.flush();

            socket = s;
            in = newIn;
            out = newOut;

            startReading();
            logToClient("Подключение установлено");

        } catch (IOException e) {
            logToClient("Не удалось подключиться. Повтор через 15 секунд...");
            scheduleConnect(RETRY_DELAY_MS);
        } finally {
            synchronized (connectLock) { connecting = false; }
        }
    }

    private void startReading() {
        readerService.submit(() -> {
            try {
                String line;
                while (!Thread.currentThread().isInterrupted() && in != null && (line = in.readLine()) != null) {
                    messageClient(line);
                }
            } catch (IOException ignored) {
            } finally {
                reconnect();
            }
        });
    }

    private void reconnect() {
        closeConnection();
        scheduleConnect(RETRY_DELAY_MS);
    }

    private void closeConnection() {
        closeQuietly(in);
        closeQuietly(out);
        if (socket != null) {
            try { socket.close(); } catch (IOException ignored) {}
        }
        in = null;
        out = null;
        socket = null;
    }


    public void messageHost(String msg) {
        if (msg == null || msg.trim().isEmpty()) return;
        messageQueue.offer(msg);
        messageClient("[IRC • " + cheatName + "] » Вы: " + msg);
    }
    public static void addMessage(String msg) {
        MESSAGES.add(msg);

        if (MESSAGES.size() > 200) {
            MESSAGES.remove(0);
        }
    }
    private void startWriter() {
        writerService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String msg = messageQueue.take();
                    BufferedWriter writer = out;
                    if (writer != null) {
                        writer.write(msg);
                        writer.newLine();
                        writer.flush();
                    } else {
                        messageQueue.offer(msg);
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException ignored) {
                    break;
                } catch (IOException e) {
                    reconnect();
                }
            }
        });
    }

    public void messageClient(String string) {
        if (mc == null || mc.player == null || mc.world == null || mc.inGameHud == null) return;

        String[] parts = string.split(":", 2);
        if (parts.length > 1 && isIgnored(parts[0].trim())) return;

        mc.inGameHud.getChatHud().addMessage(applyGradient(string));
        addMessage(string);
    }

    private Text applyGradient(String string) {
        MutableText component = Text.empty();

        int splitIndex = string.indexOf(" » ");
        String prefix = splitIndex != -1 ? string.substring(0, splitIndex) : string;
        String rest = splitIndex != -1 ? string.substring(splitIndex) : "";

        int length = Math.max(prefix.length(), 1);
        for (int i = 0; i < length; i++) {
            float ratio = (float) i / (length - 1);
            int rgb = blendColors(0x808080, 0xFFFFFF, ratio);
            component.append(Text.literal(String.valueOf(prefix.charAt(i))).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(rgb))));
        }

        if (!rest.isEmpty()) {
            component.append(Text.literal(rest).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFFFFFF))));
        }

        return component;
    }

    private int blendColors(int c1, int c2, float ratio) {
        ratio = Math.max(0f, Math.min(1f, ratio));
        int r = (int) (((c1 >> 16) & 0xFF) * (1 - ratio) + ((c2 >> 16) & 0xFF) * ratio);
        int g = (int) (((c1 >> 8) & 0xFF) * (1 - ratio) + ((c2 >> 8) & 0xFF) * ratio);
        int b = (int) ((c1 & 0xFF) * (1 - ratio) + (c2 & 0xFF) * ratio);
        return (r << 16) | (g << 8) | b;
    }

    public void ignoreNick(String nick) {
        if (nick != null) ignoredNicks.add(nick.toLowerCase());
    }

    public void unignoreNick(String nick) {
        if (nick != null) ignoredNicks.remove(nick.toLowerCase());
    }

    public boolean isIgnored(String nick) {
        return nick != null && ignoredNicks.contains(nick.toLowerCase());
    }

    public Set<String> getIgnoredNicks() {
        return Collections.unmodifiableSet(ignoredNicks);
    }

    private void logToClient(String msg) {
        System.out.println(msg);
        if (Manager.FUNCTION_MANAGER.irc.state) messageClient(msg);
    }

    private void closeQuietly(Closeable c) { try { if (c != null) c.close(); } catch (IOException ignored) {} }

    private String hmacSha256(String key, String data) {
        try {
            javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
            mac.init(new javax.crypto.spec.SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) { return ""; }
    }

    public void shutdown() {
        CompletableFuture.runAsync(() -> {
            try { readerService.shutdownNow(); } catch (Exception ignored) {}
            try { writerService.shutdownNow(); } catch (Exception ignored) {}
            try { scheduler.shutdownNow(); } catch (Exception ignored) {}
            closeConnection();
        });
    }
}
