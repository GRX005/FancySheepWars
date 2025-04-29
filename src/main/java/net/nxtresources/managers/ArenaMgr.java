package net.nxtresources.managers;

import com.google.gson.Gson;
import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static net.nxtresources.Main.gson;

public class ArenaMgr {

    public static Set<Arena> arenas = new HashSet<>();

    public static final HashMap<String,Arena> arCache = new HashMap<>();

    public static void mkCache() {
        //RT-CACHE, majd configbol valszeg.
        for(Arena a : arenas) {
            arCache.put(a.name, a);
        }
    }

    public static Arena make(String name, int size) {
        Arena arena = new Arena(name,size);
        arenas.add(arena);
        arCache.put(name, arena);
        return arena;
    }
//Itt hozzaadjuk az adott jatekost az adott arenahoz: (JoinOrLeave) 0=succ,1=noAr,2=alrInAr,3=arFull, 4=started
    public static int join(String arena, Player player) {
        if(ArenaMgr.isInArena(player))
            return 2;
        Arena a = arCache.get(arena);
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
        Setup.getWaitingLobby(player, arena);
        if(a.size==a.lobbyPlayers.size()) {
            AtomicInteger aInt = new AtomicInteger(10);
            a.countdown(()->{
                int toPr = aInt.getAndDecrement();
                a.lobbyPlayers.forEach((p->p.sendMessage("Az arena indul ennyi mulva: "+toPr)));
                if(aInt.get()==0) {//Itt indul az arena.
                    a.stat = ArenaStatus.STARTED;
                    a.start();
                    a.cancelCount();
                }
            });
        }
        return 0;
    }

    public static void leave(Player p) {
        for(Arena a : arenas) { //Beepitett if check az alabb definialt func-ban
            a.lobbyPlayers.remove(p);
            for(Arena.Team t : a.teams) {
                t.tPlayers.forEach(pl->{
                    if(p==pl) {
                        t.tPlayers.remove(pl);
                        Setup.getMainLobby(pl);
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
        if(arCache.containsKey(ar)) {
            Arena a = arCache.get(ar);
            if(!a.lobbyPlayers.isEmpty() || !a.teams.isEmpty())
                return 2;
            arenas.remove(a);
            arCache.remove(ar);
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
            arCache.clear();
            for (String key : Objects.requireNonNull(config.getConfigurationSection("arenas")).getKeys(false)) {
                String json = config.getString("arenas." + key);
                Arena arena = gson.fromJson(json, Arena.class);
                arena.lobbyPlayers = new HashSet<>();
                arena.teams = new HashSet<>();
                arenas.add(arena);
                arCache.put(arena.name, arena);
                Main.getInstance().getLogger().log(Level.INFO,arena.name + " loaded!");
            }
        }
    }

}
