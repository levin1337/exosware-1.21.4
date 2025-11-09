package ru.levin.manager.fontManager;

import java.awt.Font;
import java.util.Objects;

@SuppressWarnings("All")
public class FontUtils {

    public final String fontsDir = "/assets/exosware/font/";

    public volatile RenderFonts[] comfortaa = new RenderFonts[256];
    public static volatile RenderFonts[] durman = new RenderFonts[256];
    public static volatile RenderFonts[] glitched = new RenderFonts[256];
    public static volatile RenderFonts[] icons = new RenderFonts[256];
    public static volatile RenderFonts[] monsterrat = new RenderFonts[256];
    public static volatile RenderFonts[] profont = new RenderFonts[256];
    public static volatile RenderFonts[] sf_bold = new RenderFonts[256];
    public static volatile RenderFonts[] sf_medium = new RenderFonts[256];
    public static volatile RenderFonts[] iconsWex = new RenderFonts[256];
    public static volatile RenderFonts[] gilroy = new RenderFonts[256];
    public static volatile RenderFonts[] gilroy_bold = new RenderFonts[256];
    public static volatile RenderFonts[] hud = new RenderFonts[256];
    public static volatile RenderFonts[] icomoon = new RenderFonts[256];

    private boolean initialized = false;

    public synchronized void init() {
        if (initialized) return;

        initializationFont(comfortaa, "comfortaa.ttf");
        initializationFont(durman, "durman.ttf");
        initializationFont(glitched, "glitched.ttf");
        initializationFont(icons, "icons.ttf");
        initializationFont(monsterrat, "monsterrat.ttf");
        initializationFont(profont, "profont.ttf");
        initializationFont(sf_bold, "sf_bold.ttf");
        initializationFont(sf_medium, "sf_medium.ttf");
        initializationFont(iconsWex, "iconsWex.ttf");
        initializationFont(hud, "hud.ttf");
        initializationFont(gilroy, "gilroy.ttf");
        initializationFont(gilroy_bold, "gilroy-bold.ttf");
        initializationFont(icomoon, "icomoon.ttf");

        initialized = true;
    }

    private void initializationFont(RenderFonts[] fontArray, String fontName) {
        if (fontArray == null) return;
        try {
            Font font = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(FontUtils.class.getResourceAsStream(fontsDir + fontName)));
            for (int i = 1; i < fontArray.length; i++) {
                fontArray[i] = new RenderFonts(font, i);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
