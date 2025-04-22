package net.nxtresources.managers;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ArenaMgr {

    public volatile static Set<Arena> arenas = new HashSet<>();

    public static void make(String name, int size) {
        arenas.add(new Arena(name,size));
    }
//Itt hozzaadjuk az adott jatekost az adott arenahoz: (JoinOrLeave)
    public static boolean join(String arena, Player player) {
        for(Arena a : arenas) {
            if(Objects.equals(a.name, arena)) {
                a.players.add(player);
                return true;
            }
        }
        return false;
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
