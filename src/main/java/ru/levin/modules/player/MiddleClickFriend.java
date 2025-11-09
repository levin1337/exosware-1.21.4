package ru.levin.modules.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.EntityHitResult;
import ru.levin.events.Event;
import ru.levin.events.impl.input.EventMouse;
import ru.levin.manager.ClientManager;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;

@FunctionAnnotation(name = "MiddleClickFriend", keywords = {"MCF"}, desc = "Управление друзьями", type = Type.Player)
public class MiddleClickFriend extends Function {
    @Override
    public void onEvent(Event event) {
        if (event instanceof EventMouse e) {
            if (e.getButton() == 2) {
                if (mc.crosshairTarget instanceof EntityHitResult entityHitResult) {
                    if (entityHitResult.getEntity() instanceof PlayerEntity player) {
                        final String name = player.getName().getString();
                        if (Manager.FRIEND_MANAGER.isFriend(name)) {
                            Manager.FRIEND_MANAGER.removeFriend(name);
                            ClientManager.message(Formatting.GRAY + name + Formatting.RED + " удалён из друзей");
                        } else {
                            Manager.FRIEND_MANAGER.addFriend(name);
                            ClientManager.message(Formatting.GRAY + name + Formatting.GREEN + " добавлен в друзья");
                        }
                    }
                }
            }
        }
    }
}
