package net.nxtresources.sheeps;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

public interface FancySheep {
    void giveSheep(Player p);
    void shootSheep(Player p);
    void spawnSheep(Location loc);
    void customizeSheep(Sheep s);
}
