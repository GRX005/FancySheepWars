package net.nxtresources.managers;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class ArenaMgr {

    public volatile static Set<Arena> arenas = new HashSet<>();

    public static void make(String name, int size) {
        arenas.add(new Arena(name,size));
    }
//Itt hozzaadjuk az adott jatekost az adott arenahoz: (JoinOrLeave) 0=succ,1=noAr,2=alrInAr,3=arFull
    public static int join(String arena, Player player) {
        if(ArenaMgr.isInArena(player)) {
            return 2;
        }
        for(Arena a : arenas) {
            if(Objects.equals(a.name, arena)) {
                if(a.size>a.players.size()) {
                    a.players.add(player);
                    if(a.size==a.players.size()) {
                        AtomicInteger aInt = new AtomicInteger(10);
                        a.countdown(()->{
                            int toPr = aInt.getAndDecrement();
                            a.players.forEach((p->p.sendMessage("Az arena indul ennyi mulva: "+toPr)));
                            if(aInt.get()==0) {
                                a.cancelCount();
                            }
                        });
                    }
                    return 0;
                }
                return 3;

            }
        }
        return 1;
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

}
