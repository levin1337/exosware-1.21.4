package ru.levin.protect;


public class UserProfile {
    private final String name;
    private final String role;
    private final String expiry;

    public UserProfile(String name, String role, String expiry) {
        this.name = name;
        this.role = role;
        this.expiry = expiry;
    }

    public String getName() {
        return name;
    }
    public String getExpiry() {
        return expiry;
    }
    public String getRole() {
        return role;
    }
}
