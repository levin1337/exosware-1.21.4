package ru.levin.screens.mainmenu;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;
import ru.levin.screens.altmanager.AltManager;
import ru.levin.util.color.ColorUtil;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;

import java.awt.*;

@SuppressWarnings("All")
public class MainMenu extends Screen {

    private Button singleplayerButton;
    private Button multiplayerButton;
    private Button altmanagerButton;
    private CombinedButton optionsQuitButton;

    private final String title = "ExosWare 1.21.4";

    private int shakeTime = 0;
    private float shakeOffsetY = 0f;

    public MainMenu() {
        super(Text.literal("Custom Main Menu"));
    }

    @Override
    protected void init() {
        int buttonWidth = 200;
        int buttonHeight = 30;

        singleplayerButton = new Button("Singleplayer", 0, 0, buttonWidth, buttonHeight);
        multiplayerButton = new Button("Multiplayer", 0, 0, buttonWidth, buttonHeight);
        altmanagerButton = new Button("AltManager", 0, 0, buttonWidth, buttonHeight);
        optionsQuitButton = new CombinedButton(0, 0, buttonWidth, buttonHeight, "Options", "Quit");
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
         RenderUtil.drawRoundedRect(context.getMatrices(), -1, -1, this.width + 2, this.height + 2, 4, new Color(27, 27, 27, 255).getRGB());
        if (shakeTime > 0) {
            shakeTime--;
            shakeOffsetY = (float)(Math.sin(shakeTime * 0.5) * 3);
        } else {
            shakeOffsetY = 0f;
        }

        int titleWidth = (int) FontUtils.sf_bold[54].getWidth(title);
        float titleX = (this.width - titleWidth) / 2f;
        float titleBaseY = this.height / 5f;
        float titleY = titleBaseY + shakeOffsetY;

        float time = (System.currentTimeMillis() % 4000L) / 1500f;
        FontUtils.sf_bold[54].renderAnimatedGradientText(context.getMatrices(), title, titleX, titleY, ColorUtil.getColorStyle(30), ColorUtil.getColorStyle(260), time);

        float titleHeight = 54;

        int spacing = 12;
        int buttonWidth = 200;
        int buttonHeight = 30;

        float buttonsStartY = titleBaseY + titleHeight + spacing * 2;

        int centerX = this.width / 2 - buttonWidth / 2;

        singleplayerButton.x = centerX;
        singleplayerButton.y = (int)buttonsStartY;

        multiplayerButton.x = centerX;
        multiplayerButton.y = (int)(buttonsStartY + buttonHeight + spacing);

        altmanagerButton.x = centerX;
        altmanagerButton.y = (int)(buttonsStartY + 2 * (buttonHeight + spacing));

        optionsQuitButton.x = centerX;
        optionsQuitButton.y = (int)(buttonsStartY + 3 * (buttonHeight + spacing));
        optionsQuitButton.width = buttonWidth;

        singleplayerButton.render(context, mouseX, mouseY, delta);
        multiplayerButton.render(context, mouseX, mouseY, delta);
        altmanagerButton.render(context, mouseX, mouseY, delta);
        optionsQuitButton.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int titleWidth = (int) FontUtils.sf_bold[54].getWidth(title);
        float titleX = (this.width - titleWidth) / 2f;
        float titleY = this.height / 5f;

        if (mouseX >= titleX && mouseX <= titleX + titleWidth && mouseY >= titleY && mouseY <= titleY + 54) {
            shakeTime = 20;
            return true;
        }

        if (singleplayerButton.isHovered(mouseX, mouseY)) {
            this.client.setScreen(new SelectWorldScreen(this));
            return true;
        }
        if (multiplayerButton.isHovered(mouseX, mouseY)) {
            this.client.setScreen(new MultiplayerScreen(this));
            return true;
        }
        if (altmanagerButton.isHovered(mouseX, mouseY)) {

            this.client.setScreen(new AltManager(this));
            return true;
        }
        if (optionsQuitButton.isOptionHovered(mouseX, mouseY)) {
            this.client.setScreen(new OptionsScreen(this, client.options));
            return true;
        }
        if (optionsQuitButton.isQuitHovered(mouseX, mouseY)) {
            this.client.scheduleStop();
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private class Button {
        final String name;
        int x, y, width, height;

        private float hoverAnim = 0f;
        private float scale = 1f;

        Button(String name, int x, int y, int width, int height) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean hovered = isHovered(mouseX, mouseY);

            float animSpeed = 0.04f;

            if (hovered) {
                hoverAnim = Math.min(1f, hoverAnim + animSpeed);
                scale = Math.min(1.02f, scale + animSpeed * 0.5f);
            } else {
                hoverAnim = Math.max(0f, hoverAnim - animSpeed);
                scale = Math.max(1f, scale - animSpeed * 0.5f);
            }

            int baseColor = new Color(35, 35, 35).getRGB();
            int hoverColor = new Color(55, 55, 55).getRGB();
            int bgColor = ColorUtil.blendColorsInt(baseColor, hoverColor, hoverAnim);

            int outlineColor = new Color(60, 60, 60, 180).getRGB();

            float textHeight = 20;
            float textY = y + (height - textHeight) / 2f + 3;

            context.getMatrices().push();
            context.getMatrices().translate(x + width / 2f, y + height / 2f, 0);
            context.getMatrices().scale(scale, scale, 1);
            context.getMatrices().translate(-(x + width / 2f), -(y + height / 2f), 0);

            RenderUtil.drawRoundedRect(context.getMatrices(), x, y, width, height, 4, bgColor);
            RenderUtil.drawRoundedBorder(context.getMatrices(), x, y, width, height, 4, 1f, outlineColor);
            FontUtils.sf_medium[20].centeredDraw(context.getMatrices(), name, x + width / 2f, textY, Color.WHITE.getRGB());

            context.getMatrices().pop();
        }

        boolean isHovered(double mouseX, double mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }

    private class CombinedButton {
        int x, y, width, height;
        final String leftName, rightName;

        private float leftHoverAnim = 0f;
        private float rightHoverAnim = 0f;
        private float leftScale = 1f;
        private float rightScale = 1f;

        CombinedButton(int x, int y, int width, int height, String leftName, String rightName) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.leftName = leftName;
            this.rightName = rightName;
        }

        void render(DrawContext context, int mouseX, int mouseY, float delta) {
            int buttonGap = 2;
            int halfWidth = width / 2;
            int shrink = 4;

            boolean leftHovered = isOptionHovered(mouseX, mouseY);
            boolean rightHovered = isQuitHovered(mouseX, mouseY);

            float animSpeed = 0.04f;

            if (leftHovered) {
                leftHoverAnim = Math.min(1f, leftHoverAnim + animSpeed);
                leftScale = Math.min(1.02f, leftScale + animSpeed * 0.5f);
            } else {
                leftHoverAnim = Math.max(0f, leftHoverAnim - animSpeed);
                leftScale = Math.max(1f, leftScale - animSpeed * 0.5f);
            }

            if (rightHovered) {
                rightHoverAnim = Math.min(1f, rightHoverAnim + animSpeed);
                rightScale = Math.min(1.02f, rightScale + animSpeed * 0.5f);
            } else {
                rightHoverAnim = Math.max(0f, rightHoverAnim - animSpeed);
                rightScale = Math.max(1f, rightScale - animSpeed * 0.5f);
            }

            int baseColor = new Color(35, 35, 35).getRGB();
            int hoverColor = new Color(55, 55, 55).getRGB();
            int outlineColor = new Color(60, 60, 60, 180).getRGB();

            int buttonWidth = halfWidth - shrink;

            int leftX = x + buttonGap;
            int leftBg = ColorUtil.blendColorsInt(baseColor, hoverColor, leftHoverAnim);
            context.getMatrices().push();
            context.getMatrices().translate(leftX + buttonWidth / 2f, y + height / 2f, 0);
            context.getMatrices().scale(leftScale, leftScale, 1);
            context.getMatrices().translate(-(leftX + buttonWidth / 2f), -(y + height / 2f), 0);
            RenderUtil.drawRoundedRect(context.getMatrices(), leftX, y, buttonWidth, height, 4, leftBg);
            RenderUtil.drawRoundedBorder(context.getMatrices(), leftX, y, buttonWidth, height, 4, 1f, outlineColor);
            FontUtils.sf_medium[20].centeredDraw(context.getMatrices(), leftName, leftX + buttonWidth / 2f, y + (height - 10) / 2.2f, Color.WHITE.getRGB());
            context.getMatrices().pop();

            int rightX = x + halfWidth + buttonGap;
            int rightBg = ColorUtil.blendColorsInt(baseColor, hoverColor, rightHoverAnim);
            context.getMatrices().push();
            context.getMatrices().translate(rightX + buttonWidth / 2f, y + height / 2f, 0);
            context.getMatrices().scale(rightScale, rightScale, 1);
            context.getMatrices().translate(-(rightX + buttonWidth / 2f), -(y + height / 2f), 0);
            RenderUtil.drawRoundedRect(context.getMatrices(), rightX, y, buttonWidth, height, 4, rightBg);
            RenderUtil.drawRoundedBorder(context.getMatrices(), rightX, y, buttonWidth, height, 4, 1f, outlineColor);
            FontUtils.sf_medium[20].centeredDraw(context.getMatrices(), rightName, rightX + buttonWidth / 2f, y + (height - 10) / 2.2f, Color.WHITE.getRGB());
            context.getMatrices().pop();
        }

        boolean isOptionHovered(double mouseX, double mouseY) {
            return mouseX >= x && mouseX < x + width / 2 - 1 && mouseY >= y && mouseY <= y + height;
        }

        boolean isQuitHovered(double mouseX, double mouseY) {
            return mouseX > x + width / 2 + 1 && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
