package ru.levin.manager;


import ru.levin.protect.AES;

public class UrlManager {
    public final String host = "185.244.172.20";
    public final String webhookIdea() {
        String hook = "Pu11pRLGmzQ/jHzBizY9N0WIl4lkbqWLI06mVwVTlWgbqfxRTdM3R15WGOIlIOKNxH4Itr0oDmmU5UQK5AOBXTo2pgwOw4opfOomn/CH+FO+hKfmLKM0hfji3DJZEyETY7Ug6ZI4BgnK7xzrrD0CkvONZVcxZSZM9pE28Hk3CsM=";
        try {
            return AES.decrypt(hook);
        } catch (Exception e) {
        }
        return hook;
    }
}
