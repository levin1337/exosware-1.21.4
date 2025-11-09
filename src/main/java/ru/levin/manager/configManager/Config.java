package ru.levin.manager.configManager;

import com.google.gson.JsonObject;
import ru.levin.manager.Manager;

import java.io.File;

public final class Config {

    private final File file;

    public Config(String name) {
        this.file = new File(Manager.CONFIG_MANAGER.CONFIG_DIR, name + ".cfg");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public JsonObject save() {
        JsonObject jsonObject = new JsonObject();

        JsonObject modulesObject = new JsonObject();
        Manager.FUNCTION_MANAGER.getFunctions().forEach(module -> modulesObject.add(module.name, module.save()));
        jsonObject.add("Features", modulesObject);

        JsonObject otherObject = new JsonObject();
        if (!otherObject.has("author"))
            otherObject.addProperty("author", Manager.USER_PROFILE.getName());
        if (!otherObject.has("time"))
            otherObject.addProperty("time", System.currentTimeMillis());

        if (Manager.STYLE_MANAGER.getTheme() != null) {
            otherObject.addProperty("theme", Manager.STYLE_MANAGER.getTheme().name);
        }

        jsonObject.add("Others", otherObject);
        return jsonObject;
    }

    public void load(JsonObject object, String configuration, boolean start) {
        if (object.has("Features")) {
            JsonObject modulesObject = object.getAsJsonObject("Features");
            Manager.FUNCTION_MANAGER.getFunctions().forEach(module -> {
                if (!start && module.isState()) {
                    module.setState(false);
                }
                module.load(modulesObject.getAsJsonObject(module.name));
            });
        }

        if (object.has("Others")) {
            JsonObject otherObject = object.getAsJsonObject("Others");
            if (otherObject.has("theme")) {
                String themeName = otherObject.get("theme").getAsString();
                Manager.STYLE_MANAGER.getStyles().forEach(style -> {
                    if (style.name.equals(themeName)) {
                        Manager.STYLE_MANAGER.setTheme(style);
                    }
                });
            }
        }
    }


    public File getFile() {
        return file;
    }
}