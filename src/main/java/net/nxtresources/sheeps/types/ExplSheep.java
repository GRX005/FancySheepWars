package net.nxtresources.sheeps.types;

import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.FancySheep;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static net.nxtresources.managers.ItemMgr.explSheep;

public class ExplSheep extends FancySheep {

    public ExplSheep(Player owner) {
        super(SheepType.EXPLOSIVE, owner);
        //speed=1.0;
    }

    @Override
    public void give(){
        owner.getInventory().addItem(explSheep);
    }

//    @Override
//    public void customize() {
//        super.customize();
//        sheep.setColor(DyeColor.RED);
//        sheep.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
//    }

    @Override
    public  void trail(Location shLoc, World shWrld, Vector shVel) {
        Vector dir = shVel.clone().normalize();
        Vector back = dir.clone().multiply(-1.0).subtract(shVel);
        Location tail = shLoc.clone().add(back.getX(), 0.3 + back.getY(), back.getZ());

        Particle.FLAME.builder().location(tail).count(2).offset(0.15, 0.15, 0.15).extra(0.02).force(true).spawn();
        Particle.SMOKE.builder()
                .location(tail.clone().add(dir.clone().multiply(-0.4)))
                .count(2)
                .offset(0.1, 0.1, 0.1)
                .extra(0.01)
                .force(true)
                .spawn();
        if (sheep.getTicksLived() % 2 == 0)
            Particle.LAVA.builder()
                    .location(tail)
                    .count(1)
                    .offset(0.05, 0.05, 0.05)
                    .extra(0)
                    .force(true)
                    .spawn();

        if (sheep.getTicksLived() % 5 == 0)
            Particle.FIREWORK.builder()
                    .location(tail)
                    .count(1)
                    .offset(0.1, 0.1, 0.1)
                    .extra(0.03)
                    .force(true)
                    .spawn();
    }

    @Override
    public void tick(int tick, Location location){
        final DyeColor dc = sheep.getColor();
        if (tick>=30&&tick<50) {
            if (tick % 10 == 0) sheep.setColor(dc == DyeColor.RED ? DyeColor.WHITE : DyeColor.RED);
        }
        else if (tick>=50&&tick<70) {
            if (tick % 5 == 0) sheep.setColor(dc == DyeColor.RED ? DyeColor.WHITE : DyeColor.RED);
        }
        else if (tick>=70) {
            if (tick % 2 == 0) sheep.setColor(dc == DyeColor.RED ? DyeColor.WHITE : DyeColor.RED);
        }
    }

    @Override
    public void removeSh() {
        explode();
    }

    @Override
    public void explode() {
        sheep.remove();
        sheep.getLocation().createExplosion(sheep,4F, false);
        //world.spawnParticle(Particle.EXPLOSION, shLoc, 1); //NOT NEEDED SEE EVENT CANCEL OR NOT?
        //world.playSound(shLoc, Sound.ENTITY_GENERIC_EXPLODE, 4F,0.7F); //In MC pitch is random betw: 0.56-0.84
        var world = sheep.getWorld();
        var shLoc = sheep.getLocation();
        //world.spawnParticle(Particle.EXPLOSION, shLoc, 1, , 0);
        Particle.EXPLOSION.builder().location(shLoc).count(3).offset(0.5, 0.5, 0.5).force(true).spawn();
                //world.spawnParticle(Particle.EXPLOSION_EMITTER, shLoc, 1, 0, 0, 0, 0);

        // Fire burst outward
        //Particle.EXPLOSION_EMITTER.builder().location(shLoc).count(1).receivers(85, true).force(true).spawn();
// Flames
        Particle.FLAME.builder()
                .location(shLoc)
                .offset(0.3, 0.3, 0.3)
                .count(40)
                .extra(0.15)
                .force(true)
                .spawn();

// Lava
        Particle.LAVA.builder()
                .location(shLoc)
                .offset(0.5, 0.5, 0.5)
                .count(15)
                .extra(0.00)
                .force(true)
                .spawn();

// Smoke mushroom cloud — rises up
        Particle.LARGE_SMOKE.builder()
                .location(shLoc.clone().add(0, 1, 0))
                .offset(0.6, 0.4, 0.6)
                .count(45)
                .extra(0.05)
                .force(true)
                .spawn();

        Particle.SMOKE.builder()
                .location(shLoc)
                .offset(1.0, 0.3, 1.0)
                .count(40)
                .extra(0.08)
                .force(true)
                .spawn();

// Debris / sparks flying out
        Particle.FIREWORK.builder()
                .location(shLoc)
                .offset(0.2, 0.2, 0.2)
                .count(20)
                .extra(0.2)
                .force(true)
                .spawn();


        // Sound to match
        world.playSound(shLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.9f);
    }

}
