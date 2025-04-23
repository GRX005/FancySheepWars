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

    public static void setWaitingLobby(Player player, String name) {
        Arena asd =null;
        for (Arena a : ArenaMgr.arenas) {
            if (a.name.equalsIgnoreCase(name)) {
                asd = a; break;
            }
        }
        Location location = player.getLocation();
        assert asd != null;
        asd.setWaitingLobby(location);
        ArenaMgr.saveArena(asd);
        Main.saveArenaConfig();

    }

    public static void getWaitingLobby(Player player, String name){
        Arena asd =null;
        for (Arena a : ArenaMgr.arenas) {
            if (a.name.equalsIgnoreCase(name)) {
                asd = a; break;
            }
        }
        assert asd != null;
        Location location = asd.getWaitingLobby();
        player.teleportAsync(location).thenAccept(success -> {});

    }

    //
     //LOBBY
    //
    public static void setMainLobby(Player player) {
        Location location = player.getLocation();
        Main.locationManager.setLocation(Main.lobbyConfig,"MainLobby", location);
        Main.saveLobbyConfig();

    }

    public static void getMainLobby(Player player) {
        FileConfiguration config = Main.lobbyConfig;
        if (config != null && config.getConfigurationSection("MainLobby") != null) {
            Location loc = Main.locationManager.getLocation(Main.lobbyConfig,"MainLobby");
            player.teleport(loc);
        }
    }
}
