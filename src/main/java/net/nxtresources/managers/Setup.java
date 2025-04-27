package net.nxtresources.managers;

import com.google.gson.Gson;
import net.nxtresources.Main;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Setup {

    private static String lobby;
    private static final Gson gson = new Gson();


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
            if (a.name.equalsIgnoreCase(name))
                asd = a; break;
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
            if (a.name.equalsIgnoreCase(name))
                asd = a; break;
        }
        assert asd != null;
        Location location = asd.getWaitingLobby();
        if(location == null)
            return;
        player.teleportAsync(location).thenAccept(success -> {});

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
        player.teleportAsync(location).thenAccept(success -> {});
    }

    public static void setLobby(Location loc) {
        lobby = LocationManager.set(loc);
    }
    public static Location getLobby() {
        return LocationManager.get(lobby);
    }
}
