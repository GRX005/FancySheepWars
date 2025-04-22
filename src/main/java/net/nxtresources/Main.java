package net.nxtresources;

import net.nxtresources.commands.CMDHandler;
import net.nxtresources.listeners.InteractEvent;
import net.nxtresources.listeners.ItemDropEvent;
import net.nxtresources.listeners.JoinEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {
    private static Main plugin;

    @Override
    public void onEnable() {
        plugin =this;
        initialize();
        registerCommands();
        registerListeners();
        // Plugin startup logic

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void initialize() {

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
