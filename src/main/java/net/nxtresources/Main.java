package net.nxtresources;

import net.nxtresources.commands.SheepWarsCommandHandler;
import org.bukkit.plugin.java.JavaPlugin;

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
        getCommand("sheepwars").setExecutor(new SheepWarsCommandHandler());

    }
    private void registerListeners() {

    }

    public Main getInstance() {
        return plugin;
    }
}
