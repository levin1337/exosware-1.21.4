package ru.levin.mixin.util;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.levin.manager.ClientManager;

import java.util.List;
@Mixin(ServerList.class)
public class MixinServerList {
    @Unique
    private final List<ServerInfo> serverTop = List.of(
            new ServerInfo("Лучший HvH сервер!", "mc.furryhvh.ru", ServerInfo.ServerType.OTHER)
    );

    @Shadow @Final private List<ServerInfo> servers;
    @Shadow @Final private List<ServerInfo> hiddenServers;

    @Inject(method = "loadFile", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/ServerList;hiddenServers:Ljava/util/List;", ordinal = 0))
    private void loadFileHook(CallbackInfo ci) {
        if (ClientManager.legitMode) {
            servers.removeIf(si -> serverTop.stream().anyMatch(top -> top.address.equals(si.address)));
        } else {
            for (ServerInfo top : serverTop) {
                boolean exists = servers.stream().anyMatch(si -> si.address.equals(top.address));
                if (!exists) servers.add(top);
            }
        }
        removeDuplicates();
    }

    @Unique
    private void removeDuplicates() {
        removeDuplicatesFromList(servers);
        removeDuplicatesFromList(hiddenServers);
    }
    @Unique
    private void removeDuplicatesFromList(List<ServerInfo> list) {
        java.util.Set<String> seen = new java.util.HashSet<>();
        list.removeIf(si -> !seen.add(si.address));
    }

    @Redirect(method = "saveFile", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/NbtList;add(Ljava/lang/Object;)Z", ordinal = 0))
    private boolean saveFileHook(NbtList instance, Object o, @Local(ordinal = 0) ServerInfo info) {
        if (!ClientManager.legitMode && serverTop.stream().anyMatch(top -> top.address.equals(info.address))) {
            return true;
        }
        return instance.add((NbtElement) o);
    }
}

