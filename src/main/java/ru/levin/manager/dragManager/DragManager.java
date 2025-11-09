package ru.levin.manager.dragManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.MinecraftClient;
import ru.levin.manager.IMinecraft;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedHashMap;

public class DragManager implements IMinecraft {
    public static LinkedHashMap<String, Dragging> draggables = new LinkedHashMap<>();

    public final File DRAG_DATA = new File(MinecraftClient.getInstance().runDirectory, "\\files\\drag.ew");
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().create();

    public void init() {
        if (!DRAG_DATA.exists()) {
            System.out.println("Файл с позициями draggable не найден. Будет создан новый файл после сохранения.");
            return;
        }

        try {
            Dragging[] loadedDrags = GSON.fromJson(Files.readString(DRAG_DATA.toPath()), Dragging[].class);

            if (loadedDrags != null) {
                for (Dragging dragging : loadedDrags) {
                    if (dragging != null) {
                        Dragging currentDrag = draggables.get(dragging.getName());
                        if (currentDrag != null) {
                            currentDrag.setX(dragging.getX());
                            currentDrag.setY(dragging.getY());
                            draggables.put(dragging.getName(), currentDrag);
                        } else {
                            draggables.put(dragging.getName(), dragging);
                        }
                    }
                }
                System.out.println("Позиции draggable элементов загружены из файла.");

            } else {
                System.out.println("Данные в файле пусты или повреждены.");
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void save() {
        if (!DRAG_DATA.exists()) {
            DRAG_DATA.getParentFile().mkdirs();
        }

        try (FileWriter writer = new FileWriter(DRAG_DATA)) {
            writer.write(GSON.toJson(draggables.values()));
            System.out.println("Позиции draggable элементов успешно сохранены в файл " + DRAG_DATA.getAbsolutePath());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void reset() {
        float off = 10;
        for (Dragging dragging : draggables.values()) {
            float newX = dragging.getDefaultX() + 10;
            float newY = dragging.getDefaultY() + off;
            dragging.setX(newX);
            dragging.setY(newY);
            dragging.targetX = newX;
            dragging.targetY = newY;

            off += dragging.getHeight() + 15;
        }
        save();
    }
}
