package ru.levin.manager.commandManager.impl;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import ru.levin.manager.IMinecraft;
import ru.levin.manager.ClientManager;
import ru.levin.manager.commandManager.Command;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static com.mojang.brigadier.Command.SINGLE_SUCCESS;

public class ParseCommand extends Command implements IMinecraft {

    private static final File PARSE_DIR = new File(mc.runDirectory, "files\\parser");

    private static final Map<String, String> PRIVILEGE_REPLACEMENTS = new HashMap<>();
    static {
        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔥ §7§7", "MODER");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔥ §7§7", "MODER");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔥ §7§7", "MODER");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔉ §7§7", "HELPER");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔉ §7§7", "HELPER");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔉ §7§7", "HELPER");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔁ §7§7", "MEDIA");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔁ §7§7", "MEDIA");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔁ §7§7", "MEDIA");


        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꕠ §7§7", "D.HELPER");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꕠ §7§7", "D.HELPER");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꕠ §7§7", "D.HELPER");

        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꕒ §7§7", "RABBIT");
        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꕒ §7§7", "RABBIT");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꕒ §7§7", "RABBIT");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꕄ §7§7", "DRACULA");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꕄ §7§7", "DRACULA");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꕄ §7§7", "DRACULA");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꕈ §7§7", "COBRA");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꕈ §7§7", "COBRA");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꕈ §7§7", "COBRA");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꕀ §7§7", "HYDRA");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꕀ §7§7", "HYDRA");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꕀ §7§7", "HYDRA");

        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꕖ §7§7", "BUNNY");
        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꕖ §7§7", "BUNNY");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꕖ §7§7", "BUNNY");

        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔶ §7§7", "TIGER");
        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔶ §7§7", "TIGER");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔶ §7§7", "TIGER");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔨ §7§7", "DRAGON");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔨ §7§7", "DRAGON");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔨ §7§7", "DRAGON");

        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔤ §7§7", "IMPERATOR");
        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔤ §7§7", "IMPERATOR");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔤ §7§7", "IMPERATOR");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔠ §7§7", "MAGISTER");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔠ §7§7", "MAGISTER");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔠ §7§7", "MAGISTER");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔖ §7§7", "OVERLORD");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔖ §7§7", "OVERLORD");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔖ §7§7", "OVERLORD");

        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔒ §7§7", "AVENGER");
        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔒ §7§7", "AVENGER");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔒ §7§7", "AVENGER");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔈ §7§7", "TITAN");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔈ §7§7", "TITAN");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔈ §7§7", "TITAN");

        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔄ §7§7", "HERO");
        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔄ §7§7", "HERO");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔄ §7§7", "HERO");

        PRIVILEGE_REPLACEMENTS.put("§c●§f §fꔀ §7§7", "PLAYER");
        PRIVILEGE_REPLACEMENTS.put("§a●§f §fꔀ §7§7", "PLAYER");
        PRIVILEGE_REPLACEMENTS.put("§6●§f §fꔀ §7§7", "PLAYER");

    }

    public ParseCommand() {
        super("parse");
    }

    @Override
    public void execute(LiteralArgumentBuilder<CommandSource> builder) {
        builder.then(literal("start").executes(context -> {
            parsePlayers();
            return SINGLE_SUCCESS;
        }));

        builder.then(literal("dir").executes(context -> {
            openDirectory();
            return SINGLE_SUCCESS;
        }));
    }

    private void parsePlayers() {
        if (!PARSE_DIR.exists() && !PARSE_DIR.mkdirs()) {
            ClientManager.message(Formatting.RED + "Ошибка создания директории!");
            return;
        }

        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        if (networkHandler == null) {
            ClientManager.message(Formatting.RED + "Вы не подключены к серверу!");
            return;
        }

        Collection<PlayerListEntry> playerEntries = networkHandler.getPlayerList();
        Map<String, List<String>> privilegeGroups = new LinkedHashMap<>();

        String serverIp = "unknown";
        if (mc.getCurrentServerEntry() != null) {
            serverIp = mc.getCurrentServerEntry().address;
        } else if (mc.world != null) {
            serverIp = "localhost";
        }

        File file = getUniqueFileName(serverIp);

        try (FileWriter fileWriter = new FileWriter(file)) {
            for (PlayerListEntry entry : playerEntries) {
                Team team = entry.getScoreboardTeam();
                if (team == null) continue;
                Text prefix = team.getPrefix();
                if (prefix == null) continue;

                String privilege = replaceChineseSymbols(prefix.getString());
                privilegeGroups.computeIfAbsent(privilege, k -> new ArrayList<>());
            }

            if (privilegeGroups.isEmpty()) {
                ClientManager.message(Formatting.YELLOW + "Привилегии в табе не найдены!");
                return;
            }

            for (PlayerListEntry entry : playerEntries) {
                Team team = entry.getScoreboardTeam();
                if (team == null) continue;
                Text prefix = team.getPrefix();
                if (prefix == null) continue;

                String privilege = replaceChineseSymbols(prefix.getString());
                String playerName = entry.getProfile().getName();

                privilegeGroups.getOrDefault(privilege, new ArrayList<>()).add(playerName);
            }

            for (Map.Entry<String, List<String>> group : privilegeGroups.entrySet()) {
                if (group.getValue().isEmpty()) continue;

                fileWriter.write("// Привилегия: " + group.getKey() + "\n\n");
                for (String player : group.getValue()) {
                    fileWriter.write(player + "\n");
                }
                fileWriter.write("\n");
            }

            ClientManager.message(Formatting.GREEN + "Успешно! Спаршено " +
                    privilegeGroups.values().stream().mapToInt(List::size).sum() + " игроков по " +
                    privilegeGroups.size() + " привилегиям");
            ClientManager.message(Formatting.GRAY + "Файл сохранен как: " + file.getName());
        } catch (Exception e) {
            ClientManager.message(Formatting.RED + "Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private File getUniqueFileName(String baseName) {
        File file = new File(PARSE_DIR, baseName + ".txt");
        if (!file.exists()) return file;

        int counter = 2;
        while (true) {
            file = new File(PARSE_DIR, baseName + "_" + counter + ".txt");
            if (!file.exists()) return file;
            counter++;
        }
    }

    private String replaceChineseSymbols(String privilege) {
        for (Map.Entry<String, String> entry : PRIVILEGE_REPLACEMENTS.entrySet()) {
            if (privilege.contains(entry.getKey())) return entry.getValue();
        }
        return privilege;
    }

    private void openDirectory() {
        try {
            Runtime.getRuntime().exec("explorer \"" + PARSE_DIR.getAbsolutePath() + "\"");
        } catch (IOException e) {
            ClientManager.message(Formatting.RED + "Ошибка открытия директории: " + e.getMessage());
        }
    }
}
