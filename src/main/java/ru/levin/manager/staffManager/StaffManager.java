package ru.levin.manager.staffManager;

import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class StaffManager {
    private static final List<String> staffNames = new ArrayList<>();
    private final File file = new File(MinecraftClient.getInstance().runDirectory, "\\files\\staff.ew");

    public void init() throws Exception {
        if (!file.exists()) {
            file.createNewFile();
        } else {
            readStaffs();
        }
    }

    public void addStaff(String name) {
        staffNames.add(name);
        updateFile();
    }

    public boolean isStaff(String staffName) {
        return staffNames.stream().anyMatch(staff -> staff.equals(staffName));
    }

    public void removeStaff(String name) {
        staffNames.removeIf(friend -> friend.equalsIgnoreCase(name));
    }

    public void clearStaffs() {
        staffNames.clear();
        updateFile();
    }

    public List<String> getStaffNames() {
        return staffNames;
    }

    public void updateFile() {
        try {
            StringBuilder builder = new StringBuilder();
            staffNames.forEach(friend -> builder.append(friend).append("\n"));
            Files.write(file.toPath(), builder.toString().getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reload() {
        staffNames.clear();
        readStaffs();
    }

    private void readStaffs() {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new DataInputStream(new FileInputStream(file.getAbsolutePath()))));
            String line;
            while ((line = reader.readLine()) != null) {
                staffNames.add(line);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}