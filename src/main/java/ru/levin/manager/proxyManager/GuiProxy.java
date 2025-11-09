package ru.levin.manager.proxyManager;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.*;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.commons.lang3.StringUtils;
import ru.levin.manager.Manager;

public class GuiProxy extends Screen {
    private boolean isSocks4;
    private TextFieldWidget ipPort, username, password;
    private CheckboxWidget enabledCheck;
    private final Screen parentScreen;
    private String msg = "";
    private int[] positionY;
    private int positionX;

    public GuiProxy(Screen parentScreen) {
        super(Text.literal("proxy"));
        this.parentScreen = parentScreen;
    }

    private static boolean isValidIpPort(String ipP) {
        String[] split = ipP.split(":");
        if (split.length != 2 || !StringUtils.isNumeric(split[1])) return false;
        int port = Integer.parseInt(split[1]);
        return port >= 0 && port <= 0xFFFF;
    }

    private boolean checkProxy() {
        if (!isValidIpPort(ipPort.getText())) {
            msg = Formatting.RED + "Неверный порт";
            ipPort.setFocused(true);
            return false;
        }
        return true;
    }

    private void centerButtons(int amount, int buttonLength, int gap) {
        positionX = (this.width - buttonLength) / 2;
        positionY = new int[amount];
        int startY = (this.height - (amount - 1) * gap) / 2;
        for (int i = 0; i < amount; i++) positionY[i] = startY + gap * i;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        msg = "";
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float partialTicks) {
        super.render(context, mouseX, mouseY, partialTicks);

        if (enabledCheck.isChecked() && !isValidIpPort(ipPort.getText())) enabledCheck.onPress();

        context.drawTextWithShadow(textRenderer, "Тип прокси", positionX - 65, positionY[1] + 5, 0xA08080);
        context.drawCenteredTextWithShadow(textRenderer, "Авторизация", this.width / 2, positionY[3] + 8, Formatting.WHITE.getColorValue());
        context.drawTextWithShadow(textRenderer, "IP:PORT", positionX - 55, positionY[2] + 5, 0xA08080);

        ipPort.render(context, mouseX, mouseY, partialTicks);
        username.render(context, mouseX, mouseY, partialTicks);
        if (!isSocks4) password.render(context, mouseX, mouseY, partialTicks);
    }

    @Override
    public void init() {
        ProxyManager pm = Manager.PROXY_MANAGER;
        centerButtons(10, 160, 26);
        isSocks4 = pm.proxy.type == Proxy.ProxyType.SOCKS4;

        this.addDrawableChild(ButtonWidget.builder(Text.literal(isSocks4 ? "Socks 4" : "Socks 5"), b -> {
            isSocks4 = !isSocks4;
            b.setMessage(Text.literal(isSocks4 ? "Socks 4" : "Socks 5"));
        }).dimensions(positionX, positionY[1], 160, 20).build());

        ipPort = new TextFieldWidget(textRenderer, positionX, positionY[2], 160, 20, Text.literal(""));
        ipPort.setText(pm.proxy.ipPort);
        ipPort.setMaxLength(1024);
        ipPort.setFocused(true);
        addSelectableChild(ipPort);

        username = new TextFieldWidget(textRenderer, positionX, positionY[4], 160, 20, Text.literal(""));
        username.setText(pm.proxy.username);
        username.setMaxLength(255);
        addSelectableChild(username);

        password = new TextFieldWidget(textRenderer, positionX, positionY[5], 160, 20, Text.literal(""));
        password.setText(pm.proxy.password);
        password.setMaxLength(255);
        addSelectableChild(password);

        int posXButtons = (width - 160) / 2;
        addDrawableChild(ButtonWidget.builder(Text.translatable("Готово"), b -> {
            if (!checkProxy()) return;
            pm.proxy = new Proxy(isSocks4, ipPort.getText(), username.getText(), password.getText());
            pm.proxyEnabled = enabledCheck.isChecked();
            pm.setDefaultProxy(pm.proxy);
            pm.saveConfig();
            client.setScreen(new MultiplayerScreen(new TitleScreen()));
        }).dimensions(posXButtons, positionY[8], 77, 20).build());

        enabledCheck = CheckboxWidget.builder(Text.translatable("Включить"), textRenderer)
                .pos((width - 15 - textRenderer.getWidth(Text.translatable("Включить"))) / 2, positionY[7])
                .checked(pm.proxyEnabled)
                .build();
        addDrawableChild(enabledCheck);

        addDrawableChild(ButtonWidget.builder(Text.translatable("Назад"), b -> client.setScreen(parentScreen))
                .dimensions(posXButtons + 160 / 2 + 3, positionY[8], 77, 20).build());
    }

    @Override
    public void close() {
        super.close();
        msg = "";
    }
}
