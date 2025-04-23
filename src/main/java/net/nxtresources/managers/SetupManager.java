package net.nxtresources.managers;

import net.nxtresources.Main;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetupManager {


    //
    //ARENA
    //
    public void setSpawn(Player player) {



    }

    public void setSheep(Player player) {

    }

    public void setWaitingLobby(Player player) {
        Location location = player.getLocation();

    }

    //
     //LOBBY
    //
    public void setMainLobby(Player player) {
        Location location = player.getLocation();
        Main.locationManager.setLocation(Main.lobbyConfig,"MainLobby", location);
        Main.saveLobbyConfig();

    }

    public void getMainLobby(Player player) {
        FileConfiguration config = Main.lobbyConfig;
        if (config != null && config.getConfigurationSection("MainLobby") != null) {
            Location loc = Main.locationManager.getLocation(Main.lobbyConfig,"MainLobby");
            player.teleport(loc);
        }
    }
}
