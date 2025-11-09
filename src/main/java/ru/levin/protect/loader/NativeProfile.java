package ru.levin.protect.loader;

@SuppressWarnings("All")
public class NativeProfile {
    private static String username;
    private static String role;
    private static String expiryDate;

    static {
        username = System.getProperty("levin.username", "N/A");
        role = System.getProperty("levin.role", "Unknown role");
        expiryDate = System.getProperty("levin.expiry", "Unknown date!");
    }

    public NativeProfile(String name, String role, String expiry) {
        this.username = name;
        this.role = role;
        this.expiryDate = expiry;
    }

    public static String getName() {
        return username;
    }

    public static String getRole() {
        return role;
    }

    public static String getExpiryDate() {
        return expiryDate;
    }
}
