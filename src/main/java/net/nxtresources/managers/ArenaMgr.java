package net.nxtresources.managers;

import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.enums.BoardType;
import net.nxtresources.managers.scoreboard.Board;
import net.nxtresources.managers.scoreboard.BoardMgr;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;

import static net.nxtresources.Main.gson;

public class ArenaMgr {

    public static Set<Arena> arenas = new HashSet<>();

    public static Arena getByName(String name) {
        for (Arena a : arenas) {
            if (Objects.equals(a.name, name))
                return a;
        }
        return null;
    }

//Itt hozzaadjuk az adott jatekost az adott arenahoz: (JoinOrLeave) 0=succ,1=noAr,2=alrInAr,3=arFull, 4=started
    public static int join(String arena, Player player) {
        if(ArenaMgr.isInArena(player))
            return 2;
        Arena a = getByName(arena);
        if(a==null)
            return 1;
        if(a.size<a.lobbyPlayers.size())
            return 3;
        if(a.stat == ArenaStatus.STARTED)
            return 4;
        Location lobbyLoc = a.getWaitingLobby();
        if(lobbyLoc==null || lobbyLoc.getWorld() ==null)
            return 5;

        a.lobbyPlayers.add(player);
        SetupMgr.getWaitingLobby(player, arena);
        BoardMgr.setBoard(player, new Board(BoardType.WAITING)); //show a waiting board
        if(a.size==a.lobbyPlayers.size())
            a.countdownTask().runTaskTimerAsynchronously(Main.getInstance(),0L,20L);

        return 0;
    }

    public static void leave(Player p) {
        for(Arena a : arenas) { //Beepitett if check az alabb definialt func-ban
            if (a.lobbyPlayers.contains(p)) {
                a.lobbyPlayers.remove(p);
                SetupMgr.tpToLobby(p);
                BoardMgr.setBoard(p, new Board(BoardType.LOBBY));
                return;
            }
            for(Arena.Team t : a.teams) {
                t.tPlayers.forEach(pl->{
                    if(p==pl) {
                        t.tPlayers.remove(pl);
                        SetupMgr.tpToLobby(pl);
                        BoardMgr.setBoard(p, new Board(BoardType.LOBBY));
                    }
                });
            }
        }
    }

    public static boolean isInArena(Player p) {
        for(Arena a : arenas) {//Check InWaitingLobby
            for(Player pla : a.lobbyPlayers) {
                if(p==pla)
                    return true;
            }
            for(Arena.Team t : a.teams) {//Check IngameArena.
                for(Player pl : t.tPlayers) {
                    if(pl==p)
                        return true;
                }
            }
        }
        return false;
    }
//0=succ 1=noSuchArena 2=vannakBenneException
    public static int del(String ar) {
        var a = getByName(ar);
        if(a!=null) {
            if(!a.lobbyPlayers.isEmpty() || !a.teams.isEmpty())
                return 2;
            arenas.remove(a);
            Main.arenaConfig.set("arenas." + ar, null);
            Main.saveArenaConfig();
            return 0;
        }
        return 1;
    }


    public static void saveArena(Arena arena) {
        String json = gson.toJson(arena);
        Main.arenaConfig.set("arenas." + arena.name, json);
        Main.saveArenaConfig();
    }

    public static void loadAllArenas() {
        FileConfiguration config = Main.arenaConfig;

        if (config.contains("arenas")) {
            arenas.clear();
            List<String> loadedArenas = new ArrayList<>();
            for (String key : Objects.requireNonNull(config.getConfigurationSection("arenas")).getKeys(false)) {
                String json = config.getString("arenas." + key);
                Arena arena = gson.fromJson(json, Arena.class);
                arena.lobbyPlayers = new HashSet<>();
                arena.teams = new HashSet<>();
                arena.stat=ArenaStatus.WAITING;
                arenas.add(arena);
                loadedArenas.add(arena.name);
            }
            if(!loadedArenas.isEmpty())
                Main.getInstance().getLogger().log(Level.INFO, "Loaded arenas: "+ String.join(", ", loadedArenas));
            else
                Main.getInstance().getLogger().log(Level.INFO, "No arenas found!");
        }
    }
}
