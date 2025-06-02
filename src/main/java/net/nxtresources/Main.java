package net.nxtresources;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.commands.CMDHandler;
import net.nxtresources.listeners.*;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.DataMgr;
import net.nxtresources.managers.MsgCache;
import net.nxtresources.managers.SetupMgr;
import net.nxtresources.menus.ArenaSelectorGui;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.logging.Level;

public final class Main extends JavaPlugin {

    private static Main plugin;
    public static SetupMgr setupManager;

    public static FileConfiguration arenaConfig;
    public static File arenaFile;

    public static FileConfiguration messagesConfig;
    public static File messagesFile;

    public static FileConfiguration lobbyConfig;
    public static File lobbyFile;

    public static FileConfiguration dataConfig;
    public static File dataFile;

    public static final Gson gson =new Gson();
    public static NamespacedKey shKey;

    @Override
    public void onEnable() {
        plugin =this;
        shKey = new NamespacedKey(plugin, "SheepData");
        initialize();
        loadFiles();
        registerCommands();
        registerListeners();
        ArenaMgr.loadAllArenas();
        DataMgr.load();
        SetupMgr.loadMainLobby();
        MsgCache.load();
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initialize() {
        setupManager = new SetupMgr();

    }

    private void loadFiles() {
        arenaConfig = loadConfig("arenas.yml");
        messagesConfig = loadConfig("messages.yml");
        lobbyConfig = loadConfig("lobby.yml");
        dataConfig = loadConfig("data.yml");
    }

    private YamlConfiguration loadConfig(String fileName) {
        File file = new File(getDataFolder(), fileName);
        if (!file.exists()) {
            saveResource(fileName, false);
            getLogger().log(Level.INFO, fileName + " created!");
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        switch (fileName) {
            case "lobby.yml" -> lobbyFile = file;
            case "arenas.yml" -> arenaFile = file;
            case "messages.yml" -> messagesFile = file;
            case "data.yml" -> dataFile = file;
        }
        getLogger().log(Level.INFO, fileName + " loaded!");
        return config;
    }

    public void reload() {
        saveConfig();
        reloadConfig();
        saveMessagesConfig();
        loadFiles();
        MsgCache.load();


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
        pm.registerEvents(new ArenaSelectorGui(), this);
        pm.registerEvents(new ExplodeEvent(), this);

    }

    public static Main getInstance() {
        return plugin;
    }

    public static Component color(String str) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(str);
    }
}
