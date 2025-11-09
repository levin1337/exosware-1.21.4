package ru.levin.mixin.util;

import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import ru.levin.manager.Manager;

import static ru.levin.manager.IMinecraft.mc;

@Mixin(value = {TextVisitFactory.class})
public class MixinTextVisitFactory {
    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/text/TextVisitFactory;visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z", ordinal = 0), method = {"visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z" }, index = 0)
    private static String adjustText(String text) {
        return protect(text);
    }

    private static String protect(String string) {
        if (Manager.FUNCTION_MANAGER != null) {
            if (!Manager.FUNCTION_MANAGER.nameProtect.state || mc.player == null) {
                return string;
            }
            String me = mc.getSession().getUsername();
            if (string.contains(me) || (Manager.FRIEND_MANAGER.friends.stream().anyMatch(i -> i.getName().contains(string)) && Manager.FUNCTION_MANAGER.nameProtect.friend.get())) {
                return string.replace(me, Manager.FUNCTION_MANAGER.nameProtect.getCustomName());
            }
        }
        return string;
    }
}
