package ru.levin.modules.render;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.joml.Vector3d;
import ru.levin.modules.setting.BooleanSetting;
import ru.levin.modules.setting.MultiSetting;
import ru.levin.events.Event;
import ru.levin.events.impl.render.EventRender2D;
import ru.levin.manager.Manager;
import ru.levin.modules.Function;
import ru.levin.modules.FunctionAnnotation;
import ru.levin.modules.Type;
import ru.levin.manager.fontManager.FontUtils;
import ru.levin.util.render.RenderAddon;
import ru.levin.util.render.RenderUtil;
import ru.levin.util.render.providers.ResourceProvider;
import ru.levin.util.vector.EntityPosition;
import ru.levin.util.vector.VectorUtil;

import java.awt.*;
import java.util.*;
import java.util.List;

@FunctionAnnotation(name = "NameTags", desc = "Нейм таги", type = Type.Render)
public class NameTags extends Function {

    public final MultiSetting tags = new MultiSetting("Энтити", Arrays.asList("Игроки"), new String[]{"Игроки", "Предметы на земле"});
    private final BooleanSetting armorRender = new BooleanSetting("Показывать предметы", true, () -> tags.get("Игроки"));
    private final BooleanSetting effectRender = new BooleanSetting("Показывать эффекты", true, () -> tags.get("Игроки"));
    private final BooleanSetting enchantRender = new BooleanSetting("Показывать чары", true, () -> tags.get("Игроки"));
    private final BooleanSetting sphereRender = new BooleanSetting("Показывать Шары/Талисманы", true, () -> tags.get("Игроки"));
    private final BooleanSetting shulkerCheck = new BooleanSetting("Показывать содержимое шалкеров", true, () -> tags.get("Предметы на земле"));
    private static final int BG_COLOR = new Color(30, 30, 30, 150).getRGB();

    public NameTags() {
        addSettings(tags, armorRender, effectRender,enchantRender, sphereRender,shulkerCheck);
    }

    @Override
    public void onEvent(Event event) {
        if (event instanceof EventRender2D e) {
            if (tags.get("Игроки")) renderPlayers(e);
            if (tags.get("Предметы на земле")) renderItems(e);
        }
    }

    private void renderPlayers(EventRender2D e) {
        final int screenW = mc.getWindow().getScaledWidth();
        final int screenH = mc.getWindow().getScaledHeight();
        final float tickDelta = e.getDeltatick().getTickDelta(true);
        MatrixStack matrixStack = e.getMatrixStack();

        for (PlayerEntity player : Manager.SYNC_MANAGER.getPlayers()) {
            if (player == null || (player instanceof ClientPlayerEntity && mc.options.getPerspective().isFirstPerson()))
                continue;

            Vector3d vec = VectorUtil.toScreen(EntityPosition.get(player, 2.0f, tickDelta));
            if (vec.z < 0 || vec.x < 0 || vec.x > screenW || vec.y < 0 || vec.y > screenH) continue;

            boolean isClientUser = Manager.FUNCTION_MANAGER.globals.isClientUserCache.containsKey(player.getUuid());

            String friendPrefix = Manager.FRIEND_MANAGER.isFriend(player.getName().getString()) ? Formatting.GRAY + "[" + Formatting.GREEN + "F" + Formatting.GRAY + "] " : "";

            float health = player.getHealth() + player.getAbsorptionAmount();
            String hpText = Formatting.GRAY + " [" + (health < 300 ? Formatting.RED.toString() + (int) health : Formatting.RED + "Unknown") + Formatting.GRAY + "]" + Formatting.RESET;

            String name = Manager.FUNCTION_MANAGER.nameProtect.getProtectedName(player.getGameProfile().getName());
            Text prefix = player.getScoreboardTeam() != null ? player.getScoreboardTeam().getPrefix() : Text.literal("");
            Text itemText = null;

            if (sphereRender.get()) {
                ItemStack offHand = player.getOffHandStack();
                if (!offHand.isEmpty() && (offHand.getItem() == Items.TOTEM_OF_UNDYING || offHand.getItem() instanceof PlayerHeadItem)) {
                    Text customName = offHand.getCustomName();
                    if (customName != null) {
                        itemText = customName;
                    }
                }
            }

            float iconWidth = isClientUser ? 12f : 0f;
            float friendWidth = mc.textRenderer.getWidth(Text.literal(friendPrefix)) * 0.7f;
            float prefixWidth = mc.textRenderer.getWidth(prefix) * 0.7f;
            float nameHpWidth = FontUtils.durman[13].getWidth(name + hpText);
            float itemWidth = itemText != null ? mc.textRenderer.getWidth(itemText) * 0.7f + 3f : 0f;

            float totalWidth = iconWidth + friendWidth + prefixWidth + nameHpWidth + itemWidth;

            float x = (float) vec.x - (totalWidth + 8f) / 2f;
            float y = (float) vec.y - 14f - 1f;

            RenderUtil.drawRoundedRect(matrixStack, x, y, totalWidth + 8f, 12f, 1.5f, BG_COLOR);

            MatrixStack matrices = e.getDrawContext().getMatrices();
            matrices.push();
            matrices.translate(x + 4f, y + 3.2f, 0);
            matrices.scale(0.7f, 0.7f, 1.0f);

            int dx = 0;

            if (isClientUser) {
                RenderUtil.drawTexture(matrices, "images/hud/tags.png", dx, -2, 12, 12, 0, Color.white.getRGB());
                dx += iconWidth / 0.7f + 2;
            }

            if (!friendPrefix.isEmpty()) {
                mc.textRenderer.draw(Text.literal(friendPrefix), dx, 0, -1, false, matrices.peek().getPositionMatrix(), mc.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                dx += friendWidth / 0.7f;
            }

            mc.textRenderer.draw(prefix, dx, 0, -1, false, matrices.peek().getPositionMatrix(), mc.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
            dx += prefixWidth / 0.7f;

            matrices.pop();

            FontUtils.durman[13].drawLeftAligned(matrixStack, name + hpText, x + 4f + iconWidth + friendWidth + prefixWidth, y + 1.8f, -1);

            if (itemText != null) {
                matrices.push();
                matrices.translate(x + 4f + iconWidth + friendWidth + prefixWidth + nameHpWidth + 3f, y + 3.5f, 0);
                matrices.scale(0.7f, 0.7f, 1.0f);
                mc.textRenderer.draw(itemText, 0, 0, -1, false, matrices.peek().getPositionMatrix(), mc.getBufferBuilders().getEntityVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 0xF000F0);
                matrices.pop();
            }

            if (effectRender.get()) renderEffect(e, player);
            if (armorRender.get()) renderPlayerItems(e, x + 5f, y, player);
        }
    }


    private void renderEffect(EventRender2D e, PlayerEntity player) {
        Vector3d footPos = VectorUtil.toScreen(EntityPosition.get(player, 0.0f, e.getDeltatick().getTickDelta(true)));
        if (footPos.z < 0) return;

        int offsetY = 5;
        for (StatusEffectInstance effect : player.getStatusEffects()) {
            String name = I18n.translate(effect.getEffectType().value().getName().getString());
            int lvl = effect.getAmplifier() + 1;
            int sec = effect.getDuration() / 20;
            String text = Formatting.WHITE + name + (lvl > 1 ? " " + lvl : "") + Formatting.WHITE + " | " + sec / 60 + ":" + String.format("%02d", sec % 60);

            FontUtils.durman[14].centeredDraw(e.getDrawContext().getMatrices(), text, (float) footPos.x, (float) footPos.y + offsetY, Color.white.getRGB());
            offsetY += 9;
        }
    }

    private static final Set<String> IMPORTANT_ENCHANTS = Set.of(
            "Protection", "Защита",
            "Unbreaking", "Прочность",
            "Looting", "Добыча",
            "Fortune", "Удача",
            "Efficiency", "Эффективность",
            "Power", "Сила",
            "Feather Falling", "Невесомость",
            "Thorns", "Шипы",
            "Silk Touch", "Шёлковое касание",
            "Respiration", "Подводное дыхание",
            "Mending", "Починка",
            "Knockback", "Отдача",
            "Curse of Vanishing", "Проклятие утраты"
    );

    private void renderPlayerItems(EventRender2D e, float x, float y, PlayerEntity player) {
        List<ItemStack> stacks = new ArrayList<>(6);
        stacks.add(player.getMainHandStack());
        player.getArmorItems().forEach(stacks::add);
        stacks.add(player.getOffHandStack());
        stacks.removeIf(i -> i.isEmpty() || i.getItem() instanceof AirBlockItem);

        float offset = 0;

        for (ItemStack stack : stacks) {
            RenderAddon.renderItem(e.getDrawContext(), stack, x + offset - 3f, y - 18f, 0.8f, true);

            if (enchantRender.get() && !stack.getEnchantments().isEmpty()) {
                List<Object2IntMap.Entry<RegistryEntry<Enchantment>>> enchantments = new ArrayList<>(stack.getEnchantments().getEnchantmentEntries());

                enchantments.removeIf(entry -> {
                    Text name = Enchantment.getName(entry.getKey(), entry.getIntValue());
                    String full = name.getString();
                    return IMPORTANT_ENCHANTS.stream().noneMatch(full::contains);
                });

                if (!enchantments.isEmpty()) {
                    int totalHeight = enchantments.size() * 8;
                    int startY = (int) (y - 18f - totalHeight);

                    MatrixStack matrices = e.getMatrixStack();
                    for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : enchantments) {
                        RegistryEntry<Enchantment> regEntry = entry.getKey();
                        int level = entry.getIntValue();
                        Text enchantText = Enchantment.getName(regEntry, level);

                        String display = getShortName(enchantText, level);

                        matrices.push();
                        matrices.translate(x + offset, startY, 0);
                        matrices.scale(0.7f, 0.7f, 1.0f);
                        FontUtils.durman[14].drawLeftAligned(e.getDrawContext().getMatrices(),
                                display, 0, 0, Color.white.getRGB());
                        matrices.pop();

                        startY += 8;
                    }
                }
            }

            offset += 15f;
        }
    }

    /**
     * Автоматическое сокращение чар
     */
    private String getShortName(Text description, int level) {
        String full = description.getString();
        String[] words = full.split(" ");
        String shortName;

        if (words.length == 1) {
            shortName = words[0].substring(0, Math.min(2, words[0].length())).toUpperCase();
        } else {
            shortName = "";
            for (String w : words) {
                if (!w.isEmpty()) shortName += w.charAt(0);
            }
            shortName = shortName.toUpperCase();
        }

        return shortName + " " + level;
    }

    private void renderItems(EventRender2D e) {
        final float tickDelta = e.getDeltatick().getTickDelta(true);
        final int screenW = mc.getWindow().getScaledWidth();
        final int screenH = mc.getWindow().getScaledHeight();

        for (Entity entity : Manager.SYNC_MANAGER.getEntities()) {
            if (!(entity instanceof ItemEntity itemEntity)) continue;

            ItemStack stack = itemEntity.getStack();
            Item item = stack.getItem();

            if (item instanceof BlockItem bi && bi.getBlock() instanceof ShulkerBoxBlock) {
                Vector3d vec = VectorUtil.toScreen(EntityPosition.get(entity, 0.6f, tickDelta));
                if (vec.z < 0 || vec.x < 0 || vec.x > screenW || vec.y < 0 || vec.y > screenH) continue;

                int offsetX = (int) vec.x;
                int offsetY = (int) vec.y;
                renderShulkerToolTip(e.getDrawContext(), offsetX, offsetY, stack);
            }

            Vector3d vec = VectorUtil.toScreen(EntityPosition.get(entity, 0.6f, tickDelta));
            if (vec.z < 0 || vec.x < 0 || vec.x > screenW || vec.y < 0 || vec.y > screenH) continue;

            String name = itemEntity.getName().getString();
            int count = stack.getCount();
            if (count > 1) name += " [x" + Formatting.RED + count + Formatting.WHITE +"]";

            float width = FontUtils.sf_bold[15].getWidth(name);
            float height = FontUtils.sf_bold[15].getHeight();
            float x = (float) vec.x - width / 2f - 3f;
            float y = (float) vec.y;

            RenderUtil.drawRoundedRect(e.getMatrixStack(), x, y, width + 6f, height + 2f, 1.5f, BG_COLOR);
            FontUtils.sf_bold[15].centeredDraw(e.getDrawContext().getMatrices(), name, (float) vec.x, y + 0.3f, Color.WHITE.getRGB());
        }
    }

    public boolean renderShulkerToolTip(DrawContext context, int offsetX, int offsetY, ItemStack stack) {
        ContainerComponent compoundTag = stack.get(DataComponentTypes.CONTAINER);
        if (compoundTag == null || compoundTag.copyFirstStack().isEmpty()) return false;
        draw(context, compoundTag.stream().toList(), offsetX, offsetY);
        return true;
    }

    private void draw(DrawContext context, List<ItemStack> itemStacks, int offsetX, int offsetY) {
        final int columns = 9;
        final int rows = 3;
        final float itemSize = 16f;
        final float spacing = 0.1f;
        final int paddingX = 8;
        final int paddingY = 7;

        offsetX += paddingX;
        offsetY -= 82 - paddingY;

        int bgWidth = (int) (columns * itemSize + (columns - 1) * spacing + paddingX * 2);
        int bgHeight = (int) (rows * itemSize + (rows - 1) * spacing + paddingY * 2);
        RenderUtil.drawTexture(context.getMatrices(), ResourceProvider.container, offsetX - paddingX, offsetY - paddingY, bgWidth, bgHeight, 0, -1);
        for (int index = 0; index < itemStacks.size(); index++) {
            int row = index / columns;
            int col = index % columns;

            float x = offsetX + col * (itemSize + spacing);
            float y = offsetY + row * (itemSize + spacing);

            RenderAddon.renderItem(context, itemStacks.get(index), x, y, 0.85f, true);
        }
    }
}
