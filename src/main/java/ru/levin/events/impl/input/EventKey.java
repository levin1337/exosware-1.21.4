package ru.levin.events.impl.input;


import ru.levin.events.Event;

public class EventKey extends Event {
    public int key;

    public EventKey(int key) {
        this.key = key;
    }
}
