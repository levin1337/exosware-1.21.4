package ru.levin.screens.dropdown;

import net.minecraft.client.gui.DrawContext;
import ru.levin.modules.setting.Setting;
import ru.levin.modules.setting.TextSetting;


public interface SettingRenderer<T extends Setting> {

    void render(DrawContext ctx, T setting, int x, int y, int width, int height);

    boolean mouseClicked(T setting, double mouseX, double mouseY, int button, int x, int y, int width, int height);

    default boolean mouseReleased(T setting, double mouseX, double mouseY, int button, int x, int y, int width, int height) {
        return false;
    }

    default boolean mouseScrolled(T setting, double mouseX, double mouseY, double scrollX, double scrollY, int x, int y, int width, int height) {
        return false;
    }

    default boolean keyPressed(T setting, int keyCode, int scanCode, int modifiers) {
        return false;
    }
    default boolean charTyped(T setting, char c, int modifiers) {
        return false;
    }

    default boolean keyReleased(T setting, int keyCode, int scanCode, int modifiers) {
        return false;
    }

    default void tick(T setting, float delta) {
    }

    int getHeight();
}
