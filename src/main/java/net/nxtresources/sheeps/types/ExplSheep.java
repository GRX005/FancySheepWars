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

//    @Override
//    public void customize() {
//        super.customize();
//        sheep.setColor(DyeColor.RED);
//        sheep.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
//    }

    @Override
    public void spawnLaunchParticle(Location shLoc) {
        World world = shLoc.getWorld();

        Vector vel = sheep.getVelocity();

        Vector dir = vel.clone().normalize();
        Vector back = dir.clone().multiply(-1.0).subtract(vel);
        Location tail = shLoc.clone().add(back.getX(), 0.3 + back.getY(), back.getZ());

        world.spawnParticle(Particle.FLAME, tail, 3, 0.15, 0.15, 0.15, 0.02);
        world.spawnParticle(Particle.SMOKE, tail.clone().add(dir.clone().multiply(-0.4)), 2, 0.1, 0.1, 0.1, 0.01);
        if (sheep.getTicksLived() % 2 == 0) world.spawnParticle(Particle.LAVA, tail, 1, 0.05, 0.05, 0.05, 0);
        if (sheep.getTicksLived() % 5 == 0) world.spawnParticle(Particle.FIREWORK, tail, 1, 0.1, 0.1, 0.1, 0.03);
    }

    @Override
    public void tick(int tick, Location location){
        DyeColor dc = sheep.getColor();
        if (tick > 0 && tick % 20 == 0) sheep.setColor(dc == DyeColor.RED ? DyeColor.WHITE : DyeColor.RED); //TODO: gyorsulas
    }

    @Override
    public void explode(){
        sheep.remove();
        sheep.getLocation().createExplosion(sheep,4F, false);
        //world.spawnParticle(Particle.EXPLOSION, shLoc, 1); //NOT NEEDED SEE EVENT CANCEL OR NOT?
        //world.playSound(shLoc, Sound.ENTITY_GENERIC_EXPLODE, 4F,0.7F); //In MC pitch is random betw: 0.56-0.84
        var world = sheep.getWorld();
        var shLoc = sheep.getLocation();
        world.spawnParticle(Particle.EXPLOSION, shLoc, 3, 0.5, 0.5, 0.5, 0);
        world.spawnParticle(Particle.EXPLOSION_EMITTER, shLoc, 1, 0, 0, 0, 0);

        // Fire burst outward
        world.spawnParticle(Particle.FLAME, shLoc, 40, 0.3, 0.3, 0.3, 0.15);
        world.spawnParticle(Particle.LAVA, shLoc, 15, 0.5, 0.5, 0.5, 0);

        // Smoke mushroom cloud — rises up
        world.spawnParticle(Particle.LARGE_SMOKE, shLoc.clone().add(0, 1, 0), 45, 0.6, 0.4, 0.6, 0.05);
        world.spawnParticle(Particle.SMOKE, shLoc, 40, 1.0, 0.3, 1.0, 0.08);

        // Debris / sparks flying out
        world.spawnParticle(Particle.FIREWORK, shLoc, 20, 0.2, 0.2, 0.2, 0.2);

        // Sound to match
        world.playSound(shLoc, Sound.ENTITY_GENERIC_EXPLODE, 1.5f, 0.9f);
    }

    @Override
    public void giveSheep(Player player){
        player.getInventory().addItem(explSheep);
    }
}
