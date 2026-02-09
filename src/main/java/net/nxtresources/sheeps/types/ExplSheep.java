package net.nxtresources.sheeps.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.FancySheep;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import static net.nxtresources.managers.ItemMgr.explSheep;

public class ExplSheep extends FancySheep {

    public ExplSheep(Player owner) {
        super(SheepType.EXPLOSIVE, owner);
        //speed=1.0;
    }

    @Override
    public void customize() {
        super.customize();
        sheep.setColor(DyeColor.RED);
        sheep.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
    }

    @Override
    public void spawnLaunchParticle(Location shLoc) {
        World world = shLoc.getWorld();

        Vector vel = sheep.getVelocity();
        if (vel.lengthSquared() < 0.001) return;

        Vector dir = vel.clone().normalize();
        Vector back = dir.clone().multiply(-1.0).subtract(vel);
        Location tail = shLoc.clone().add(back.getX(), 0.3 + back.getY(), back.getZ());

        world.spawnParticle(Particle.FLAME, tail, 3, 0.15, 0.15, 0.15, 0.02);
        world.spawnParticle(Particle.SMOKE, tail.clone().add(dir.clone().multiply(-0.4)), 2, 0.1, 0.1, 0.1, 0.01);
        if (sheep.getTicksLived() % 3 == 0) world.spawnParticle(Particle.LAVA, tail, 1, 0.1, 0.1, 0.1, 0);
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
    }

    @Override
    public void giveSheep(Player player){
        player.getInventory().addItem(explSheep);
    }
}
