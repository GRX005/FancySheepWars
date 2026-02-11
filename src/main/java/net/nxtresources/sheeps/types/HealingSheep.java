package net.nxtresources.sheeps.types;

import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.FancySheep;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Objects;

import static net.nxtresources.managers.ItemMgr.healingSheep;

public class HealingSheep extends FancySheep {

    int max = 2000;
    int happyVillager = 45;
    int ticksRun = 0;
    int radius = 5;
    public HealingSheep(Player owner) {
        super(SheepType.HEALING, owner);
    }

    @Override
    public void giveSheep(Player player){
        player.getInventory().addItem(healingSheep);
    }

    @Override
    public void spawnLaunchParticle(Location shLoc){
        World world = shLoc.getWorld();
        if (world == null) return;

        Vector vel = sheep.getVelocity();

        Vector dir = vel.clone().normalize();
        Vector back = dir.clone().multiply(-1.0).subtract(vel);
        Location tail = shLoc.clone().add(back.getX(), 0.3 + back.getY(), back.getZ());

        world.spawnParticle(Particle.HAPPY_VILLAGER, tail, 4, 0.15, 0.15, 0.15, 0.02);
        world.spawnParticle(Particle.HEART, tail.clone().add(dir.clone().multiply(-0.4)), 1, 0.2, 0.2, 0.2, 0);
        if (sheep.getTicksLived() % 4 == 0) world.spawnParticle(Particle.END_ROD, tail, 1, 0.1, 0.1, 0.1, 0.01);
    }

    @Override
    public void explode() {
        ticksRun = 0;
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), expl ->{
            if(ticksRun>=max || sheep.isDead()) {
                if (sheep != null && !sheep.isDead()) sheep.remove();
                expl.cancel();
                return;
            }

            var loc = sheep.getLocation();

            //else location = loc.clone();
            final World world = loc.getWorld();

            double radius = 4.0;
            int tick = sheep.getTicksLived();

            // Rotating ring of green sparkles at ground level
            double angle1 = (tick % 40) / 40.0 * 2 * Math.PI;
            double angle2 = angle1 + Math.PI; // opposite side for symmetry
            for (double a : new double[]{angle1, angle2}) {
                world.spawnParticle(Particle.HAPPY_VILLAGER,
                        loc.clone().add(Math.cos(a) * radius, 0.2, Math.sin(a) * radius),
                        1, 0.1, 0.05, 0.1, 0);
            }

            // Gentle upward particles scattered inside the ring
            if (tick % 3 == 0) {
                double r = Math.random() * radius;
                double a = Math.random() * 2 * Math.PI;
                world.spawnParticle(Particle.COMPOSTER,
                        loc.clone().add(Math.cos(a) * r, 0.1, Math.sin(a) * r),
                        1, 0, 0, 0, 0.04);
            }

            // Rising hearts inside the aura
            if (tick % 10 == 0) {
                double r = Math.random() * (radius * 0.6);
                double a = Math.random() * 2 * Math.PI;
                world.spawnParticle(Particle.HEART,
                        loc.clone().add(Math.cos(a) * r, 0.5, Math.sin(a) * r),
                        1, 0, 0, 0, 0);
            }

            // Soft glow at the sheep (center beacon)
            world.spawnParticle(Particle.END_ROD, loc.clone().add(0, 0.8, 0), 1, 0.15, 0.3, 0.15, 0.005);
            for (Entity e : world.getNearbyEntities(loc, radius, radius, radius)) {
                if (e instanceof Player player) {
                    var maxHp = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
                    var newHp = Math.min(player.getHealth() + 0.5, maxHp);
                    player.setHealth(newHp);
                    player.spawnParticle(Particle.HEART, loc, 3, 2, 2, 2, 1);
                }
            }
            ticksRun += 10;
        }, 0L, 1L);
    }
}
