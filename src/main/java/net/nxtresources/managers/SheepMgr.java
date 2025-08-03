package net.nxtresources.managers;

import net.nxtresources.sheeps.FancySheep;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SheepMgr {

    public static void giveSheep(FancySheep s, Player p) {
        s.giveSheep(p);
    }

    public static void shootSheep(FancySheep s,Player p) {
        s.shootSheep(p);
    }

    public static void spawnSheep(FancySheep s, Location loc) {
        s.spawnSheep(loc);
    }
}
