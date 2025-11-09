package ru.levin.screens.altmanager;

import org.lwjgl.glfw.GLFW;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import ru.levin.manager.ClientManager;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.Manager;
import ru.levin.util.color.ColorUtil;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.math.MathUtil;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.render.Scissor;

@SuppressWarnings("All")
public class AltManager extends Screen implements IMinecraft {
    private final Screen parent;
    private boolean isTyping = false;
    private final StringBuilder inputText = new StringBuilder();
    private final List<String> accounts = Manager.ACCOUNT_MANAGER.getAccounts();
    private float scrollOffset = 0;
    private float targetScrollOffset = 0;
    private float hoverAnimationInput = 0;
    private float[] hoverAnimations1;
    private float[] hoverAnimations2;
    private int selectedAccountIndex = -1;
    private static final float SCALE = 1.5f;

    private float createHoverAnim = 0f, clearHoverAnim = 0f, randomHoverAnim = 0f;
    private float createScale = 1f, clearScale = 1f, randomScale = 1f;

    private final String title = "AltManager";

    private int shakeTime = 0;
    private float shakeOffsetY = 0f;
    private boolean showConfirmDialog = false;
    public AltManager(Screen parent) {
        super(Text.of("Account Manager"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        scrollOffset = MathUtil.lerp(scrollOffset, targetScrollOffset, 8);
        RenderUtil.drawRoundedRect(drawContext.getMatrices(), -1, -1, this.width + 2, this.height + 2, 4, new Color(27, 27, 27, 255).getRGB());
        if (shakeTime > 0) {
            shakeTime--;
            shakeOffsetY = (float)(Math.sin(shakeTime * 0.5) * 3);
        } else {
            shakeOffsetY = 0f;
        }


        int titleWidth = (int) FontUtils.sf_bold[48].getWidth(title);
        float titleX = (this.width - titleWidth) / 2f;
        float titleBaseY = this.height / 7f;
        float titleY = titleBaseY + shakeOffsetY;

        float time = (System.currentTimeMillis() % 4000L) / 1500f;
        FontUtils.sf_bold[48].renderAnimatedGradientText(drawContext.getMatrices(), title, titleX, titleY, ColorUtil.getColorStyle(30), ColorUtil.getColorStyle(260), time);

        int centerX = width / 2;
        int centerY = height / 2;

        int inputWidth = (int)(220 * SCALE);
        int inputHeight = (int)(17 * SCALE);
        int inputX = centerX - (int)(110 * SCALE);
        int inputY = centerY - (int)(92 * SCALE);

        boolean isHoveredInput = RenderUtil.isInRegion(mouseX, mouseY, inputX, inputY, inputWidth, inputHeight);
        hoverAnimationInput = MathUtil.lerp(hoverAnimationInput, isHoveredInput ? 1 : 0, 10);
        int nameColor = ColorUtil.interpolateColor(ColorUtil.rgba(180, 180, 180, 255), ColorUtil.rgba(230, 230, 230, 255), hoverAnimationInput);

        RenderUtil.drawRoundedRect(drawContext.getMatrices(), inputX, inputY, inputWidth, inputHeight, 4, ColorUtil.rgba(35, 35, 35,255));
        if (!isTyping) {
            StringBuilder placeholder = new StringBuilder("Enter your name");
            for (int i = 0; i < (System.currentTimeMillis() / 500 % 4); i++) placeholder.append(".");
            FontUtils.durman[21].drawLeftAligned(drawContext.getMatrices(), placeholder.toString(), inputX + 6, inputY + inputHeight / 2f - 7, nameColor);
        } else {
            StringBuilder builder = new StringBuilder(inputText);
            builder.append((System.currentTimeMillis() / 500 % 2) == 0 ? "_" : "");
            FontUtils.durman[21].drawLeftAligned(drawContext.getMatrices(), builder.toString(), inputX + 6, inputY + inputHeight / 2f - 7, nameColor);
        }

        int listX = inputX;
        int listY = centerY - (int)(70 * SCALE);
        int listWidth = (int)(220 * SCALE);
        int listHeight = (int)(140 * SCALE);

        RenderUtil.drawRoundedRect(drawContext.getMatrices(), listX, listY, listWidth, listHeight, 4,new Color(35, 35, 35,255).getRGB());

        Scissor.push();
        Scissor.setFromComponentCoordinates(listX, listY, listWidth, listHeight);

        if (hoverAnimations1 == null || hoverAnimations1.length != accounts.size()) hoverAnimations1 = new float[accounts.size()];
        if (hoverAnimations2 == null || hoverAnimations2.length != accounts.size()) hoverAnimations2 = new float[accounts.size()];

        float startY = listY + 5;
        float itemHeight = 35 * SCALE;

        for (int i = 0; i < accounts.size(); i++) {
            float y = startY - scrollOffset + i * itemHeight;

            int entryX = centerX - (int)(105 * SCALE);
            int entryWidth = (int)(140 * SCALE);
            int entryHeight = (int)(30 * SCALE);

            RenderUtil.drawRoundedRect(drawContext.getMatrices(), entryX, y, entryWidth + 10, entryHeight, 4, ColorUtil.rgba(23, 23, 23, 255));

            int bgColor = (i == selectedAccountIndex) ? ColorUtil.rgba(50, 50, 80, 255) : ColorUtil.rgba(23, 23, 23, 255);
            RenderUtil.drawRoundedBorder(drawContext.getMatrices(), entryX, y, entryWidth + 10, entryHeight, 4, 0.3f, bgColor);

            FontUtils.durman[21].drawLeftAligned(drawContext.getMatrices(), accounts.get(i), entryX + 10, y + 5, ColorUtil.rgba(200, 200, 200, 255));
            FontUtils.durman[16].drawLeftAligned(drawContext.getMatrices(), "Date " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), entryX + 10, y + 30, ColorUtil.rgba(140, 140, 140, 255));

            int btnWidth = (int)(60 * SCALE);
            int btnHeight = (int)(13 * SCALE);

            int selectBtnX = entryX + entryWidth + (int)(10 * SCALE);
            int selectBtnY = (int)(y);
            boolean accountHovered1 = RenderUtil.isInRegion(mouseX, mouseY, selectBtnX, selectBtnY, btnWidth, btnHeight);
            hoverAnimations1[i] = MathUtil.lerp(hoverAnimations1[i], accountHovered1 ? 1 : 0, 12);

            int selectBgColor = ColorUtil.blendColorsInt(new Color(35, 35, 35).getRGB(), new Color(55, 55, 55).getRGB(), hoverAnimations1[i]);
            int outlineColor = new Color(60, 60, 60, 180).getRGB();
            RenderUtil.drawRoundedRect(drawContext.getMatrices(), selectBtnX, selectBtnY, btnWidth, btnHeight + 2, 4, selectBgColor);
            RenderUtil.drawRoundedBorder(drawContext.getMatrices(), selectBtnX, selectBtnY, btnWidth, btnHeight + 2, 4, 1f, outlineColor);
            FontUtils.sf_medium[20].centeredDraw(drawContext.getMatrices(), "Select", selectBtnX + btnWidth / 2f, selectBtnY + btnHeight / 2f - 6, Color.WHITE.getRGB());

            int deleteBtnX = selectBtnX;
            int deleteBtnY = selectBtnY + btnHeight + (int)(3 * SCALE);
            boolean accountHovered2 = RenderUtil.isInRegion(mouseX, mouseY, deleteBtnX, deleteBtnY, btnWidth, btnHeight);
            hoverAnimations2[i] = MathUtil.lerp(hoverAnimations2[i], accountHovered2 ? 1 : 0, 12);
            int deleteBgColor = ColorUtil.blendColorsInt(new Color(35, 35, 35).getRGB(), new Color(55, 55, 55).getRGB(), hoverAnimations2[i]);
            RenderUtil.drawRoundedRect(drawContext.getMatrices(), deleteBtnX, deleteBtnY, btnWidth, btnHeight + 2, 4, deleteBgColor);
            RenderUtil.drawRoundedBorder(drawContext.getMatrices(), deleteBtnX, deleteBtnY, btnWidth, btnHeight + 2, 4, 1f, outlineColor);
            FontUtils.sf_medium[20].centeredDraw(drawContext.getMatrices(), "Delete", deleteBtnX + btnWidth / 2f, deleteBtnY + btnHeight / 2f - 6, Color.WHITE.getRGB());
        }

        Scissor.unset();
        Scissor.pop();

        int buttonsY = listY + listHeight + (int)(10 * SCALE);
        int buttonWidth = (int)(70 * SCALE);
        int buttonHeight = inputHeight;

        int createX = centerX - buttonWidth - (int)(40 * SCALE);
        int clearX = centerX - (buttonWidth / 2);
        int randomX = centerX + buttonWidth + (int)(-30 * SCALE);

        float animSpeed = 0.04f;

        boolean isHoveredCreate = RenderUtil.isInRegion(mouseX, mouseY, createX, buttonsY, buttonWidth, buttonHeight);
        if (isHoveredCreate) {
            createHoverAnim = Math.min(1f, createHoverAnim + animSpeed);
            createScale = Math.min(1.04f, createScale + animSpeed * 0.5f);
        } else {
            createHoverAnim = Math.max(0f, createHoverAnim - animSpeed);
            createScale = Math.max(1f, createScale - animSpeed * 0.5f);
        }
        int createBgColor = ColorUtil.blendColorsInt(new Color(35, 35, 35).getRGB(), new Color(55, 55, 55).getRGB(), createHoverAnim);
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(createX + buttonWidth / 2f, buttonsY + buttonHeight / 2f, 0);
        drawContext.getMatrices().scale(createScale, createScale, 1);
        drawContext.getMatrices().translate(-(createX + buttonWidth / 2f), -(buttonsY + buttonHeight / 2f), 0);
        RenderUtil.drawRoundedRect(drawContext.getMatrices(), createX, buttonsY, buttonWidth, buttonHeight, 4, createBgColor);
        RenderUtil.drawRoundedBorder(drawContext.getMatrices(), createX, buttonsY, buttonWidth, buttonHeight, 4, 1f, new Color(60, 60, 60, 180).getRGB());
        FontUtils.sf_medium[20].centeredDraw(drawContext.getMatrices(), "Create", createX + buttonWidth / 2f, buttonsY + buttonHeight / 2f - 7, Color.WHITE.getRGB());
        drawContext.getMatrices().pop();

        boolean isHoveredClear = RenderUtil.isInRegion(mouseX, mouseY, clearX, buttonsY, buttonWidth, buttonHeight);
        if (isHoveredClear) {
            clearHoverAnim = Math.min(1f, clearHoverAnim + animSpeed);
            clearScale = Math.min(1.04f, clearScale + animSpeed * 0.5f);
        } else {
            clearHoverAnim = Math.max(0f, clearHoverAnim - animSpeed);
            clearScale = Math.max(1f, clearScale - animSpeed * 0.5f);
        }
        int clearBgColor = ColorUtil.blendColorsInt(new Color(35, 35, 35).getRGB(), new Color(55, 55, 55).getRGB(), clearHoverAnim);
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(clearX + buttonWidth / 2f, buttonsY + buttonHeight / 2f, 0);
        drawContext.getMatrices().scale(clearScale, clearScale, 1);
        drawContext.getMatrices().translate(-(clearX + buttonWidth / 2f), -(buttonsY + buttonHeight / 2f), 0);
        RenderUtil.drawRoundedRect(drawContext.getMatrices(), clearX, buttonsY, buttonWidth, buttonHeight, 4, clearBgColor);
        RenderUtil.drawRoundedBorder(drawContext.getMatrices(), clearX, buttonsY, buttonWidth, buttonHeight, 4, 1f, new Color(60, 60, 60, 180).getRGB());
        FontUtils.sf_medium[20].centeredDraw(drawContext.getMatrices(), "Clear all", clearX + buttonWidth / 2f, buttonsY + buttonHeight / 2f - 7, Color.WHITE.getRGB());
        drawContext.getMatrices().pop();

        boolean isHoveredRandom = RenderUtil.isInRegion(mouseX, mouseY, randomX, buttonsY, buttonWidth, buttonHeight);
        if (isHoveredRandom) {
            randomHoverAnim = Math.min(1f, randomHoverAnim + animSpeed);
            randomScale = Math.min(1.04f, randomScale + animSpeed * 0.5f);
        } else {
            randomHoverAnim = Math.max(0f, randomHoverAnim - animSpeed);
            randomScale = Math.max(1f, randomScale - animSpeed * 0.5f);
        }
        int randomBgColor = ColorUtil.blendColorsInt(new Color(35, 35, 35).getRGB(), new Color(55, 55, 55).getRGB(), randomHoverAnim);
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(randomX + buttonWidth / 2f, buttonsY + buttonHeight / 2f, 0);
        drawContext.getMatrices().scale(randomScale, randomScale, 1);
        drawContext.getMatrices().translate(-(randomX + buttonWidth / 2f), -(buttonsY + buttonHeight / 2f), 0);
        RenderUtil.drawRoundedRect(drawContext.getMatrices(), randomX, buttonsY, buttonWidth, buttonHeight, 4, randomBgColor);
        RenderUtil.drawRoundedBorder(drawContext.getMatrices(), randomX, buttonsY, buttonWidth, buttonHeight, 4, 1f, new Color(60, 60, 60, 180).getRGB());
        FontUtils.sf_medium[20].centeredDraw(drawContext.getMatrices(), "Random", randomX + buttonWidth / 2f, buttonsY + buttonHeight / 2f - 7, Color.WHITE.getRGB());
        drawContext.getMatrices().pop();

        String accountName = mc.getSession().getUsername();
        FontUtils.sf_medium[18].centeredDraw(drawContext.getMatrices(), "Selected account: " + accountName, centerX, buttonsY + buttonHeight + (int)(20 * SCALE), -1);
        FontUtils.sf_medium[18].centeredDraw(drawContext.getMatrices(), "Quantity: " + accounts.size(), centerX, buttonsY + buttonHeight + (int)(40 * SCALE), -1);



        if (showConfirmDialog) {
            drawConfirmDialog(drawContext);
            return;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = width / 2;
        int centerY = height / 2;
        int inputWidth = (int)(220 * SCALE);
        int inputHeight = (int)(17 * SCALE);
        int inputX = centerX - (int)(110 * SCALE);
        int inputY = centerY - (int)(92 * SCALE);

        int buttonWidth = (int)(70 * SCALE);
        int buttonsY = centerY - (int)(70 * SCALE) + (int)(140 * SCALE) + (int)(10 * SCALE);
        int createX = centerX - buttonWidth - (int)(40 * SCALE);
        int clearX = centerX - (buttonWidth / 2);
        int randomX = centerX + buttonWidth + (int)(-30 * SCALE);

        if (RenderUtil.isInRegion(mouseX, mouseY, inputX, inputY, inputWidth, inputHeight) && !isTyping && button == 0) {
            isTyping = true;
            return true;
        }

        if (!RenderUtil.isInRegion(mouseX, mouseY, inputX, inputY, inputWidth, inputHeight) && !RenderUtil.isInRegion(mouseX, mouseY, createX, buttonsY, buttonWidth, inputHeight) && !RenderUtil.isInRegion(mouseX, mouseY, clearX, buttonsY, buttonWidth, inputHeight) && !RenderUtil.isInRegion(mouseX, mouseY, randomX, buttonsY, buttonWidth, inputHeight) && isTyping && button == 0) {
            isTyping = false;
            return true;
        }


        int titleWidth = (int) FontUtils.sf_bold[48].getWidth(title);
        float titleX = (this.width - titleWidth) / 2f;
        float titleY = this.height / 7f;
        if (mouseX >= titleX && mouseX <= titleX + titleWidth && mouseY >= titleY && mouseY <= titleY + 25) {
            shakeTime = 20;
            return true;
        }

        if (RenderUtil.isInRegion(mouseX, mouseY, createX, buttonsY, buttonWidth, inputHeight) && isTyping && button == 0) {
            String newAccount = inputText.toString().trim();
            if (!newAccount.isEmpty() && accounts.stream().noneMatch(a -> a.equalsIgnoreCase(newAccount))) {
                isTyping = false;
                accounts.add(newAccount);
                Manager.ACCOUNT_MANAGER.addAccount(newAccount);
                inputText.setLength(0);
            }
            return true;
        }



        if (showConfirmDialog) {
            int boxWidth = 300;
            int boxHeight = 130;
            int boxX = (width - boxWidth) / 2;
            int boxY = (height - boxHeight) / 2;
            int btnWidth = 90;
            int btnHeight = 28;
            int yesX = boxX + 35;
            int noX = boxX + boxWidth - 35 - btnWidth;
            int btnY = boxY + boxHeight - 50;

            if (RenderUtil.isInRegion(mouseX, mouseY, yesX, btnY, btnWidth, btnHeight)) {
                accounts.clear();
                Manager.ACCOUNT_MANAGER.clearAll();
                selectedAccountIndex = -1;
                showConfirmDialog = false;
                return true;
            }
            if (RenderUtil.isInRegion(mouseX, mouseY, noX, btnY, btnWidth, btnHeight)) {
                showConfirmDialog = false;
                return true;
            }
            return true;
        }

        if (RenderUtil.isInRegion(mouseX, mouseY, clearX, buttonsY, buttonWidth, (int)(17 * SCALE)) && button == 0) {
            showConfirmDialog = true;
            return true;
        }
        if (RenderUtil.isInRegion(mouseX, mouseY, randomX, buttonsY, buttonWidth, inputHeight) && button == 0) {
            int maxNumber = 9999999;
            String prefix = "exosware_";
            String randomNumberStr = String.valueOf((int)(Math.random() * (maxNumber + 1)));
            String randomName = prefix + randomNumberStr;
            if (randomName.length() > 16) {
                randomName = randomName.substring(0, 16);
            }

            if (!accounts.contains(randomName)) {
                accounts.add(randomName);
                Manager.ACCOUNT_MANAGER.addAccount(randomName);
            }

            ClientManager.loginAccount(randomName);

            selectedAccountIndex = accounts.indexOf(randomName);
            Manager.ACCOUNT_MANAGER.setLastSelectedAccount(randomName);
            return true;
        }


        int listX = inputX;
        int listY = centerY - (int)(70 * SCALE);
        int listWidth = (int)(220 * SCALE);
        int listHeight = (int)(140 * SCALE);

        if (RenderUtil.isInRegion(mouseX, mouseY, listX, listY, listWidth, listHeight)) {
            float startY = listY + 5;
            float itemHeight = 35 * SCALE;

            int btnWidth = (int)(60 * SCALE);
            int btnHeight = (int)(13 * SCALE);

            int entryX = centerX - (int)(105 * SCALE);
            int entryWidth = (int)(140 * SCALE);
            int entryHeight = (int)(30 * SCALE);

            for (int i = 0; i < accounts.size(); i++) {
                float y = startY - scrollOffset + i * itemHeight;

                if (RenderUtil.isInRegion(mouseX, mouseY, entryX, (int) y, entryWidth + 10, entryHeight) && button == 0) {
                    String selected = accounts.get(i);
                    ClientManager.loginAccount(selected);
                    selectedAccountIndex = i;
                    Manager.ACCOUNT_MANAGER.setLastSelectedAccount(selected);
                    return true;
                }

                int selectBtnX = entryX + entryWidth + (int)(10 * SCALE);
                int selectBtnY = (int)(y);
                if (RenderUtil.isInRegion(mouseX, mouseY, selectBtnX, selectBtnY, btnWidth, btnHeight) && button == 0) {
                    String selected = accounts.get(i);
                    ClientManager.loginAccount(selected);
                    selectedAccountIndex = i;
                    Manager.ACCOUNT_MANAGER.setLastSelectedAccount(selected);
                    return true;
                }

                int deleteBtnX = selectBtnX;
                int deleteBtnY = selectBtnY + btnHeight + (int)(3 * SCALE);
                if (RenderUtil.isInRegion(mouseX, mouseY, deleteBtnX, deleteBtnY, btnWidth, btnHeight) && button == 0) {
                    if (selectedAccountIndex == i) selectedAccountIndex = -1;
                    Manager.ACCOUNT_MANAGER.removeAccount(accounts.get(i));
                    accounts.remove(i);
                    int maxOffset = Math.max(0, (accounts.size() * (int)(38 * SCALE)) - (int)(135 * SCALE));
                    targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxOffset));
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int centerY = height / 2;
        int listY = centerY - (int)(70 * SCALE);
        int listHeight = (int)(140 * SCALE);

        if (mouseY >= listY && mouseY <= listY + listHeight) {
            targetScrollOffset -= scrollY * (int)(30 * SCALE);
            int maxOffset = Math.max(0, (accounts.size() * (int)(36 * SCALE)) - listHeight);
            targetScrollOffset = Math.max(0, Math.min(targetScrollOffset, maxOffset));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY,scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isTyping) {
            boolean ctrl = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS || GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
            if (ctrl && keyCode == GLFW.GLFW_KEY_V) {
                String clipboard = GLFW.glfwGetClipboardString(mc.getWindow().getHandle());
                if (clipboard != null && !clipboard.isEmpty()) {
                    String filtered = clipboard.replaceAll("[^\\w]", "");
                    int maxLength = 16 - inputText.length();
                    if (maxLength > 0) {
                        if (filtered.length() > maxLength) {
                            filtered = filtered.substring(0, maxLength);
                        }
                        inputText.append(filtered);
                    }
                }
                return true;
            }

            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                String newAccount = inputText.toString().trim();
                if (!newAccount.isEmpty() && accounts.stream().noneMatch(a -> a.equalsIgnoreCase(newAccount))) {
                    isTyping = false;
                    accounts.add(newAccount);
                    Manager.ACCOUNT_MANAGER.addAccount(newAccount);
                    inputText.setLength(0);
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE && inputText.length() > 0) {
                inputText.deleteCharAt(inputText.length() - 1);
                return true;
            }
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }


    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (isTyping) {
            if (chr == '\n' || chr == '\r') return false;
            if (inputText.length() < 16 && (Character.isLetterOrDigit(chr) || chr == '_')) {
                inputText.append(chr);
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        if (parent != null) {
            mc.setScreen(parent);
        }
        super.close();
    }
    private void drawConfirmDialog(DrawContext drawContext) {
        int boxWidth = 300;
        int boxHeight = 130;
        int boxX = (width - boxWidth) / 2;
        int boxY = (height - boxHeight) / 2;

        RenderUtil.drawRoundedRect(drawContext.getMatrices(), 0, 0, width, height, 0, new Color(0, 0, 0, 120).getRGB());

        RenderUtil.drawRoundedRect(drawContext.getMatrices(), boxX, boxY, boxWidth, boxHeight, 6, new Color(40, 40, 40, 240).getRGB());

        FontUtils.sf_bold[22].centeredDraw(drawContext.getMatrices(), "Вы точно хотите очистить все аккаунты?", width / 2f, boxY + 30, Color.WHITE.getRGB());

        int btnWidth = 90;
        int btnHeight = 28;
        int yesX = boxX + 35;
        int noX = boxX + boxWidth - 35 - btnWidth;
        int btnY = boxY + boxHeight - 50;

        RenderUtil.drawRoundedRect(drawContext.getMatrices(), yesX, btnY, btnWidth, btnHeight, 5, new Color(60, 180, 75).getRGB());
        FontUtils.sf_medium[20].centeredDraw(drawContext.getMatrices(), "Да", yesX + btnWidth / 2f, btnY + btnHeight / 2f - 6, Color.WHITE.getRGB());

        RenderUtil.drawRoundedRect(drawContext.getMatrices(), noX, btnY, btnWidth, btnHeight, 5, new Color(200, 60, 60).getRGB());
        FontUtils.sf_medium[20].centeredDraw(drawContext.getMatrices(), "Нет", noX + btnWidth / 2f, btnY + btnHeight / 2f - 6, Color.WHITE.getRGB());
    }
}
