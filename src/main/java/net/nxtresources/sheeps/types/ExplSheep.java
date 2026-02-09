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
    public void spawnLaunchParticle(Location shLoc){
        World world = shLoc.getWorld();
        if (world == null) return;

        Vector velocity = sheep.getVelocity();

        // Normalize the velocity to get the direction the sheep is moving,
        // then invert it so we offset particles BEHIND the sheep
        if (velocity.lengthSquared() < 0.001) return; // not moving, skip

        Vector direction = velocity.clone().normalize();

        // Offset the particle origin to the center-back of the sheep
        // Sheep are ~0.9 blocks long; offset ~0.5 blocks backward from center
        double behindOffset = 0.5;
        Location trailOrigin = shLoc.clone().add(
                -direction.getX() * behindOffset,
                0.3, // slightly above ground level (sheep center height)
                -direction.getZ() * behindOffset
        );

        // Main flame trail - small cluster behind the sheep
        world.spawnParticle(
                Particle.FLAME,
                trailOrigin,
                3,              // 3 particles per tick — enough to look full, still cheap
                0.15, 0.15, 0.15, // slight random spread for natural look
                0.02            // very low extra speed so they linger in place
        );

        // Smaller smoke accent for depth — sits slightly further behind
        Location smokeOrigin = shLoc.clone().add(
                -direction.getX() * (behindOffset + 0.3),
                0.3,
                -direction.getZ() * (behindOffset + 0.3)
        );

        world.spawnParticle(
                Particle.SMOKE,
                smokeOrigin,
                2,
                0.1, 0.1, 0.1,
                0.01
        );

        // Occasional small lava drip for extra flair (every few ticks)
        if (sheep.getTicksLived() % 3 == 0) {
            world.spawnParticle(
                    Particle.LAVA,
                    trailOrigin,
                    1,
                    0.1, 0.1, 0.1,
                    0
            );
        }
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
