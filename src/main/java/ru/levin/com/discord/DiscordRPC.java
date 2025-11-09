package ru.levin.com.discord;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface DiscordRPC extends Library {
    DiscordRPC INSTANCE = Native.load("discord-rpc", DiscordRPC.class);
    
    void Discord_UpdatePresence(final DiscordRichPresence p0);
    
    void Discord_Shutdown();
    
    void Discord_RunCallbacks();
    
    void Discord_Initialize(final String p0, final DiscordEventHandlers p1, final boolean p2, final String p3);
}
