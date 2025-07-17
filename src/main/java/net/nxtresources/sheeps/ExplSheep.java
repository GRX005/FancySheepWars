package net.nxtresources.sheeps;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minecraft.util.parsing.packrat.Atom;
import net.nxtresources.Main;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static net.nxtresources.managers.ItemMgr.blue;
import static net.nxtresources.managers.ItemMgr.explSheep;

public class ExplSheep implements FancySheep {
    @Override
    public void giveSheep(Player p) {
        p.getInventory().addItem(explSheep);

    }

    @Override
    public void shootSheep(Player p) {//TODO ADD DEFAULT FOR COMMON?
        final Location loc = p.getLocation();
        final World world = loc.getWorld();
        final Vector dir = loc.getDirection().normalize();
        world.spawn(loc.add(dir.multiply(1.5)), Sheep.class, sh -> {
            sh.setColor(DyeColor.RED);
            sh.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
            sh.setCustomNameVisible(true); //Name visible all the time, not just when entity in aim
            Bukkit.getMobGoals().removeAllGoals(sh); //Instead of setAI false, so it can still move but it won't
            sh.setGravity(false);
            final var sp = 1.0;
            final var nmsSh = ((CraftEntity) sh).getHandle(); //We check block collisions via NMS
            var timer = new AtomicInteger(0);
            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task -> {
                var tim = timer.get();
                timer.set(tim+1);
                if (tim>0&& tim%20==0) {
                    DyeColor dc = sh.getColor();
                    if(dc==DyeColor.RED)
                        sh.setColor(DyeColor.WHITE);
                    else if(dc==DyeColor.WHITE)
                        sh.setColor(DyeColor.RED);
                }
                final Location shLoc = sh.getLocation();
                if(sh.isDead() || !sh.isValid())
                    task.cancel();
                Collection<Entity> hits = world.getNearbyEntities(sh.getBoundingBox()); //Get entity collisions
                if(nmsSh.horizontalCollision || nmsSh.verticalCollision || hits.size() != 1 && !hits.contains(p)) { //We reg a hit here.
                    sh.remove();
                    shLoc.createExplosion(sh,3F, false);
                    world.spawnParticle(Particle.EXPLOSION, shLoc, 1);
                    world.playSound(shLoc, Sound.ENTITY_GENERIC_EXPLODE, 4F,0.7F); //In MC pitch is random betw: 0.56-0.84
                    task.cancel();
                }
                world.spawnParticle(Particle.FLAME, shLoc, 1);
                sh.setVelocity(dir.multiply(sp));
            },0L, 1L);
        });
    }

    //TODO: cat sound
    // sheep spawning
    public static void spawnSheep(Location loc) {
        NamespacedKey explosiveSheep = new NamespacedKey(Main.getInstance(), "explosive_sheep");
        Collection<Entity> nearby = loc.getWorld().getNearbyEntities(loc, 0.5, 1.0, 0.5);
        for(Entity e : nearby) {
            PersistentDataContainer data = e.getPersistentDataContainer();
            if (data.has(explosiveSheep, PersistentDataType.BYTE))
                return;
        }

        Sheep sheep = (Sheep) loc.getWorld().spawnEntity(loc, EntityType.SHEEP);
        sheep.setAI(false);
        sheep.setGravity(false);
        sheep.setCustomNameVisible(true);
        sheep.setBaby();
        sheep.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
        sheep.setColor(DyeColor.RED);
        sheep.setAgeLock(true);

        PersistentDataContainer data = sheep.getPersistentDataContainer();
        data.set(explosiveSheep, PersistentDataType.BYTE, (byte) 1);

        final double radius = 0.02; /* minimalism radiusz kell a szaggatás elkerulese erdekeben */
        final Location center = sheep.getLocation().clone();
        dVariable floating = new dVariable(0);
        dVariable speed = new dVariable(0);
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), updateTask -> {
            double x = center.getX() + radius * Math.cos(speed.d);
            double z = center.getZ() + radius * Math.sin(speed.d);
            //floating
            double baseY = center.getY() + 0.2;
            double y = baseY + Math.sin(floating.d) * 0.2;
            Location newLoc = new Location(center.getWorld(), x, y, z);
            newLoc.setYaw((float) Math.toDegrees(-speed.d + Math.PI));
            sheep.teleport(newLoc);
            speed.d += Math.toRadians(5); /* forgási sebesség */
            floating.d += 0.1; /* fel-le sebesség */

            //pickup sheep
            Collection<Entity> nearbyEntities = sheep.getWorld().getNearbyEntities(sheep.getBoundingBox().expand(0.2, 0.5, 0.2));
            for (Entity e : nearbyEntities) {
                if (e instanceof Player player) {
                    PersistentDataContainer sheepData = sheep.getPersistentDataContainer();
                    if (sheepData.has(explosiveSheep, PersistentDataType.BYTE)) {
                        player.getInventory().addItem(explSheep);
                        sheep.getWorld().playSound(sheep.getLocation(), Sound.ENTITY_ITEM_PICKUP, 1F, 1F);
                        removeSheeps(sheep.getWorld());
                        updateTask.cancel(); //stop animation
                        break;
                    }
                }
            }
        },0L, 1L);
    }

    public static void removeSheeps(World world) {
        NamespacedKey explosiveSheep = new NamespacedKey(Main.getInstance(), "explosive_sheep");
        for (Entity e : world.getEntities()) {
            if (e instanceof Sheep) {
                PersistentDataContainer data = e.getPersistentDataContainer();
                if (data.has(explosiveSheep, PersistentDataType.BYTE))
                    e.remove();
            }
        }
    }


    //gpt gané
    public static List<Location> getFreeSheepSpawns(Collection<Location> locations) {
        List<Location> free = new ArrayList<>();
        NamespacedKey explosiveSheepKey = new NamespacedKey(Main.getInstance(), "explosive_sheep");
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

    static class dVariable {
        double d;
        public dVariable(double d) {
            this.d = d;
        }

    }
}
