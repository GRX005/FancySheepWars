package net.nxtresources.managers;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.IOException;

public class LobbyMgr {

    private static String mainLobbyLocation;

    //for main lobby
    public static void setLobbyLocation(Location location){
        mainLobbyLocation = LocationMgr.set(location);
    }

    public static Location getLobbyLocation(){
        return LocationMgr.get(mainLobbyLocation);
    }

    public static void setMainLobby(Player player){
        Location location = player.getLocation();
        ConfigMgr.lobbyConfig.set("lobby", LocationMgr.set(location));
        try{
            ConfigMgr.lobbyConfig.save(ConfigMgr.lobbyFile);
            setLobbyLocation(location);
        } catch (RuntimeException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void tpMainLobby(Player player){
        Location location = getLobbyLocation();
        player.teleportAsync(location);
    }

    public static void loadMainLobby(){
        if(ConfigMgr.lobbyConfig.contains("lobby"))
            mainLobbyLocation = ConfigMgr.lobbyConfig.getString("lobby");
    }
}
