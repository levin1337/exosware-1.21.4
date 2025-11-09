package ru.levin.manager.configManager;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;
import ru.levin.manager.ClientManager;
import ru.levin.manager.IMinecraft;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public final class ConfigManager implements IMinecraft {

    public final File CONFIG_DIR = new File(MinecraftClient.getInstance().runDirectory, "\\files\\configs");
    private final File autoCfgDir = new File(MinecraftClient.getInstance().runDirectory, "\\files\\configs\\AUTOCFG.cfg");
    private final JsonParser jsonParser = new JsonParser();

    public void init() throws Exception {
        if (!CONFIG_DIR.exists()) {
            CONFIG_DIR.mkdirs();
        } else if (autoCfgDir.exists()) {
            loadConfiguration("AUTOCFG", true);
            System.out.println("[+] Загружаю AutoCfg...");
        } else {
            System.out.println("[-] AutoCfg не найден");
            autoCfgDir.createNewFile();
        }
    }

    public List<String> getAllConfigurations() {
        List<String> configurations = new ArrayList<>();
        File[] files = CONFIG_DIR.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".cfg")) {
                    String configName = file.getName().substring(0, file.getName().lastIndexOf(".cfg"));
                    configurations.add(configName);
                }
            }
        }
        return configurations;
    }

    public void loadConfiguration(String configuration, boolean start) {
        Config config = findConfig(configuration);
        if (config == null) {
            ClientManager.message("Конфиг " + configuration + " не был найден!");
            return;
        }

        try {
            JsonElement element = readJsonFromFile(config.getFile());

            if (element != null && element.isJsonObject()) {
                config.load(element.getAsJsonObject(), configuration, start);
                ClientManager.message("Конфиг " + configuration + " успешно загружен.");
            } else {
                saveConfiguration(configuration);
                ClientManager.message("Конфиг " + configuration + " был создан, так как отсутствовал или был повреждён.");
            }
        } catch (JsonParseException e) {
            ClientManager.message("Ошибка разбора JSON-строки в конфиге " + configuration + "!");
        } catch (NullPointerException e) {
            ClientManager.message("Конфиг " + configuration + " устарел и не может быть загружен!");
        } catch (ClassCastException classCastException) {
            ClientManager.message("Конфиг " + configuration + " пустой или содержит неправильные данные!");
        }
    }


    private JsonElement readJsonFromFile(File file) {
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return jsonParser.parse(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveConfiguration(String configuration) {
        Config config = findConfig(configuration);
        if (config == null) {
            config = new Config(configuration);
        }

        writeJsonToFile(config.getFile(), config.save());
        System.out.println("[+] Конфиг " + configuration + " сохранён");
    }

    private void writeJsonToFile(File file, JsonElement jsonElement) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(jsonElement);
            writer.write(jsonString);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config findConfig(String configName) {
        if (configName == null) return null;
        if (new File(CONFIG_DIR, configName + ".cfg").exists())
            return new Config(configName);

        return null;
    }

    public void deleteConfig(String configName) {
        if (configName == null)
            return;
        Config config = findConfig(configName);
        if (config != null) {
            File file = config.getFile();
            if (file.exists()) {
                boolean deleted = file.delete();
                if (!deleted) {
                    System.out.println("Не удалось удалить конфиг " + file.getAbsolutePath());
                }
            }
        }
    }
}
