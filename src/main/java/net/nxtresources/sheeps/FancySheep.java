package net.nxtresources.sheeps;

import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.types.ExplSheep;
import net.nxtresources.sheeps.types.HealingSheep;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class FancySheep {


    /*
    * Harom allapota van a baranyoknak
    *
    * 1. Item (jatekos invjebe) -> ItemMgr.java
    * 2. Kilott barany -> 'sheeps' package
    * 3. forgo barany modell (amit felvesz a jatekos) -> SheepMgr.java
    *
    * */

    public SheepType type;
    public double speed;
    public double damage;
    public double radius; //barany hatotavolsaga
    public Player owner;
    public Sheep sheep;

    public FancySheep(SheepType type, Player owner){
        this.type = type;
        this.owner=owner;
        init();

    }

    public static FancySheep create(SheepType type, Player owner) {
        return switch (type) {
            case HEALING -> new HealingSheep(owner);
            case EXPLOSIVE -> new ExplSheep(owner);
        };
    }

    private void init(){
        speed=1.0; //sheep default launch speed
        damage=4.0;
        radius=3.0;
    }//Barany hang kiloveskor


    public void tick(Sheep sheep, int tick, Location loc){}
    public void movement(boolean gravity) {
        var loc = owner.getLocation().add(0, 1.8, 0);
        var dir = loc.getDirection().normalize();
        loc.add(dir.clone().multiply(1.5));//TODO make the effect better
        owner.spawnParticle(Particle.SMOKE, loc, 3, 0.02, 0.02, 0.02, 0.005);
//        final var sp = 1.0;
        var wrld = loc.getWorld();
        wrld.spawn(loc, Sheep.class, sh -> {
            //sh.setVelocity(dir.clone().multiply(speed)); //first kick
            sheep = sh;
            customize(sh);
            sh.getPersistentDataContainer().set(Main.shKey, PersistentDataType.STRING, type.name());
            //Location shLoc = sh.getLocation();
            final var nmsSh = ((CraftEntity) sh).getHandle();
            final AtomicInteger timer = new AtomicInteger(0);
            Bukkit.getMobGoals().removeAllGoals(sh); //Instead of setAI false, so it can still move but it won't

            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task -> {
                int t = timer.getAndIncrement();
                if (!sh.isValid() || t > 100) {
                    task.cancel();
                    sh.remove();
                    return;
                }

                sh.setVelocity(dir.clone().multiply(speed)); //loop kick
                var shLoc = sh.getLocation();
                spawnLaunchParticle(shLoc);
                tick(sh, t, shLoc);

                var hits = wrld.getNearbyEntities(sh.getBoundingBox(), e -> e instanceof Player && !e.equals(owner));
                var hitBlock = nmsSh.horizontalCollision || nmsSh.verticalCollision;

                if(hitBlock || !hits.isEmpty()){
                    task.cancel();
                    sh.setVelocity(new Vector());
                    explode();
                }
            }, 0L, 1L);
        });
    }
    public abstract void explode();
    public abstract void giveSheep(Player player);
    public abstract void spawnLaunchParticle(Location shLoc);
    public abstract void customize(Sheep sheep);
}
