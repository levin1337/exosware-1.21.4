package ru.levin.manager.accountManager;

import net.minecraft.client.MinecraftClient;
import ru.levin.manager.ClientManager;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class AccountManager implements IMinecraft {

    private final File file = new File(MinecraftClient.getInstance().runDirectory, "files/alts.ew");
    private final File lastAltFile = new File(MinecraftClient.getInstance().runDirectory, "files/lastAlt.ew");

    private final List<String> accounts = new ArrayList<>();
    private String lastSelectedAccount;

    public void init() {
        accounts.clear();

        if (!file.exists()) {
            try {
                file.getParentFile().mkdirs();
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    accounts.add(line.trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        loadLastAlt();
    }

    public void saveAccounts() {
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
                for (String account : accounts) {
                    writer.write(account);
                    writer.newLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addAccount(String account) {
        if (account != null && !account.trim().isEmpty() && !accounts.contains(account)) {
            accounts.add(account.trim());
            saveAccounts();
        }
    }

    public void removeAccount(String account) {
        if (accounts.remove(account)) {
            saveAccounts();
        }
    }

    public void clearAll() {
        accounts.clear();
        saveAccounts();
    }

    public List<String> getAccounts() {
        return new ArrayList<>(accounts);
    }

    public void setLastSelectedAccount(String account) {
        this.lastSelectedAccount = account;
        saveLastAlt();
    }

    public String getLastSelectedAccount() {
        return lastSelectedAccount;
    }

    public void saveLastAlt() {
        if (lastSelectedAccount == null) return;

        try {
            if (!lastAltFile.exists()) {
                lastAltFile.getParentFile().mkdirs();
                lastAltFile.createNewFile();
            }
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(lastAltFile), StandardCharsets.UTF_8))) {
                writer.write(lastSelectedAccount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadLastAlt() {
        if (!lastAltFile.exists()) return;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lastAltFile), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            if (line != null && !line.trim().isEmpty()) {
                lastSelectedAccount = line.trim();
                String selected = lastSelectedAccount;
                ClientManager.loginAccount(selected);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
