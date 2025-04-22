package net.nxtresources.managers;

import net.nxtresources.Main;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class SetupManager {

    LocationManager lm = Main.getInstance().getLocationManager();

    //
    //ARENA
    //
    public void setSpawn(Player player) {



    }

    public void setSheep(Player player) {

    }

    //
     //LOBBY
    //
    public void setMainLobby(Player player) {
        Location location = player.getLocation();
        lm.setLocation(Main.getInstance().lobbyConfig,"MainLobby", location);
        Main.getInstance().saveLobbyConfig();

    }

    public void getMainLobby(Player player) {
        FileConfiguration config = Main.getInstance().getLobbyConfig();
        if (config != null && config.getConfigurationSection("MainLobby") != null) {
            Location loc = lm.getLocation(Main.getInstance().lobbyConfig,"MainLobby");
            player.teleport(loc);
        }
    }
}
