package net.nxtresources.sheeps.types;

import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.FancySheep;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Objects;

import static net.nxtresources.managers.ItemMgr.healingSheep;

public class HealingSheep extends FancySheep {

    int max = 2000;
    int ticksRun = 0;
    public HealingSheep(Player owner) {
        super(SheepType.HEALING, owner);
    }

    @Override
    public void give(){
        owner.getInventory().addItem(healingSheep);
    }

    @Override
    public void trail(Location shLoc, World shWrld, Vector shVel){
        Vector dir = shVel.clone().normalize();
        Vector back = dir.clone().multiply(-1.0).subtract(shVel);
        Location tail = shLoc.clone().add(back.getX(), 0.3 + back.getY(), back.getZ());

        Particle.HAPPY_VILLAGER.builder()
                .location(tail)
                .count(4)
                .offset(0.15, 0.15, 0.15)
                .extra(0.02)
                .force(true)
                .spawn();

        Particle.HEART.builder()
                .location(tail.clone().add(dir.clone().multiply(-0.4)))
                .count(1)
                .offset(0.2, 0.2, 0.2)
                .extra(0)
                .force(true)
                .spawn();

        if (sheep.getTicksLived() % 4 == 0)
            Particle.END_ROD.builder()
                    .location(tail)
                    .count(1)
                    .offset(0.1, 0.1, 0.1)
                    .extra(0.01)
                    .force(true)
                    .spawn();
    }

    @Override
    public void removeSh() {
        sheep.remove();
    }
//TODO SEE ABOUT ASYNC
    @Override
    public void explode() {
        ticksRun = 0;
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), expl ->{
            if(ticksRun>=max || sheep.isDead()) {
                if (!sheep.isDead())
                    sheep.remove();
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
                Particle.HAPPY_VILLAGER.builder()
                        .location(loc.clone().add(Math.cos(a) * radius, 0.2, Math.sin(a) * radius))
                        .count(1)
                        .offset(0.1, 0.05, 0.1)
                        .extra(0)
                        .force(true)
                        .spawn();
            }

            // Gentle upward particles scattered inside the ring
            if (tick % 3 == 0) {
                double r = Math.random() * radius;
                double a = Math.random() * 2 * Math.PI;
                Particle.COMPOSTER.builder()
                        .location(loc.clone().add(Math.cos(a) * r, 0.1, Math.sin(a) * r))
                        .count(1)
                        .extra(0.04)
                        .force(true)
                        .spawn();
            }

            // Rising hearts inside the aura
            if (tick % 10 == 0) {
                double r = Math.random() * (radius * 0.6);
                double a = Math.random() * 2 * Math.PI;
                Particle.HEART.builder()
                        .location(loc.clone().add(Math.cos(a) * r, 0.5, Math.sin(a) * r))
                        .count(1)
                        .extra(0)
                        .force(true)
                        .spawn();
            }

            // Soft glow at the sheep (center beacon)
            Particle.END_ROD.builder()
                    .location(loc.clone().add(0, 0.8, 0))
                    .count(1)
                    .offset(0.15, 0.3, 0.15)
                    .extra(0.005)
                    .force(true)
                    .spawn();
            for (Player player : world.getNearbyPlayers(loc, radius, radius, radius)) {
                var maxHp = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
                var newHp = Math.min(player.getHealth() + 0.5, maxHp);
                player.setHealth(newHp);
                Particle.HEART.builder()
                        .location(loc)
                        .count(3)
                        .offset(2, 2, 2)
                        .extra(1)
                        .receivers(player)
                        .force(true)
                        .spawn();
            }
            ticksRun += 10;
        }, 0L, 1L);
    }
}
