package ru.levin.modules.setting;


import java.util.function.Supplier;

public class TextSetting extends Setting {
    private Supplier<Boolean> visible;
    private String value;

    public boolean isFocused = false;
    public int cursorPosition = 0;
    public boolean cursorVisible = false;
    public boolean hasText = false;

    public boolean isFocused() {
        return isFocused;
    }
    public void setFocused(boolean focused) {
        isFocused = focused;
    }

    public int getCursorPosition() {
        return cursorPosition;
    }
    public void setCursorPosition(int pos) {
        cursorPosition = pos;
    }

    public boolean isCursorVisible() {
        return cursorVisible;
    }

    public boolean hasText() {
        return hasText;
    }
    public void setHasText(boolean has) {
        hasText = has;
    }

    public TextSetting(String name, String value) {
        this.name = name;
        setValue(value);
        setVisible(() -> true);
    }

    public TextSetting(String name, String value, Supplier<Boolean> visible) {
        this.name = name;
        setValue(value);
        setVisible(visible);
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isVisible() {
        return visible.get();
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible;
    }
}