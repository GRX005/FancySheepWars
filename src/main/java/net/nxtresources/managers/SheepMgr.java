package net.nxtresources.managers;

import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.FancySheep;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SheepMgr {

    private static final double ORBIT_R = 0.02;
    private static final double ROT_SPEED = Math.toRadians(5); // ~0.08727 rad/tick
    private static final double BOB_SPEED = 0.1;
    private static final double BOB_AMP = 0.2;
    private static final double PICKUP_R = 0.7;
    private static final double PICKUP_R_SQ = PICKUP_R * PICKUP_R;

    public static void spawnSheep(Location loc, SheepType type) {
        var w = loc.getWorld();
        Collection<Entity> nearby = w.getNearbyEntities(loc, 0.5, 1.0, 0.5);
        for (Entity e : nearby) {
            PersistentDataContainer data = e.getPersistentDataContainer();
            if (data.has(Main.sheepKickupKey, PersistentDataType.STRING)) return;
        }

        Sheep sheep = w.spawn(loc, Sheep.class, s -> {
            s.setAI(false);
            s.setGravity(false);
            s.setCustomNameVisible(true);
            s.setBaby();
            s.setAgeLock(true);
            s.setColor(type.dyeColor());
            s.customName(type.displayName());
            s.getPersistentDataContainer()
                    .set(Main.sheepKickupKey, PersistentDataType.STRING, type.name());
        });

        final double cx = loc.getX(), cy = loc.getY() + BOB_AMP, cz = loc.getZ();
        final double[] state = {0, 0};
        final Location nl = new Location(w,0,0,0);
        /*SHEEP ROTATING*/
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task -> {
            if (!sheep.isValid()) {
                task.cancel();
                return;
            }

            double angle = (state[0] += ROT_SPEED);
            double bob = (state[1] += BOB_SPEED);

            nl.set(cx + ORBIT_R * Math.cos(angle),
                    cy + Math.sin(bob) * BOB_AMP,
                    cz + ORBIT_R * Math.sin(angle));
            nl.setYaw((float) Math.toDegrees(-angle + Math.PI));
            sheep.teleport(nl);

            for (Player p : w.getPlayers()) {
                if (p.getLocation().distanceSquared(nl) < PICKUP_R_SQ) {
                    FancySheep.create(type, p).giveSheep(p);
                    w.playSound(nl, Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
                    sheep.remove();
                    task.cancel();
                    return;
                }
            }
        }, 0L, 1L);
    }

    public static void rmSheeps(World w){
        for (Entity e : w.getEntitiesByClass(Sheep.class)) {
            PersistentDataContainer data = e.getPersistentDataContainer();
            if (data.has(Main.sheepKickupKey, PersistentDataType.STRING)) e.remove();
            //etc
        }
    }

    public static List<Location> getFreeSheepSpawns(Collection<Location> locations) {
        List<Location> free = new ArrayList<>();
        for (Location loc : locations) {
            var sheep = false;
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 0.5, 1.0, 0.5)) { //sync only
                PersistentDataContainer data = e.getPersistentDataContainer();
                if (data.has(Main.sheepKickupKey, PersistentDataType.STRING)) {
                    sheep = true;
                    break;
                }
            }
            if (!sheep) free.add(loc);
        }
        return free;
    }
}
