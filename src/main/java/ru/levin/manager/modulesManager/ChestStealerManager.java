package ru.levin.manager.modulesManager;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ChestStealerManager {

    private final File file;
    private final Set<Item> whitelist = new HashSet<>();

    public ChestStealerManager() {
        this.file = new File(
                new File(Objects.requireNonNull(MinecraftClient.getInstance().runDirectory), "files/modules"),
                "cheststealer.ew"
        );
        load();

        if (whitelist.isEmpty()) {
            addDefaultItems();
            save();
        }
    }

    private void addDefaultItems() {
        addItem("minecraft:totem_of_undying");
        addItem("minecraft:player_head");
    }

    public boolean addItem(String name) {
        Identifier id = Identifier.tryParse(name);
        if (id != null && Registries.ITEM.containsId(id)) {
            Item item = Registries.ITEM.get(id);
            if (whitelist.add(item)) {
                save();
                return true;
            }
        }
        return false;
    }

    public boolean removeItem(String name) {
        Identifier id = Identifier.tryParse(name);
        if (id != null && Registries.ITEM.containsId(id)) {
            Item item = Registries.ITEM.get(id);
            if (whitelist.remove(item)) {
                save();
                return true;
            }
        }
        return false;
    }

    public boolean isAllowed(Item item) {
        return whitelist.contains(item);
    }

    public Set<Item> getWhitelist() {
        return whitelist;
    }

    private void save() {
        try {
            file.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                for (Item item : whitelist) {
                    Identifier id = Registries.ITEM.getId(item);
                    writer.write(id.toString());
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        whitelist.clear();
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Identifier id = Identifier.tryParse(line.trim());
                if (id != null && Registries.ITEM.containsId(id)) {
                    whitelist.add(Registries.ITEM.get(id));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
