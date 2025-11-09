package ru.levin.manager.commandManager.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3d;
import ru.levin.manager.ClientManager;
import ru.levin.manager.commandManager.Command;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.vector.VectorUtil;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class WayPointCommand extends Command {

    private final Path FILE = Paths.get(mc.runDirectory.getAbsolutePath(), "files\\modules", "waypoints.ew");
    private final Gson GSON = new Gson();
    private final Type TYPE = new TypeToken<Map<String, BlockPos>>() {}.getType();
    private static Map<String, BlockPos> waypoints = new ConcurrentHashMap<>();

    public WayPointCommand() {
        super("way");
        load();
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("add")
                .then(arg("name", StringArgumentType.word())
                        .executes(ctx -> {
                            BlockPos pos = mc.player.getBlockPos();
                            String name = StringArgumentType.getString(ctx, "name");
                            waypoints.put(name, pos);
                            save();
                            ClientManager.message("§aТочка §f" + name + " §aсохранена: " + pos.toShortString());
                            return SINGLE_SUCCESS;
                        })
                        .then(arg("x", IntegerArgumentType.integer())
                                .then(arg("y", IntegerArgumentType.integer())
                                        .then(arg("z", IntegerArgumentType.integer())
                                                .executes(ctx -> {
                                                    String name = StringArgumentType.getString(ctx, "name");
                                                    int x = IntegerArgumentType.getInteger(ctx, "x");
                                                    int y = IntegerArgumentType.getInteger(ctx, "y");
                                                    int z = IntegerArgumentType.getInteger(ctx, "z");
                                                    BlockPos pos = new BlockPos(x, y, z);
                                                    waypoints.put(name, pos);
                                                    save();
                                                    ClientManager.message("§aТочка §f" + name + " §aсохранена: " + pos.toShortString());
                                                    return SINGLE_SUCCESS;
                                                }))))));

        builder.then(literal("remove")
                .then(arg("name", StringArgumentType.word())
                        .suggests((ctx, suggestionsBuilder) -> {
                            waypoints.keySet().forEach(suggestionsBuilder::suggest);
                            return suggestionsBuilder.buildFuture();
                        })
                        .executes(ctx -> {
                            String name = StringArgumentType.getString(ctx, "name");
                            if (waypoints.remove(name) != null) {
                                save();
                                ClientManager.message("§aТочка §f" + name + " §aудалена!");
                            } else {
                                ClientManager.message("§cТакой точки нет!");
                            }
                            return SINGLE_SUCCESS;
                        })));


        builder.then(literal("list")
                .executes(ctx -> {
                    if (waypoints.isEmpty()) {
                        ClientManager.message("Нет сохранённых точек.");
                    } else {
                        ClientManager.message("Список точек:");
                        waypoints.forEach((n, pos) -> ClientManager.message("§f" + n + " §7→ " + pos.toShortString()));
                    }
                    return SINGLE_SUCCESS;
                }));
    }

    public static void render(MatrixStack matrices) {
        if (waypoints.isEmpty()) return;

        Camera camera = mc.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();
        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        waypoints.forEach((name, pos) -> {
            Vector3d sp = VectorUtil.toScreen(pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5);
            if (sp.z() <= 0 || sp.x() < 0 || sp.x() > sw || sp.y() < 0 || sp.y() > sh) return;

            int dist = (int) camPos.distanceTo(Vec3d.of(pos));
            String text = name + " [" + dist + "m]";
            float tw = FontUtils.durman[15].getWidth(text);
            float th = FontUtils.durman[15].getHeight();

            float px = 4f, py = 1.5f;
            RenderUtil.drawRoundedRect(matrices, (float) sp.x() - tw / 2f - px, (float) sp.y() - th / 2f - py, tw + px * 2, th + py * 2, 1.5f, new Color(30, 30, 30, 150).getRGB());
            FontUtils.durman[15].centeredDraw(matrices, text, (float) sp.x(), (float) sp.y() - th / 2f - 0.5f, Color.WHITE.getRGB());
        });
    }

    private void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(waypoints, TYPE), StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load() {
        if (!Files.exists(FILE)) return;
        try {
            String json = Files.readString(FILE, StandardCharsets.UTF_8);
            waypoints = GSON.fromJson(json, TYPE);
            if (waypoints == null) waypoints = new ConcurrentHashMap<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
