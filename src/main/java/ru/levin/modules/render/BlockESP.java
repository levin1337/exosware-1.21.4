package ru.levin.modules.render;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import ru.levin.events.Event;
import ru.levin.events.impl.render.EventRender3D;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.SliderSetting;
import ru.levin.util.render.RenderUtil;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@FunctionAnnotation(name = "BlockESP", desc = "Показывает сундуки, печки, спавнеры и др.", type = Type.Render)
public class BlockESP extends Function {

    private final SliderSetting radius = new SliderSetting("Радиус", 20f, 1f, 30f, 1f);
    private final BooleanSetting chest = new BooleanSetting("Сундуки", true);
    private final BooleanSetting furnace = new BooleanSetting("Печки", true);
    private final BooleanSetting spawner = new BooleanSetting("Спавнеры", true);
    private final BooleanSetting brewingStand = new BooleanSetting("Варочные", true);
    private final BooleanSetting enderChest = new BooleanSetting("Эндер сундуки", true);
    private final BooleanSetting detectorRail = new BooleanSetting("Детектор рельс", true);

    public BlockESP() {
        addSettings(radius, chest, furnace, spawner, brewingStand, enderChest, detectorRail);
    }

    private final Map<String, Color> customBlocks = new HashMap<>();

    public void addCustomBlock(String blockId, Color color) {
        Block block = Registries.BLOCK.get(Identifier.of(blockId));
        if (block != Blocks.AIR) {
            customBlocks.put(blockId, color);
        }
    }

    public void removeCustomBlock(String blockId) {
        customBlocks.remove(blockId);
    }

    public Map<String, Color> getCustomBlocks() {
        return customBlocks;
    }

    @Override
    public void onEvent(Event event) {
        if (!(event instanceof EventRender3D)) return;

        int r = radius.get().intValue();
        BlockPos playerPos = mc.player.getBlockPos();

        for (int x = playerPos.getX() - r; x <= playerPos.getX() + r; x++) {
            for (int y = playerPos.getY() - r; y <= playerPos.getY() + r; y++) {
                for (int z = playerPos.getZ() - r; z <= playerPos.getZ() + r; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = mc.world.getBlockState(pos);
                    Box box = new Box(pos).contract(0.01);

                    if (chest.get() && (state.getBlock() == Blocks.CHEST || state.getBlock() == Blocks.TRAPPED_CHEST)) {
                        RenderUtil.render3D.drawHoleOutline(box, new Color(139, 69, 19, 150).getRGB(), 2);
                    } else if (furnace.get() && (state.getBlock() == Blocks.FURNACE || state.getBlock() == Blocks.BLAST_FURNACE || state.getBlock() == Blocks.SMOKER)) {
                        RenderUtil.render3D.drawHoleOutline(box, new Color(128, 128, 128, 150).getRGB(), 2);
                    } else if (spawner.get() && state.getBlock() == Blocks.SPAWNER) {
                        RenderUtil.render3D.drawHoleOutline(box, new Color(255, 0, 255, 150).getRGB(), 2);
                    } else if (brewingStand.get() && state.getBlock() == Blocks.BREWING_STAND) {
                        RenderUtil.render3D.drawHoleOutline(box, new Color(0, 191, 255, 150).getRGB(), 2);
                    } else if (enderChest.get() && state.getBlock() == Blocks.ENDER_CHEST) {
                        RenderUtil.render3D.drawHoleOutline(box, new Color(75, 0, 130, 150).getRGB(), 2);
                    } else if (detectorRail.get() && state.getBlock() == Blocks.DETECTOR_RAIL) {
                        RenderUtil.render3D.drawHoleOutline(box, new Color(255, 165, 0, 150).getRGB(), 2);
                    } else {
                        String id = Registries.BLOCK.getId(state.getBlock()).toString();
                        Color c = customBlocks.get(id);
                        if (c != null) {
                            RenderUtil.render3D.drawHoleOutline(box, c.getRGB(), 2);
                        }
                    }
                }
            }
        }
    }
}
