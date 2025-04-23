package net.nxtresources;

import net.nxtresources.commands.CMDHandler;
import net.nxtresources.listeners.InteractEvent;
import net.nxtresources.listeners.ItemDropEvent;
import net.nxtresources.listeners.JoinEvent;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.LocationManager;
import net.nxtresources.managers.SetupManager;
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
    public static LocationManager locationManager;
    public static SetupManager setupManager;

    public static FileConfiguration arenaConfig;
    public static File arenaFile;

    public static FileConfiguration messagesConfig;
    public static File messagesFile;

    public static FileConfiguration lobbyConfig;
    public static File lobbyFile;

    @Override
    public void onEnable() {
        plugin =this;
        initialize();
        loadFiles();
        registerCommands();
        registerListeners();
        ArenaMgr.mkCache();
        ArenaMgr.loadAllArenas();
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initialize() {
        locationManager = new LocationManager();
        setupManager = new SetupManager();

    }

    private void loadFiles() {
        loadConfig("arenas.yml", config -> arenaConfig = config);
        loadConfig("messages.yml", config -> messagesConfig = config);
        loadConfig("lobby.yml", config -> lobbyConfig = config);
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
            Bukkit.getLogger().log(Level.INFO, String.valueOf(e));
        }
    }

    public static void saveMessagesConfig() {
        try {
            messagesConfig.save(messagesFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.INFO, String.valueOf(e));
        }
    }

    public static void saveLobbyConfig() {
        try{
            lobbyConfig.save(lobbyFile);
        } catch (IOException e) {
            Bukkit.getLogger().log(Level.INFO, String.valueOf(e));
        }
    }

    private void registerCommands() {
        Objects.requireNonNull(getCommand("sheepwars")).setExecutor(new CMDHandler());

    }
    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new JoinEvent(), this);
        pm.registerEvents(new ItemDropEvent(), this);
        pm.registerEvents(new InteractEvent(), this);

    }

    public static Main getInstance() {
        return plugin;
    }
}
