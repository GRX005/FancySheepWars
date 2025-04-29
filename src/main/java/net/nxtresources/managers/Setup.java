package net.nxtresources.managers;

import com.google.gson.Gson;
import net.nxtresources.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Setup {

    private static String lobby;


    //
    //ARENA
    //
    public void setSpawn(Player player) {



    }

    public void setSheep(Player player) {

    }

    public static void setWaitingLobby(Player player) {
        Location loc = player.getLocation();
        if(!SetupManager.isInSetup(player))
            return;
        TemporaryArena tempData = SetupManager.tempdata.get(player.getUniqueId());
        if(tempData!=null)
            tempData.waitingLobby = loc;
    }

    public static void getWaitingLobby(Player player, String name){
        Arena arena =null;
        for (Arena a : ArenaMgr.arenas) {
            if (a.name.equalsIgnoreCase(name)) {
                arena = a;
                break;
            }
        }
        if(arena == null)
            return;
        Location location = arena.getWaitingLobby();
        player.teleportAsync(location);

    }

    //
     //LOBBY
    //

    public static void loadMainLobby(){
        if(Main.lobbyConfig.contains("lobby"))
            lobby = String.valueOf(LocationManager.get(Main.lobbyConfig.getString("lobby")));
    }
    public static void setMainLobby(Player player) {
        Location location = player.getLocation();
        Main.lobbyConfig.set("lobby", LocationManager.set(location));
        setLobby(location);
        Main.saveLobbyConfig();
    }

    public static void getMainLobby(Player player) {
        Location location = getLobby();
        if(location==null)
            return;
        player.teleportAsync(location);
    }

    public static void setLobby(Location loc) {
        lobby = LocationManager.set(loc);
    }
    public static Location getLobby() {
        return LocationManager.get(lobby);
    }
}
