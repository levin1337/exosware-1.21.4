package ru.levin.manager.proxyManager;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.apache.commons.io.FileUtils;
import ru.levin.manager.IMinecraft;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class ProxyManager implements IMinecraft {
    private final File CONFIG_PATH = new File(MinecraftClient.getInstance().runDirectory, "files/proxy.ew");
    public HashMap<String, Proxy> accounts = new HashMap<>();
    public String lastPlayerName = "";

    public static boolean proxyEnabled = false;
    public static Proxy proxy = new Proxy();
    public static Proxy lastUsedProxy = new Proxy();
    public static ButtonWidget proxyMenuButton;

    public static String getLastUsedProxyIp() {
        return lastUsedProxy.ipPort.isEmpty() ? "none" : lastUsedProxy.getIp();
    }

    public void init() {
        try {
            if (!CONFIG_PATH.exists() && !CONFIG_PATH.createNewFile()) {
                System.out.println("Error creating proxy config file");
                return;
            }

            String content = FileUtils.readFileToString(CONFIG_PATH, StandardCharsets.UTF_8);
            if (!content.isEmpty()) {
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                proxyEnabled = json.get("proxy-enabled").getAsBoolean();

                Type type = new TypeToken<HashMap<String, Proxy>>(){}.getType();
                accounts = new Gson().fromJson(json.get("accounts"), type);
                if (accounts == null) accounts = new HashMap<>();
            }
        } catch (Exception e) {
            System.out.println("Error reading proxy config");
            e.printStackTrace();
        }
    }

    public void setDefaultProxy(Proxy proxy) {
        accounts.put("", proxy);
    }

    public void saveConfig() {
        try {
            JsonObject json = new JsonObject();
            json.addProperty("proxy-enabled", proxyEnabled);
            json.add("accounts", new Gson().toJsonTree(accounts));

            FileUtils.write(CONFIG_PATH, new GsonBuilder().setPrettyPrinting().create().toJson(json), StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.out.println("Error saving proxy config");
            e.printStackTrace();
        }
    }
}
