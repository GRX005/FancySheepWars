package net.nxtresources.sheeps;

import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.types.ExplSheep;
import net.nxtresources.sheeps.types.HealingSheep;
import org.bukkit.*;
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
    }

    public void spawnLaunchParticle(Location shLoc){}
    public void customize(Sheep sheep){}
    public void tick(Sheep sheep, int tick, Location loc){}
    public void movement(){
        Location loc = owner.getLocation();
        World w = loc.getWorld();
        final Vector dir = loc.getDirection().normalize();
        var fLoc = loc.add(dir.multiply(1.5));
//        final var sp = 1.0;
        fLoc.setY(loc.getY() + 1.8);
        w.spawn(fLoc, Sheep.class, sh -> {
            sh.setVelocity(dir.multiply(speed)); //first kick
            sheep = sh;
            customize(sh);
            sh.getPersistentDataContainer().set(Main.shKey, PersistentDataType.STRING, type.name());
            //Location shLoc = sh.getLocation();
            final var nmsSh = ((CraftEntity) sh).getHandle();
            final AtomicInteger timer = new AtomicInteger(0);
            Bukkit.getMobGoals().removeAllGoals(sh); //Instead of setAI false, so it can still move but it won't
            owner.spawnParticle(Particle.SMOKE, loc, 3, 0.02, 0.02, 0.02, 0.005);
            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task -> {
                if (sh.isDead() || !sh.isValid()) {
                    task.cancel();
                    return;
                }
                int t = timer.getAndIncrement();
                if(t > 100){
                    sh.remove();
                    task.cancel();
                    return;
                }
                sh.setVelocity(dir.clone().multiply(speed)); //loop kick
                Location shLoc = sh.getLocation();
                spawnLaunchParticle(shLoc);
                tick(sh, t, shLoc);
                Collection<Entity> hits = w.getNearbyEntities(sh.getBoundingBox(), e -> e instanceof Player && !e.equals(owner));
                var hitBlock = nmsSh.horizontalCollision || nmsSh.verticalCollision;
                var hitEntity = !hits.isEmpty();
                if(hitBlock || hitEntity){
                    task.cancel();
                    sh.setVelocity(new Vector(0, 0, 0));
                    explode(shLoc.clone());
                }
            }, 0L, 1L);
        });
    }
    public abstract void explode(Location loc);
    public abstract void giveSheep(Player player);
}
