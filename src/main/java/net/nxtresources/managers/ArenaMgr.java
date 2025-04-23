package net.nxtresources.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

public class ArenaMgr {

    public static Set<Arena> arenas = new HashSet<>();

    private static final HashMap<String,Arena> arCache = new HashMap<>();

    public static void mkCache() {
        //RT-CACHE, majd configbol valszeg.
        for(Arena a : arenas) {
            arCache.put(a.name, a);
        }
    }

    public static Arena make(String name, int size) {
        Arena arena = new Arena(name,size);
        arenas.add(arena);
        return arena;
    }
//Itt hozzaadjuk az adott jatekost az adott arenahoz: (JoinOrLeave) 0=succ,1=noAr,2=alrInAr,3=arFull, 4=started
    public static int join(String arena, Player player) {
        if(ArenaMgr.isInArena(player))
            return 2;
        Arena a = arCache.get(arena);
        if(a==null)
            return 1;
        if(a.size<a.players.size())
            return 3;
        if(a.stat == ArenaStatus.STARTED)
            return 4;

        a.players.add(player);
        if(a.size==a.players.size()) {
            AtomicInteger aInt = new AtomicInteger(10);
            a.countdown(()->{
                int toPr = aInt.getAndDecrement();
                a.players.forEach((p->p.sendMessage("Az arena indul ennyi mulva: "+toPr)));
                if(aInt.get()==0) {
                    a.stat = ArenaStatus.STARTED;
                    a.cancelCount();
                }
            });
        }
        return 0;
    }

    public static void leave(Player p) {
        for(Arena a : arenas) { //Beepitett if check az alabb definialt func-ban
            a.players.remove(p);
        }
    }

    public static boolean isInArena(Player p) {
        for(Arena a : arenas) {
            for(Player pla : a.players) {
                if(p==pla)
                    return true;
            }
        }
        return false;
    }
//0=succ 1=noSuchArena 2=vannakBenneException
    public static int del(String ar) {
        if(arCache.containsKey(ar)) {
            Arena a = arCache.get(ar);
            if(!a.players.isEmpty())
                return 2;
            arenas.remove(a);
            return 0;
        }
        return 1;
    }

    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithModifiers(java.lang.reflect.Modifier.TRANSIENT)
            .setPrettyPrinting()
            .create();

    public static void saveArena(Arena arena) {
        String json = gson.toJson(arena);
        Main.getInstance().getArenaConfig().set("arenas." + arena.name, json);
        Main.getInstance().saveArenaConfig();
    }

    public static void loadAllArenas() {
        FileConfiguration config = Main.getInstance().getArenaConfig();

        if (config.contains("arenas")) {
            for (String key : Objects.requireNonNull(config.getConfigurationSection("arenas")).getKeys(false)) {
                String json = config.getString("arenas." + key);
                Arena arena = gson.fromJson(json, Arena.class);
                arena.players = new HashSet<>();
                arenas.add(arena);
                Main.getInstance().getLogger().log(Level.INFO,arena.name + " loaded!");
            }
        }
    }

}
