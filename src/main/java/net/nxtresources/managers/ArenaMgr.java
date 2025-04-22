package net.nxtresources.managers;

import net.nxtresources.enums.ArenaStatus;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ArenaMgr {

    public static Set<Arena> arenas = new HashSet<>();

    private static final HashMap<String,Arena> arCache = new HashMap<>();

    public static void mkCache() {
        //RT-CACHE, majd configbol valszeg.
        for(Arena a : arenas) {
            arCache.put(a.name, a);
        }
    }

    public static void make(String name, int size) {
        arenas.add(new Arena(name,size));
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

}
