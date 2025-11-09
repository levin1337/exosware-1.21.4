package ru.levin.events.impl.input;
import ru.levin.events.Event;

public class EventMouse extends Event {
    private int button;

    public EventMouse(int button) {
        this.button = button;
    }

    public void setButton(int button) {
        this.button = button;
    }

    public int getButton() {
        return button;
    }
}
