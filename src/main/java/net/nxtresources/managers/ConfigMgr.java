package net.nxtresources.managers;

import net.nxtresources.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public record ConfigMgr(Main plugin) {

    public static FileConfiguration arenaConfig, messagesConfig, lobbyConfig, dataConfig;
    public static File arenaFile, messagesFile, lobbyFile, dataFile;

    public void loadFiles() {
        arenaConfig = loadConfig("arenas.yml");
        messagesConfig = loadConfig("messages.yml");
        lobbyConfig = loadConfig("lobby.yml");
        dataConfig = loadConfig("data.yml");
    }

    private YamlConfiguration loadConfig(String fileName) {
        File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            plugin.saveResource(fileName, false);
            plugin.getLogger().log(Level.INFO, fileName + " created!");
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        switch (fileName) {
            case "lobby.yml" -> lobbyFile = file;
            case "arenas.yml" -> arenaFile = file;
            case "messages.yml" -> messagesFile = file;
            case "data.yml" -> dataFile = file;
        }
        plugin.getLogger().log(Level.INFO, fileName + " loaded!");
        return config;
    }

    public void saveAllConfig() {
        try {
            arenaConfig.save(arenaFile);
            messagesConfig.save(messagesFile);
            lobbyConfig.save(lobbyFile);
            dataConfig.save(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
