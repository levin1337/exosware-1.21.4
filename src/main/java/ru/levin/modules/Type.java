package ru.levin.modules;

public enum Type {
    Combat("f"),
    Move("w"),
    Render("E"),
    Player("r"),
    Misc("v");

    public final String icon;

    Type(String icon) {
        this.icon = icon;
    }
}