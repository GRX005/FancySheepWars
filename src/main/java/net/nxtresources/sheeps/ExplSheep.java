package net.nxtresources.sheeps;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.nxtresources.ItemBuilder;
import net.nxtresources.Main;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;

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
            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task -> {
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
}
