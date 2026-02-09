package net.nxtresources;

import com.google.gson.Gson;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.commands.CMDHandler;
import net.nxtresources.listeners.*;
import net.nxtresources.managers.*;
import net.nxtresources.menus.ArenaSelectorGui;
import net.nxtresources.utils.MsgCache;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {

    private static Main plugin;
    public static SetupMgr setupManager;
    private ConfigMgr configMgr;

    public static final Gson gson =new Gson();
    public static NamespacedKey shKey, sheepKickupKey, itemData;

    @Override
    public void onEnable() {
        plugin =this;
        shKey = new NamespacedKey(plugin, "SheepData");
        sheepKickupKey = new NamespacedKey(plugin, "PickupSheepData");
        itemData = new NamespacedKey(plugin, "ItemData");
        initialize();
        configMgr.loadFiles();
        registerCommands();
        registerListeners();
        ArenaMgr.loadAllArenas();
        DataMgr.load();
        //SetupMgr.loadMainLobby();
        LobbyMgr.loadMainLobby();
        MsgCache.load();
        // Plugin startup logic
    }

    @Override
    public void onDisable() {
        saveConfig();
        configMgr.saveAllConfig();
        // Plugin shutdown logic
    }

    private void initialize() {
        setupManager = new SetupMgr();
        configMgr = new ConfigMgr(this);
    }

    public void reload() {
        reloadConfig();
        configMgr.loadFiles();
        MsgCache.load();
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
        pm.registerEvents(new SheepEvent(), this);
        pm.registerEvents(new SheepDmg(), this);
        pm.registerEvents(new ClickEvent(), this);

    }

    public static Main getInstance() {
        return plugin;
    }

    public static Component color(String str) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(str);
    }

    public static SetupMgr getSetupMgr(){
        return setupManager;
    }
}
