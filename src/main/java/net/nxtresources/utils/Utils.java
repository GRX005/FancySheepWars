package net.nxtresources.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.nxtresources.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Utils {

    private static final Map<UUID, Integer> lastTick = new HashMap<>();
    private static final Multimap<UUID, BukkitTask> drawDustTasks = ArrayListMultimap.create();

    public static boolean clickTick(Player player){
        Integer last = lastTick.get(player.getUniqueId());
        if (last != null && last == Bukkit.getCurrentTick()) return true;
        lastTick.put(player.getUniqueId(), Bukkit.getCurrentTick());
        return false;
    }

    public static void drawDust(Location location1, Location location2, Color color1, Color color2, double yMin, double yMax, UUID pUUID){
        var ta = Bukkit.getScheduler().runTaskTimer(Main.getInstance(),()-> {
            World world = location1.getWorld();

            double minX = Math.min(location1.getX(), location2.getX());
            double maxX = Math.max(location1.getX(), location2.getX());
            double minY = Math.min(yMin, yMax);
            double maxY = Math.max(yMin, yMax);
            double minZ = Math.min(location1.getZ(), location2.getZ());
            double maxZ = Math.max(location1.getZ(), location2.getZ());
            double particleSpacing = 0.30f;

            Particle.DustTransition dust = new Particle.DustTransition(color1, color2, 1.5f);

            //Lower frame
            for (double x = minX; x <= maxX; x += particleSpacing) {
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, x, minY, minZ, 1, 0, 0, 0, 0, dust);
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, x, minY, maxZ, 1, 0, 0, 0, 0, dust);
            }
            for (double z = minZ; z <= maxZ; z += particleSpacing) {
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, minX, minY, z, 1, 0, 0, 0, 0, dust);
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, maxX, minY, z, 1, 0, 0, 0, 0, dust);
            }

            //Upper frame
            for (double x = minX; x <= maxX; x += particleSpacing) {
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, x, maxY, minZ, 1, 0, 0, 0, 0, dust);
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, x, maxY, maxZ, 1, 0, 0, 0, 0, dust);
            }
            for (double z = minZ; z <= maxZ; z += particleSpacing) {
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, minX, maxY, z, 1, 0, 0, 0, 0, dust);
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, maxX, maxY, z, 1, 0, 0, 0, 0, dust);
            }

            //Vertical strips
            for (double y = minY; y <= maxY; y += particleSpacing) {
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, minX, y, minZ, 1, 0, 0, 0, 0, dust);
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, minX, y, maxZ, 1, 0, 0, 0, 0, dust);
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, maxX, y, minZ, 1, 0, 0, 0, 0, dust);
                world.spawnParticle(Particle.DUST_COLOR_TRANSITION, maxX, y, maxZ, 1, 0, 0, 0, 0, dust);
            }
        },0,10);
        drawDustTasks.put(pUUID,ta);
    }

    public static void endTasks(UUID pID) {
        drawDustTasks.removeAll(pID).forEach(BukkitTask::cancel);
    }

    public static boolean isInsideRegion(Location loc, Location pos1, Location pos2) {
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return !(loc.getX() >= minX) || !(loc.getX() <= maxX) || !(loc.getY() >= minY) || !(loc.getY() <= maxY) || !(loc.getZ() >= minZ) || !(loc.getZ() <= maxZ);
    }
}