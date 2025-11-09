package ru.levin.manager.apiManager;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class ClientAPI {

    private final String host;
    private final int port;
    private final ConcurrentHashMap<String, Boolean> cache = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private static final String SECRET_KEY = "k0tikBolomotik";

    public ClientAPI(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void isClientUserAsync(String nick, Consumer<Boolean> callback) {
        if (cache.containsKey(nick)) {
            callback.accept(cache.get(nick));
            return;
        }

        executor.submit(() -> {
            boolean result = isClientUser(nick);
            cache.put(nick, result);
            callback.accept(result);
        });
    }

    public boolean isClientUser(String nick) {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            String hash = sha256(SECRET_KEY + nick);
            out.write("check:" + hash + ":" + nick + "\n");
            out.flush();

            String response = in.readLine();
            return "true".equalsIgnoreCase(response);
        } catch (IOException e) {
            return false;
        }
    }

    public void addPlayer(String nick) {
        executor.submit(() -> sendCommand("add", nick));
    }

    public void removePlayer(String nick) {
        executor.submit(() -> sendCommand("remove", nick));
    }

    private void sendCommand(String cmd, String nick) {
        try (Socket socket = new Socket(host, port);
             BufferedWriter out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            String hash = sha256(SECRET_KEY + nick);
            out.write(cmd + ":" + hash + ":" + nick + "\n");
            out.flush();
        } catch (IOException ignored) {}
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
