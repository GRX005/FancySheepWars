package net.nxtresources.sheeps.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.FancySheep;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.util.Vector;

import java.util.Collection;

import static net.nxtresources.managers.ItemMgr.explSheep;

public class ExplSheep extends FancySheep {


    public ExplSheep(Player owner) {
        super(SheepType.EXPLOSIVE, owner);
        speed=1.0;
    }

    @Override
    public void customize(Sheep sheep){
        sheep.setGravity(true);
        sheep.setColor(DyeColor.RED);
        sheep.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
        sheep.setCustomNameVisible(true); //Name visible all the time, not just when entity in aim
    }

    @Override
    public void spawnLaunchParticle(Location shLoc){
        for (int i = 0; i < 3; i++) owner.spawnParticle(Particle.FLAME, shLoc.clone().add((Math.random() - 0.5) * 0.3,(Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3), 5, 0.01, 0.01, 0.01, 0.01);
    }

    @Override
    public void tick(Sheep sheep, int tick, Location location){
        DyeColor dc = sheep.getColor();
        if (tick > 0 && tick % 20 == 0) sheep.setColor(dc == DyeColor.RED ? DyeColor.WHITE : DyeColor.RED); //TODO: gyorsulas
    }

    @Override
    public void explode(Location loc){
        loc = owner.getLocation();
        final World world = loc.getWorld();
        final Vector dir = loc.getDirection().normalize();
        var fLoc = loc.add(dir.multiply(1.5));
        fLoc.setY(loc.getY()+1.8);
        Collection<Entity> hits = world.getNearbyEntities(sheep.getBoundingBox()); //Get entity collisions
        final var nmsSh = ((CraftEntity) sheep).getHandle(); //We check block collisions via NMS
        final Location shLoc = sheep.getLocation();
        if(nmsSh.horizontalCollision || nmsSh.verticalCollision || hits.size() != 1 && !hits.contains(owner)) { //We reg a hit here.
            sheep.remove();
            shLoc.createExplosion(sheep,3F, false);
            world.spawnParticle(Particle.EXPLOSION, shLoc, 1);
            world.playSound(shLoc, Sound.ENTITY_GENERIC_EXPLODE, 4F,0.7F); //In MC pitch is random betw: 0.56-0.84
        }
        sheep.setVelocity(dir.multiply(speed));
    }

    @Override
    public void giveSheep(Player player){
        player.getInventory().addItem(explSheep);
    }
}
