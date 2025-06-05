package net.nxtresources.managers;

import net.nxtresources.Main;
import net.nxtresources.enums.TeamType;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

import static net.nxtresources.managers.ItemMgr.*;

public class SetupMgr {
    public static final Map<UUID, String> playerSetupArena = new HashMap<>();
    public static final Set<Arena> temporaryArenas = new HashSet<>();
    public static final Map<UUID, Arena.Temp> tempdata = new HashMap<>();
    private static String lobby;

    public static int startSetup(Player player, String name, int size, boolean isTemporary){
        if(isInSetup(player))
            return 1;
        Arena arena;
        if(isTemporary){
            arena = new Arena(name,size);
            temporaryArenas.add(arena);
            tempdata.put(player.getUniqueId(), new Arena.Temp(name, size));
        }
        playerSetupArena.put(player.getUniqueId(), name);
        player.getInventory().clear();
        player.getInventory().setItem(0, setwaitinglobby);
        player.getInventory().setItem(7, saveAndExit);
        player.getInventory().setItem(8, leaveSetup);
        return 0;
    }

    public static void finishSetup(Player player, boolean succ){
        String name = playerSetupArena.remove(player.getUniqueId());
        Arena.Temp tempData = tempdata.remove(player.getUniqueId());
        if(name == null ||tempData==null)
            return;
        if(succ) {
            Arena arena = new Arena(tempData.name, tempData.size);
            if(tempData.waitingLobby!=null)
                arena.setWaitingLobby(tempData.waitingLobby);
            if(tempData.teamSpawns!=null)
                for (Map.Entry<String, Location> entry : tempData.teamSpawns.entrySet())
                    arena.setTeamSpawn(TeamType.valueOf(entry.getKey()), entry.getValue());
            if(tempData.pos1 != null && tempData.pos2 != null) {
                arena.setPos1(tempData.pos1);
                arena.setPos2(tempData.pos2);
            }
            ArenaMgr.arenas.add(arena);
            ArenaMgr.saveArena(arena);
            player.getInventory().clear();
            ItemMgr.lobbyItems(player);
            //copy world
            //World world = Bukkit.getWorld(name); TODO: kiirtani a barmokat



        } else{
            player.getInventory().clear();
            ItemMgr.lobbyItems(player);
            temporaryArenas.removeIf(a -> a.name.equals(name));
        }
    }

    //
    //tools
    //
    public void addSelector(){}

    //
    //ARENA
    //
    public void setSpawn(Player player) {}

    public void setSheep(Player player) {}

    public static void setWaitingLobby(Player player) {
        Location loc = player.getLocation();
        if(!SetupMgr.isInSetup(player))
            return;
        Arena.Temp tempData = SetupMgr.tempdata.get(player.getUniqueId());
        if(tempData!=null) {
            player.getInventory().clear();
            player.getInventory().setItem(0, red);
            player.getInventory().setItem(1, blue);
            player.getInventory().setItem(2, selectorTool);
            player.getInventory().setItem(7, saveAndExit);
            player.getInventory().setItem(8, leaveSetup);
            tempData.waitingLobby = loc;
        }
    }

    public static void getWaitingLobby(Player player, String name){
        Arena arena = ArenaMgr.getByName(name);

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
            lobby = String.valueOf(LocationMgr.get(Main.lobbyConfig.getString("lobby")));
    }
    public static void setMainLobby(Player player) {
        Location location = player.getLocation();
        Main.lobbyConfig.set("lobby", LocationMgr.set(location));
        setLobby(location);
        Main.saveLobbyConfig();
    }

    public static void tpToLobby(Player player) {
        Location location = getLobby();
        if(location==null)
            return;
        player.teleportAsync(location);
    }
    public static void setLobby(Location loc) {
        lobby = LocationMgr.set(loc);
    }
    public static Location getLobby() {
        return LocationMgr.get(lobby);
    }
    public static String getSetupArena(Player player) {
        return playerSetupArena.get(player.getUniqueId());
    }
    public static boolean isInSetup(Player player) {
        return playerSetupArena.containsKey(player.getUniqueId());
    }
}
