package ru.levin.manager.friendManager;

import net.minecraft.client.MinecraftClient;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public final class FriendManager {
    public final List<Friend> friends = new CopyOnWriteArrayList<>();
    private final Path FRIENDS_FILE = Paths.get(Objects.requireNonNull(MinecraftClient.getInstance().runDirectory).getAbsolutePath(), "files", "friends.ew");

    public void init() {
        try {
            Files.createDirectories(FRIENDS_FILE.getParent());
            if (Files.notExists(FRIENDS_FILE)) {
                Files.createFile(FRIENDS_FILE);
            } else {
                readFriends();
            }
        } catch (IOException e) {
            System.err.println("FriendManager init error: " + e.getMessage());
        }
    }

    public void addFriend(String name) {
        if (name == null || name.isBlank() || isFriend(name)) return;
        friends.add(new Friend(name));
        updateFile();
    }

    public boolean isFriend(String name) {
        return name != null && friends.stream().anyMatch(f -> f.getName().equalsIgnoreCase(name));
    }

    public void removeFriend(String name) {
        if (name == null) return;
        friends.removeIf(f -> f.getName().equalsIgnoreCase(name));
        updateFile();
    }

    public void clearFriends() {
        friends.clear();
        updateFile();
    }

    public List<Friend> getFriends() {
        return List.copyOf(friends);
    }

    private void updateFile() {
        try {
            Files.write(FRIENDS_FILE, friends.stream().map(Friend::getName).toList(), StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to update friends file: " + e.getMessage());
        }
    }

    private void readFriends() {
        try {
            Files.lines(FRIENDS_FILE, StandardCharsets.UTF_8).map(String::trim).filter(s -> !s.isEmpty()).forEach(name -> friends.add(new Friend(name)));
        } catch (IOException e) {
            System.err.println("Failed to read friends file: " + e.getMessage());
        }
    }
}
