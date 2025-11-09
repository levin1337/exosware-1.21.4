package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector2f;
import ru.levin.manager.ClientManager;
import ru.levin.manager.commandManager.Command;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;

import java.awt.*;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

@SuppressWarnings("ALL")
public class GpsCommand extends Command {

    private static boolean enabled;
    private static BlockPos GPS_POSITION;
    private static float smoothYaw = 0f;

    public GpsCommand() {
        super("gps");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {

        RequiredArgumentBuilder<CommandSource, Integer> xArg = arg("x", IntegerArgumentType.integer());
        RequiredArgumentBuilder<CommandSource, Integer> zArg = arg("z", IntegerArgumentType.integer());
        zArg.executes(context -> {
            int x = IntegerArgumentType.getInteger(context, "x");
            int z = IntegerArgumentType.getInteger(context, "z");
            setGps(x, z);
            return SINGLE_SUCCESS;
        });
        xArg.then(zArg);
        builder.then(xArg);

        builder.then(literal("set")
                .then(arg("x", IntegerArgumentType.integer())
                        .then(arg("z", IntegerArgumentType.integer())
                                .executes(context -> {
                                    int x = IntegerArgumentType.getInteger(context, "x");
                                    int z = IntegerArgumentType.getInteger(context, "z");
                                    setGps(x, z);
                                    return SINGLE_SUCCESS;
                                }))));

        builder.then(literal("off").executes(context -> {
            enabled = false;
            GPS_POSITION = null;
            ClientManager.message(Formatting.GRAY + "GPS выключен!");
            return SINGLE_SUCCESS;
        }));
    }

    private static void setGps(int x, int z) {
        GPS_POSITION = new BlockPos(x, (int) mc.player.getY(), z);
        enabled = true;
        ClientManager.message(Formatting.GRAY + "GPS на координаты X: " + x + ", Y: " + mc.player.getY() + ", Z: " + z);
    }

    private static int getDistance(BlockPos pos) {
        double dx = mc.player.getX() - pos.getX();
        double dz = mc.player.getZ() - pos.getZ();
        return (int) MathHelper.sqrt((float) (dx * dx + dz * dz));
    }

    private static float getYaw(Vector2f target) {
        double dx = target.x - mc.player.getX();
        double dz = target.y - mc.player.getZ();
        return (float) -(Math.atan2(dx, dz) * (180 / Math.PI));
    }

    public static void render(MatrixStack matrixStack) {
        if (!enabled || GPS_POSITION == null || mc.player == null) return;

        float screenWidth = mc.getWindow().getScaledWidth() / 2f;
        float screenHeight = mc.getWindow().getScaledHeight() / 2f;

        float targetYaw = getYaw(new Vector2f(GPS_POSITION.getX(), GPS_POSITION.getZ())) - mc.player.getYaw();
        targetYaw = MathHelper.wrapDegrees(targetYaw);

        float maxStep = 4.0f;
        float delta = MathHelper.wrapDegrees(targetYaw - smoothYaw);
        smoothYaw += MathHelper.clamp(delta, -maxStep, maxStep);

        int distance = getDistance(GPS_POSITION);
        if (distance < 1) {
            FontUtils.durman[15].centeredDraw(matrixStack, "Вы на месте!", screenWidth, screenHeight - 105, -1);
            return;
        }

        matrixStack.push();
        matrixStack.translate(screenWidth, screenHeight - 120, 0);
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(smoothYaw));
        RenderUtil.drawTexture(matrixStack, "images/triangle2.png", -12, -12, 25, 25, 0, Color.white.getRGB());
        matrixStack.pop();

        FontUtils.durman[15].centeredDraw(matrixStack, distance + "m", screenWidth, screenHeight - 105, -1);
    }
}
