package net.nxtresources;

import net.nxtresources.commands.CMDHandler;
import net.nxtresources.listeners.DeathEvent;
import net.nxtresources.listeners.InteractEvent;
import net.nxtresources.listeners.ItemDropEvent;
import net.nxtresources.listeners.JoinAndQuitEvent;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.DataManager;
import net.nxtresources.managers.LocationManager;
import net.nxtresources.managers.Setup;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    private static Main plugin;
    public static Setup setupManager;

    public static FileConfiguration arenaConfig;
    public static File arenaFile;

    public static FileConfiguration messagesConfig;
    public static File messagesFile;

    public static FileConfiguration lobbyConfig;
    public static File lobbyFile;

    public static FileConfiguration dataConfig;
    public static File dataFile;

    @Override
    public void onEnable() {
        plugin =this;
        initialize();
        loadFiles();
        registerCommands();
        registerListeners();
        ArenaMgr.mkCache();
        ArenaMgr.loadAllArenas();
        DataManager.load();
        Setup.loadMainLobby();
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initialize() {
        setupManager = new Setup();

    }

    private void loadFiles() {
        loadConfig("arenas.yml", config -> arenaConfig = config);
        loadConfig("messages.yml", config -> messagesConfig = config);
        loadConfig("lobby.yml", config -> lobbyConfig = config);
        loadConfig("data.yml", config -> dataConfig = config);
    }

    private void loadConfig(String fileName, Consumer<YamlConfiguration> configSetter) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
            getLogger().log(Level.INFO, fileName + " created!");
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        configSetter.accept(config);
        switch (fileName) {
            case "lobby.yml" -> lobbyFile = file;
            case "arenas.yml" -> arenaFile = file;
            case "messages.yml" -> messagesFile = file;
            case "data.yml" -> dataFile = file;
        }
        getLogger().log(Level.INFO, fileName + " loaded!");
    }

    public void reload() {
        saveConfig();
        reloadConfig();
        loadFiles();

    }

    public static void saveArenaConfig() {
        try {
            arenaConfig.save(arenaFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveMessagesConfig() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void saveLobbyConfig() {
        try{
            lobbyConfig.save(lobbyFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void saveDataConfig(){
        try{
            dataConfig.save(dataFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("sheepwars")).setExecutor(new CMDHandler());

    }
    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new JoinAndQuitEvent(), this);
        pm.registerEvents(new ItemDropEvent(), this);
        pm.registerEvents(new InteractEvent(), this);
        pm.registerEvents(new DeathEvent(), this);

    }

    public static Main getInstance() {
        return plugin;
    }
}
