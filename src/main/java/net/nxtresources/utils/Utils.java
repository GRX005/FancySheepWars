package net.nxtresources.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.nxtresources.Main;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Utils {

    private static final Map<UUID, Integer> lastTick = new HashMap<>();
    private static final Multimap<UUID, BukkitTask> drawDustTasks = ArrayListMultimap.create();

    public static boolean clickTick(Player player) {
        var cTick = Bukkit.getCurrentTick();
        Integer last = lastTick.put(player.getUniqueId(),cTick);
        return last != null && last == cTick;
    }

    public static void drawDust(Location location1, Location location2, Color color1, Color color2, double yMin, double yMax, UUID pUUID) {
        final double minX = Math.min(location1.getX(), location2.getX());
        final double maxX = Math.max(location1.getX(), location2.getX());
        final double actualMinY = Math.min(yMin, yMax);
        final double actualMaxY = Math.max(yMin, yMax);
        final double minZ = Math.min(location1.getZ(), location2.getZ());
        final double maxZ = Math.max(location1.getZ(), location2.getZ());

        final Particle.DustTransition dustData = new Particle.DustTransition(color1, color2, 1.5f);
        final double spacing = 0.30;
        var dust = Particle.DUST_COLOR_TRANSITION.builder()
                .extra(0)
                .data(dustData)
                .force(true);
        var wrld = location1.getWorld();

        BukkitTask task = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {
            // Reusable builder to minimize object allocation per particle
            // Horizontal Frame: Bottom (minY) and Top (maxY)
            for (double x = minX; x <= maxX; x += spacing) {
                dust.location(wrld, x, actualMinY, minZ).spawn();
                dust.location(wrld, x, actualMinY, maxZ).spawn();
                dust.location(wrld, x, actualMaxY, minZ).spawn();
                dust.location(wrld, x, actualMaxY, maxZ).spawn();
            }
            for (double z = minZ; z <= maxZ; z += spacing) {
                dust.location(wrld, minX, actualMinY, z).spawn();
                dust.location(wrld, maxX, actualMinY, z).spawn();
                dust.location(wrld, minX, actualMaxY, z).spawn();
                dust.location(wrld, maxX, actualMaxY, z).spawn();
            }

            // Vertical Pillars: Four corners
            for (double y = actualMinY; y <= actualMaxY; y += spacing) {
                dust.location(wrld, minX, y, minZ).spawn();
                dust.location(wrld, minX, y, maxZ).spawn();
                dust.location(wrld, maxX, y, minZ).spawn();
                dust.location(wrld, maxX, y, maxZ).spawn();
            }
        }, 0L, 5L);

        drawDustTasks.put(pUUID, task);
    }

    public static void endTasks(UUID pID) {
        drawDustTasks.removeAll(pID).forEach(BukkitTask::cancel);
    }

    public static boolean isOutsideRegion(Location loc, Location pos1, Location pos2) {
        double minX = Math.min(pos1.getX(), pos2.getX());
        double maxX = Math.max(pos1.getX(), pos2.getX());
        double minY = Math.min(pos1.getY(), pos2.getY());
        double maxY = Math.max(pos1.getY(), pos2.getY());
        double minZ = Math.min(pos1.getZ(), pos2.getZ());
        double maxZ = Math.max(pos1.getZ(), pos2.getZ());

        return !(loc.getX() >= minX) || !(loc.getX() <= maxX) || !(loc.getY() >= minY) || !(loc.getY() <= maxY) || !(loc.getZ() >= minZ) || !(loc.getZ() <= maxZ);
    }
}