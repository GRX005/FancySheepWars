package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.FancySheep;
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

public class SheepMgr {

    public static void spawnSheep(Location loc, SheepType type) {
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, 0.5, 1.0, 0.5);
        for (Entity e : nearby) {
            PersistentDataContainer data = e.getPersistentDataContainer();
            if (data.has(Main.sheepKickupKey, PersistentDataType.BYTE)) return;
        }

        Sheep sheep = (Sheep) loc.getWorld().spawnEntity(loc, EntityType.SHEEP);
        sheep.setAI(false);
        sheep.setGravity(false);
        sheep.setCustomNameVisible(true);
        sheep.setBaby();
        sheep.setAgeLock(true);
        //customizeSheep(sheep);

        switch (type){
            case EXPLOSIVE -> {
                sheep.setColor(DyeColor.RED);
                sheep.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
            }
            case HEALING -> {
                sheep.setColor(DyeColor.PINK);
                sheep.customName(Component.text("Healing Sheep", NamedTextColor.LIGHT_PURPLE));
            }
        }

        PersistentDataContainer data = sheep.getPersistentDataContainer();
        data.set(Main.sheepKickupKey, PersistentDataType.STRING, type.name());

        final double radius = 0.02; /* minimalism radiusz kell a szaggatás elkerulese erdekeben */
        final Location center = sheep.getLocation().clone();
        final double[]floating={0};
        final double[] speed= {0};

        /*SHEEP ROTATING*/
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), updateTask -> {
            if(!sheep.isValid() || sheep.isDead()){
                updateTask.cancel();
                return;
            }
            var x = center.getX() + radius * Math.cos(speed[0]);
            var z = center.getZ() + radius * Math.sin(speed[0]);
            //floating
            var baseY = center.getY() + 0.2;
            var y = baseY + Math.sin(floating[0]) * 0.2;
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
                    String typeName = sheepData.get(Main.sheepKickupKey, PersistentDataType.STRING);

                    SheepType pickupType;
                    pickupType= SheepType.valueOf(typeName);

                    //player.getInventory().addItem(explSheep);
                    FancySheep fancy = FancySheep.create(pickupType, player);
                    fancy.giveSheep(player);
                    sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
                    updateTask.cancel(); //stop animation
                    sheep.remove();
                    break;
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
