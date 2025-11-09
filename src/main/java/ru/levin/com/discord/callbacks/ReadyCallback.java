package ru.levin.com.discord.callbacks;

import com.sun.jna.Callback;
import ru.levin.com.discord.DiscordUser;

public interface ReadyCallback extends Callback {
    void apply(final DiscordUser p0);
}
