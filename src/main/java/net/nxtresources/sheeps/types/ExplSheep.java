package net.nxtresources.sheeps.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.nxtresources.Main;
import net.nxtresources.sheeps.FancySheep;
import net.nxtresources.sheeps.SpawnAndRmSheep;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

import static net.nxtresources.managers.ItemMgr.explSheep;

public class ExplSheep extends SpawnAndRmSheep implements FancySheep {

    @Override
    public void giveSheep(Player p) {
        p.getInventory().addItem(explSheep);
    }

    public ExplSheep(){
        super("expl_sheep");
    }

    //TODO Sheep egyre kozelebb a robbanashoz -> egyre gyorsabban villog, kiloveskor 1szer flame particle szet loves?
    @Override
    public void shootSheep(Player p) {//TODO ADD DEFAULT FOR COMMON?
        final Location loc = p.getLocation();
        final World world = loc.getWorld();
        final Vector dir = loc.getDirection().normalize();
        var fLoc = loc.add(dir.multiply(1.5));
        fLoc.setY(loc.getY()+1.8);

        world.spawn(fLoc, Sheep.class, sh -> {
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
                    else
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
                //world.spawnParticle(Particle.FLAME, shLoc.clone().subtract(dir.clone().multiply(0.3)), 0,0,0,0,0);
                //world.spawnParticle(Particle.FLAME, shLoc, 0, 0, 0, 0, 5);
                for (int i = 0; i < 3; i++) {
                    p.spawnParticle(
                            Particle.FLAME,
                            shLoc.clone().add(
                                    (Math.random() - 0.5) * 0.3,
                                    (Math.random() - 0.5) * 0.3,
                                    (Math.random() - 0.5) * 0.3),
                            5,
                            0.01, 0.01, 0.01,
                            0.01
                    );
                }
                p.spawnParticle(Particle.SMOKE, shLoc, 3, 0.02, 0.02, 0.02, 0.005);
                sh.setVelocity(dir.multiply(sp));
            }, 0L, 1L);
        });
    }
    @Override
    public void customizeSheep(Sheep sh) {
        sh.setColor(DyeColor.RED);
        sh.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
    }
}
