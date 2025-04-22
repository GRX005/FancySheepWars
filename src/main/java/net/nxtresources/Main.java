package net.nxtresources;

import net.nxtresources.commands.CMDHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Main extends JavaPlugin {
    private Main plugin;

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

    }

    public Main getInstance() {
        return plugin;
    }
}
