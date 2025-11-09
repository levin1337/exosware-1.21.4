package ru.levin.modules;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ru.levin.modules.misc.ClientSounds;
import ru.levin.modules.setting.*;
import ru.levin.events.Event;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.manager.notificationManager.NotificationType;
import ru.levin.util.player.AudioUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("All")
public abstract class Function implements IMinecraft {
    private final FunctionAnnotation initerFunctions = this.getClass().getAnnotation(FunctionAnnotation.class);

    public String name;
    public String keywords;
    private Type category;
    public int bind;
    public String desc;
    public boolean state;
    public boolean expanded;
    private final ArrayList<Setting> settings = new ArrayList<>();

    public Function() {
        initializeProperties();
    }

    public Function(String name, Type category) {
        this.name = name;
        this.category = category;
        this.state = false;
        this.bind = 0;
    }

    private void initializeProperties() {
        name = initerFunctions.name();
        desc = initerFunctions.desc();
        category = initerFunctions.type();
        keywords = Arrays.toString(initerFunctions.keywords());
        state = false;
        bind = initerFunctions.key();
    }

    public abstract void onEvent(final Event event);

    public final void setState(final boolean enabled) {
        if (state == enabled) return;

        state = enabled;
        try {
            if (state) onEnable();
            else onDisable();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onEnable() {}
    protected void onDisable() {}

    public void addSettings(Setting... options) {
        settings.addAll(Arrays.asList(options));
    }

    public void toggle() {
        state = !state;
        try {
            if (state) {
                onEnable();
                playSound(true);
            } else {
                onDisable();
                playSound(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Manager.NOTIFICATION_MANAGER.add(state ? NotificationType.SUCCESS : NotificationType.REMOVED, name, "Модуль " + (state ? "включен!" : "выключен!"), 2);
    }

    private void playSound(boolean enable) {
        ClientSounds clientSounds = Manager.FUNCTION_MANAGER.clientSounds;
        if (!clientSounds.state) return;
        String mode = clientSounds.mode.get();
        String soundFile;

        switch (mode) {
            case "Type-1" -> soundFile = enable ? "nuron.wav" : "nuroff.wav";
            case "Type-2" -> soundFile = enable ? "akron.wav" : "akroff.wav";
            case "Type-3" -> soundFile = enable ? "celon.wav" : "celoff.wav";
            case "Type-4" -> soundFile = enable ? "enableold.wav" : "disableold.wav";
            default -> {
                return;
            }
        }
        AudioUtil.playSound(soundFile);
    }

    public Type getCategory() {
        return category;
    }

    public boolean isState() {
        return state;
    }

    public List<Setting> getSettings() {
        return settings;
    }

    public int getBindCode() {
        return bind;
    }

    public void setBindCode(int key) {
        bind = key;
    }

    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("state", state);
        object.addProperty("keyIndex", bind);

        JsonObject propertiesObject = new JsonObject();
        for (Setting set : settings) {
            if (set instanceof BindBooleanSetting bbs) {
                JsonObject bbsObject = new JsonObject();
                bbsObject.addProperty("state", bbs.get());
                bbsObject.addProperty("bindKey", bbs.getBindKey());
                propertiesObject.add(bbs.getName(), bbsObject);
            } else if (set instanceof BooleanSetting bs) {
                propertiesObject.addProperty(set.getName(), bs.get());
            } else if (set instanceof MultiSetting ms) {
                propertiesObject.addProperty(set.getName(), ms.getConfigValue());
            } else if (set instanceof ModeSetting ms) {
                propertiesObject.addProperty(set.getName(), ms.get());
            } else if (set instanceof SliderSetting ss) {
                propertiesObject.addProperty(set.getName(), ss.get());
            } else if (set instanceof BindSetting bs) {
                propertiesObject.addProperty(set.getName(), bs.getKey());
            } else if (set instanceof TextSetting ts) {
                propertiesObject.addProperty(set.getName(), ts.getValue());
            }
        }

        object.add("Settings", propertiesObject);
        return object;
    }

    public void load(JsonObject object) {
        if (object == null) return;

        if (object.has("state")) {
            setState(object.get("state").getAsBoolean());
        }
        if (object.has("keyIndex")) {
            setBindCode(object.get("keyIndex").getAsInt());
        }

        JsonElement settingsElement = object.get("Settings");
        if (settingsElement == null || !settingsElement.isJsonObject()) return;

        JsonObject propertiesObject = settingsElement.getAsJsonObject();

        for (Setting set : settings) {
            String name = set.getName();
            if (!propertiesObject.has(name)) continue;

            if (set instanceof BindBooleanSetting bbs && propertiesObject.get(name).isJsonObject()) {
                JsonObject bbsObject = propertiesObject.getAsJsonObject(name);
                if (bbsObject.has("state")) bbs.set(bbsObject.get("state").getAsBoolean());
                if (bbsObject.has("bindKey")) bbs.setKey(bbsObject.get("bindKey").getAsInt());
            } else {
                if (set instanceof BooleanSetting bs) {
                    bs.set(propertiesObject.get(name).getAsBoolean());
                } else if (set instanceof MultiSetting ms) {
                    ms.setConfigValue(propertiesObject.get(name).getAsString());
                } else if (set instanceof ModeSetting ms) {
                    ms.set(propertiesObject.get(name).getAsString());
                } else if (set instanceof SliderSetting ss) {
                    ss.set(propertiesObject.get(name).getAsFloat());
                } else if (set instanceof BindSetting bs) {
                    bs.setKey(propertiesObject.get(name).getAsInt());
                } else if (set instanceof TextSetting ts) {
                    ts.setValue(propertiesObject.get(name).getAsString());
                }
            }
        }
    }
}
