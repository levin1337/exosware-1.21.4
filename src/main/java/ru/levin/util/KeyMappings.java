package ru.levin.util;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("All")
public class KeyMappings {
    public static List<String> getAllKeys() {
        List<String> keys = new ArrayList<>();

        for (int i = 1; i <= 8; i++) {
            keys.add("MOUSE" + i);
        }
        for (int key = GLFW.GLFW_KEY_SPACE; key <= GLFW.GLFW_KEY_MENU; key++) {
            String name = keyMappings(key);
            if (!name.equals("NONE") && !keys.contains(name)) {
                keys.add(name);
            }
        }
        for (int i = 0; i <= 9; i++) keys.add("NUM" + i);
        keys.add("NUM."); keys.add("NUM/"); keys.add("NUM*"); keys.add("NUM-"); keys.add("NUM+");
        keys.add("NUMENTER"); keys.add("NUM=");
        keys.add("LSHIFT"); keys.add("LCONTROL"); keys.add("LALT"); keys.add("LSUPER");
        keys.add("RSHIFT"); keys.add("RCONTROL"); keys.add("RALT"); keys.add("RSUPER");
        keys.add("MENU");
        for (int i = 1; i <= 12; i++) keys.add("F" + i);

        return keys;
    }
    public static String keyMappings(int keyCode) {
        if (keyCode < -1) {
            int mouseButton = -keyCode - 2;
            return switch (mouseButton) {
                case GLFW.GLFW_MOUSE_BUTTON_1 -> "MOUSE1";
                case GLFW.GLFW_MOUSE_BUTTON_2 -> "MOUSE2";
                case GLFW.GLFW_MOUSE_BUTTON_3 -> "MOUSE3";
                case GLFW.GLFW_MOUSE_BUTTON_4 -> "MOUSE4";
                case GLFW.GLFW_MOUSE_BUTTON_5 -> "MOUSE5";
                case GLFW.GLFW_MOUSE_BUTTON_6 -> "MOUSE6";
                case GLFW.GLFW_MOUSE_BUTTON_7 -> "MOUSE7";
                case GLFW.GLFW_MOUSE_BUTTON_8 -> "MOUSE8";
                default -> "NONE";
            };
        }

        return switch (keyCode) {
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_APOSTROPHE -> "'";
            case GLFW.GLFW_KEY_COMMA -> ",";
            case GLFW.GLFW_KEY_MINUS -> "-";
            case GLFW.GLFW_KEY_PERIOD -> ".";
            case GLFW.GLFW_KEY_SLASH -> "/";
            case GLFW.GLFW_KEY_0 -> "0";
            case GLFW.GLFW_KEY_1 -> "1";
            case GLFW.GLFW_KEY_2 -> "2";
            case GLFW.GLFW_KEY_3 -> "3";
            case GLFW.GLFW_KEY_4 -> "4";
            case GLFW.GLFW_KEY_5 -> "5";
            case GLFW.GLFW_KEY_6 -> "6";
            case GLFW.GLFW_KEY_7 -> "7";
            case GLFW.GLFW_KEY_8 -> "8";
            case GLFW.GLFW_KEY_9 -> "9";
            case GLFW.GLFW_KEY_SEMICOLON -> ";";
            case GLFW.GLFW_KEY_EQUAL -> "=";
            case GLFW.GLFW_KEY_A -> "A";
            case GLFW.GLFW_KEY_B -> "B";
            case GLFW.GLFW_KEY_C -> "C";
            case GLFW.GLFW_KEY_D -> "D";
            case GLFW.GLFW_KEY_E -> "E";
            case GLFW.GLFW_KEY_F -> "F";
            case GLFW.GLFW_KEY_G -> "G";
            case GLFW.GLFW_KEY_H -> "H";
            case GLFW.GLFW_KEY_I -> "I";
            case GLFW.GLFW_KEY_J -> "J";
            case GLFW.GLFW_KEY_K -> "K";
            case GLFW.GLFW_KEY_L -> "L";
            case GLFW.GLFW_KEY_M -> "M";
            case GLFW.GLFW_KEY_N -> "N";
            case GLFW.GLFW_KEY_O -> "O";
            case GLFW.GLFW_KEY_P -> "P";
            case GLFW.GLFW_KEY_Q -> "Q";
            case GLFW.GLFW_KEY_R -> "R";
            case GLFW.GLFW_KEY_S -> "S";
            case GLFW.GLFW_KEY_T -> "T";
            case GLFW.GLFW_KEY_U -> "U";
            case GLFW.GLFW_KEY_V -> "V";
            case GLFW.GLFW_KEY_W -> "W";
            case GLFW.GLFW_KEY_X -> "X";
            case GLFW.GLFW_KEY_Y -> "Y";
            case GLFW.GLFW_KEY_Z -> "Z";
            case GLFW.GLFW_KEY_LEFT_BRACKET -> "[";
            case GLFW.GLFW_KEY_BACKSLASH -> "\\";
            case GLFW.GLFW_KEY_RIGHT_BRACKET -> "]";
            case GLFW.GLFW_KEY_GRAVE_ACCENT -> "`";
            case GLFW.GLFW_KEY_ESCAPE -> "ESCAPE";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_PAGE_UP -> "PAGEUP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PAGEDOWN";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPSLOCK";
            case GLFW.GLFW_KEY_SCROLL_LOCK -> "SCROLLLOCK";
            case GLFW.GLFW_KEY_NUM_LOCK -> "NUMLOCK";
            case GLFW.GLFW_KEY_PRINT_SCREEN -> "PRINTSCREEN";
            case GLFW.GLFW_KEY_PAUSE -> "PAUSE";
            case GLFW.GLFW_KEY_F1 -> "F1";
            case GLFW.GLFW_KEY_F2 -> "F2";
            case GLFW.GLFW_KEY_F3 -> "F3";
            case GLFW.GLFW_KEY_F4 -> "F4";
            case GLFW.GLFW_KEY_F5 -> "F5";
            case GLFW.GLFW_KEY_F6 -> "F6";
            case GLFW.GLFW_KEY_F7 -> "F7";
            case GLFW.GLFW_KEY_F8 -> "F8";
            case GLFW.GLFW_KEY_F9 -> "F9";
            case GLFW.GLFW_KEY_F10 -> "F10";
            case GLFW.GLFW_KEY_F11 -> "F11";
            case GLFW.GLFW_KEY_F12 -> "F12";
            case GLFW.GLFW_KEY_KP_0 -> "NUM0";
            case GLFW.GLFW_KEY_KP_1 -> "NUM1";
            case GLFW.GLFW_KEY_KP_2 -> "NUM2";
            case GLFW.GLFW_KEY_KP_3 -> "NUM3";
            case GLFW.GLFW_KEY_KP_4 -> "NUM4";
            case GLFW.GLFW_KEY_KP_5 -> "NUM5";
            case GLFW.GLFW_KEY_KP_6 -> "NUM6";
            case GLFW.GLFW_KEY_KP_7 -> "NUM7";
            case GLFW.GLFW_KEY_KP_8 -> "NUM8";
            case GLFW.GLFW_KEY_KP_9 -> "NUM9";
            case GLFW.GLFW_KEY_KP_DECIMAL -> "NUM.";
            case GLFW.GLFW_KEY_KP_DIVIDE -> "NUM/";
            case GLFW.GLFW_KEY_KP_MULTIPLY -> "NUM*";
            case GLFW.GLFW_KEY_KP_SUBTRACT -> "NUM-";
            case GLFW.GLFW_KEY_KP_ADD -> "NUM+";
            case GLFW.GLFW_KEY_KP_ENTER -> "NUMENTER";
            case GLFW.GLFW_KEY_KP_EQUAL -> "NUM=";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCONTROL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_LEFT_SUPER -> "LSUPER";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCONTROL";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_RIGHT_SUPER -> "RSUPER";
            case GLFW.GLFW_KEY_MENU -> "MENU";
            default -> "NONE";
        };
    }

    public static int keyCode(String name) {
        name = name.toUpperCase();
        return switch (name) {
            case "MOUSE1" -> -GLFW.GLFW_MOUSE_BUTTON_1 - 2;
            case "MOUSE2" -> -GLFW.GLFW_MOUSE_BUTTON_2 - 2;
            case "MOUSE3" -> -GLFW.GLFW_MOUSE_BUTTON_3 - 2;
            case "MOUSE4" -> -GLFW.GLFW_MOUSE_BUTTON_4 - 2;
            case "MOUSE5" -> -GLFW.GLFW_MOUSE_BUTTON_5 - 2;
            case "MOUSE6" -> -GLFW.GLFW_MOUSE_BUTTON_6 - 2;
            case "MOUSE7" -> -GLFW.GLFW_MOUSE_BUTTON_7 - 2;
            case "MOUSE8" -> -GLFW.GLFW_MOUSE_BUTTON_8 - 2;
            case "SPACE" -> GLFW.GLFW_KEY_SPACE;
            case "'" -> GLFW.GLFW_KEY_APOSTROPHE;
            case "," -> GLFW.GLFW_KEY_COMMA;
            case "-" -> GLFW.GLFW_KEY_MINUS;
            case "." -> GLFW.GLFW_KEY_PERIOD;
            case "/" -> GLFW.GLFW_KEY_SLASH;
            case "0" -> GLFW.GLFW_KEY_0;
            case "1" -> GLFW.GLFW_KEY_1;
            case "2" -> GLFW.GLFW_KEY_2;
            case "3" -> GLFW.GLFW_KEY_3;
            case "4" -> GLFW.GLFW_KEY_4;
            case "5" -> GLFW.GLFW_KEY_5;
            case "6" -> GLFW.GLFW_KEY_6;
            case "7" -> GLFW.GLFW_KEY_7;
            case "8" -> GLFW.GLFW_KEY_8;
            case "9" -> GLFW.GLFW_KEY_9;
            case ";" -> GLFW.GLFW_KEY_SEMICOLON;
            case "=" -> GLFW.GLFW_KEY_EQUAL;
            case "A" -> GLFW.GLFW_KEY_A;
            case "B" -> GLFW.GLFW_KEY_B;
            case "C" -> GLFW.GLFW_KEY_C;
            case "D" -> GLFW.GLFW_KEY_D;
            case "E" -> GLFW.GLFW_KEY_E;
            case "F" -> GLFW.GLFW_KEY_F;
            case "G" -> GLFW.GLFW_KEY_G;
            case "H" -> GLFW.GLFW_KEY_H;
            case "I" -> GLFW.GLFW_KEY_I;
            case "J" -> GLFW.GLFW_KEY_J;
            case "K" -> GLFW.GLFW_KEY_K;
            case "L" -> GLFW.GLFW_KEY_L;
            case "M" -> GLFW.GLFW_KEY_M;
            case "N" -> GLFW.GLFW_KEY_N;
            case "O" -> GLFW.GLFW_KEY_O;
            case "P" -> GLFW.GLFW_KEY_P;
            case "Q" -> GLFW.GLFW_KEY_Q;
            case "R" -> GLFW.GLFW_KEY_R;
            case "S" -> GLFW.GLFW_KEY_S;
            case "T" -> GLFW.GLFW_KEY_T;
            case "U" -> GLFW.GLFW_KEY_U;
            case "V" -> GLFW.GLFW_KEY_V;
            case "W" -> GLFW.GLFW_KEY_W;
            case "X" -> GLFW.GLFW_KEY_X;
            case "Y" -> GLFW.GLFW_KEY_Y;
            case "Z" -> GLFW.GLFW_KEY_Z;
            case "[" -> GLFW.GLFW_KEY_LEFT_BRACKET;
            case "\\" -> GLFW.GLFW_KEY_BACKSLASH;
            case "]" -> GLFW.GLFW_KEY_RIGHT_BRACKET;
            case "`" -> GLFW.GLFW_KEY_GRAVE_ACCENT;
            case "ESCAPE" -> GLFW.GLFW_KEY_ESCAPE;
            case "ENTER" -> GLFW.GLFW_KEY_ENTER;
            case "TAB" -> GLFW.GLFW_KEY_TAB;
            case "BACKSPACE" -> GLFW.GLFW_KEY_BACKSPACE;
            case "INSERT" -> GLFW.GLFW_KEY_INSERT;
            case "DELETE" -> GLFW.GLFW_KEY_DELETE;
            case "RIGHT" -> GLFW.GLFW_KEY_RIGHT;
            case "LEFT" -> GLFW.GLFW_KEY_LEFT;
            case "DOWN" -> GLFW.GLFW_KEY_DOWN;
            case "UP" -> GLFW.GLFW_KEY_UP;
            case "PAGEUP" -> GLFW.GLFW_KEY_PAGE_UP;
            case "PAGEDOWN" -> GLFW.GLFW_KEY_PAGE_DOWN;
            case "HOME" -> GLFW.GLFW_KEY_HOME;
            case "END" -> GLFW.GLFW_KEY_END;
            case "CAPSLOCK" -> GLFW.GLFW_KEY_CAPS_LOCK;
            case "SCROLLLOCK" -> GLFW.GLFW_KEY_SCROLL_LOCK;
            case "NUMLOCK" -> GLFW.GLFW_KEY_NUM_LOCK;
            case "PRINTSCREEN" -> GLFW.GLFW_KEY_PRINT_SCREEN;
            case "PAUSE" -> GLFW.GLFW_KEY_PAUSE;
            case "F1" -> GLFW.GLFW_KEY_F1;
            case "F2" -> GLFW.GLFW_KEY_F2;
            case "F3" -> GLFW.GLFW_KEY_F3;
            case "F4" -> GLFW.GLFW_KEY_F4;
            case "F5" -> GLFW.GLFW_KEY_F5;
            case "F6" -> GLFW.GLFW_KEY_F6;
            case "F7" -> GLFW.GLFW_KEY_F7;
            case "F8" -> GLFW.GLFW_KEY_F8;
            case "F9" -> GLFW.GLFW_KEY_F9;
            case "F10" -> GLFW.GLFW_KEY_F10;
            case "F11" -> GLFW.GLFW_KEY_F11;
            case "F12" -> GLFW.GLFW_KEY_F12;
            case "NUM0" -> GLFW.GLFW_KEY_KP_0;
            case "NUM1" -> GLFW.GLFW_KEY_KP_1;
            case "NUM2" -> GLFW.GLFW_KEY_KP_2;
            case "NUM3" -> GLFW.GLFW_KEY_KP_3;
            case "NUM4" -> GLFW.GLFW_KEY_KP_4;
            case "NUM5" -> GLFW.GLFW_KEY_KP_5;
            case "NUM6" -> GLFW.GLFW_KEY_KP_6;
            case "NUM7" -> GLFW.GLFW_KEY_KP_7;
            case "NUM8" -> GLFW.GLFW_KEY_KP_8;
            case "NUM9" -> GLFW.GLFW_KEY_KP_9;
            case "NUM." -> GLFW.GLFW_KEY_KP_DECIMAL;
            case "NUM/" -> GLFW.GLFW_KEY_KP_DIVIDE;
            case "NUM*" -> GLFW.GLFW_KEY_KP_MULTIPLY;
            case "NUM-" -> GLFW.GLFW_KEY_KP_SUBTRACT;
            case "NUM+" -> GLFW.GLFW_KEY_KP_ADD;
            case "NUMENTER" -> GLFW.GLFW_KEY_KP_ENTER;
            case "NUM=" -> GLFW.GLFW_KEY_KP_EQUAL;
            case "LSHIFT" -> GLFW.GLFW_KEY_LEFT_SHIFT;
            case "LCONTROL" -> GLFW.GLFW_KEY_LEFT_CONTROL;
            case "LALT" -> GLFW.GLFW_KEY_LEFT_ALT;
            case "LSUPER" -> GLFW.GLFW_KEY_LEFT_SUPER;
            case "RSHIFT" -> GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "RCONTROL" -> GLFW.GLFW_KEY_RIGHT_CONTROL;
            case "RALT" -> GLFW.GLFW_KEY_RIGHT_ALT;
            case "RSUPER" -> GLFW.GLFW_KEY_RIGHT_SUPER;
            case "MENU" -> GLFW.GLFW_KEY_MENU;
            default -> {
                try {
                    java.lang.reflect.Field field = org.lwjgl.glfw.GLFW.class.getDeclaredField("GLFW_KEY_" + name);
                    yield field.getInt(null);
                } catch (Exception e) {
                    yield -1;
                }
            }
        };
    }
}