package net.nxtresources.sheeps;

import net.nxtresources.Main;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.nxtresources.managers.ItemMgr.explSheep;

public abstract class SpawnAndRmSheep implements FancySheep {

    public NamespacedKey npk;

    public SpawnAndRmSheep(String id) {
        this.npk = new NamespacedKey(Main.getInstance(), id);
    }

    @Override
    public void spawnSheep(Location loc) {
        NamespacedKey sheepid = this.npk;
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, 0.5, 1.0, 0.5);
        for (Entity e : nearby) {
            PersistentDataContainer data = e.getPersistentDataContainer();
            if (data.has(sheepid, PersistentDataType.BYTE))
                return;
        }

        Sheep sheep = (Sheep) loc.getWorld().spawnEntity(loc, EntityType.SHEEP);
        sheep.setAI(false);
        sheep.setGravity(false);
        sheep.setCustomNameVisible(true);
        sheep.setBaby();
        sheep.setAgeLock(true);
        customizeSheep(sheep);

        PersistentDataContainer data = sheep.getPersistentDataContainer();
        data.set(sheepid, PersistentDataType.BYTE, (byte) 1);

        final double radius = 0.02; /* minimalism radiusz kell a szaggatás elkerulese erdekeben */
        final Location center = sheep.getLocation().clone();
        //var floating = 0;
        //var speed = 0;
        final double[]floating={0};
        final double[] speed= {0};
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), updateTask -> {
            double x = center.getX() + radius * Math.cos(speed[0]);
            double z = center.getZ() + radius * Math.sin(speed[0]);
            //floating
            double baseY = center.getY() + 0.2;
            double y = baseY + Math.sin(floating[0]) * 0.2;
            Location newLoc = new Location(center.getWorld(), x, y, z);
            newLoc.setYaw((float) Math.toDegrees(-speed[0] + Math.PI));
            sheep.teleport(newLoc);
            speed[0] += Math.toRadians(5); /* forgási sebesség */
            floating[0] += 0.1; /* fel-le sebesség */

            //pickup sheep
            Collection<Entity> nearbyEntities = sheep.getWorld().getNearbyEntities(sheep.getBoundingBox().expand(0.2, 0.5, 0.2));
            for (Entity e : nearbyEntities) {
                if (e instanceof Player player) {
                    PersistentDataContainer sheepData = sheep.getPersistentDataContainer();
                    if (sheepData.has(sheepid, PersistentDataType.BYTE)) {
                        player.getInventory().addItem(explSheep);
                        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
                        //removeSheeps(sheep.getWorld());
                        updateTask.cancel(); //stop animation
                        sheep.remove();
                        break;
                    }
                }
            }
        }, 0L, 1L);
    }

    public static void rmSheeps(World w){
        for (Entity e : w.getEntitiesByClass(Sheep.class)) {
            PersistentDataContainer data = e.getPersistentDataContainer();
            if (data.has(new NamespacedKey(Main.getInstance(), "expl_sheep"), PersistentDataType.BYTE))
                e.remove();
            //etc
        }
    }


    public static List<Location> getFreeSheepSpawns(Collection<Location> locations) {
        List<Location> free = new ArrayList<>();
        NamespacedKey explosiveSheepKey = new NamespacedKey(Main.getInstance(), "expl_sheep");
        for (Location loc : locations) {
            boolean sheep = false;
            for (Entity e : loc.getWorld().getNearbyEntities(loc, 0.5, 1.0, 0.5)) { //sync only
                if (e.getPersistentDataContainer().has(explosiveSheepKey, PersistentDataType.BYTE)) {
                    sheep = true;
                    break;
                }
            }
            if (!sheep) free.add(loc);
        }
        return free;
    }

    public NamespacedKey getSheepId() {
        return this.npk;
    }

}
