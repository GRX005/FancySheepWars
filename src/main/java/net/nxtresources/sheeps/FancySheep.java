package net.nxtresources.sheeps;

import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.types.ExplSheep;
import net.nxtresources.sheeps.types.HealingSheep;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;

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
//TODO Read papper particle docs, use ParticleBuilder for explosion and other effects

    public void movement(boolean gravity) {
        var loc = owner.getLocation().add(0, 1.8, 0);
        var vel = loc.getDirection().multiply(speed); // Pre-calculate velocity
        loc.add(vel.clone().multiply(1.5 / speed));

        var wrld = loc.getWorld();
        wrld.spawnParticle(Particle.SMOKE, loc, 3, 0.02, 0.02, 0.02, 0.005);

        wrld.spawn(loc, Sheep.class, sh -> {
            //sh.setVelocity(dir.clone().multiply(speed)); //first kick
            sheep = sh;
            customize();
            sh.getPersistentDataContainer().set(Main.shKey, PersistentDataType.STRING, type.name());
            //Location shLoc = sh.getLocation();
            final var nmsSh = ((CraftEntity) sh).getHandle();
            final int[] t = {0};
            //Bukkit.getMobGoals().removeAllGoals(sh); //Instead of setAI false, so it can still move but it won't
            sh.setAware(false);

            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task -> {
                //int t = timer.getAndIncrement();
                if (!sh.isValid() || ++t[0] > 100) {
                    task.cancel();
                    sh.remove();
                    return;
                }

                sh.setVelocity(vel); //loop kick
                var shLoc = sh.getLocation();
                spawnLaunchParticle(shLoc);
                tick(t[0], shLoc);

                if(nmsSh.horizontalCollision || nmsSh.verticalCollision || !wrld.getNearbyEntities(sh.getBoundingBox(), e -> e instanceof Player p && !p.equals(owner)).isEmpty()){
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
    public abstract void customize();

    public void tick(int tick, Location loc) {}
}
