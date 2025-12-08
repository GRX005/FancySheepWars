package net.nxtresources.sheeps.types;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.sheeps.FancySheep;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;

import java.util.Objects;

import static net.nxtresources.managers.ItemMgr.healingSheep;

public class HealingSheep extends FancySheep {

    final Player owner;
    int max = 200;
    int happyVillager = 45;
    int ticksRun = 0;
    public HealingSheep(Player owner) {
        super(SheepType.HEALING, owner);
        this.owner=owner;
        damage=0;
        radius=5;
    }

    @Override
    public void giveSheep(Player player){
        player.getInventory().addItem(healingSheep);
    }


    @Override
    public void customize(Sheep sheep){
        //Location loc = sheep.getLocation();
        sheep.setGravity(true);
        sheep.setColor(DyeColor.PINK);
        sheep.customName(Component.text("Healing Sheep", NamedTextColor.LIGHT_PURPLE));
        sheep.setCustomNameVisible(true); //Name visible all the time, not just when entity in aim

        //owner.spawnParticle(Particle.FLAME, loc.clone().add((Math.random() - 0.5) * 0.3,(Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3), 5, 0.01, 0.01, 0.01, 0.01);

    }

    @Override
    public void spawnLaunchParticle(Location shLoc){
        for (int i = 0; i < 3; i++) sheep.getWorld().spawnParticle(Particle.HEART, shLoc.clone().add((Math.random() - 0.5) * 0.3,(Math.random() - 0.5) * 0.3, (Math.random() - 0.5) * 0.3), 5, 0.01, 0.01, 0.01, 0.01);
    }

    @Override
    public void explode() {
        ticksRun = 0;
        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), expl ->{
            final int radiusPoints = 16;
            if(ticksRun>=max || sheep.isDead()) {
                if (sheep != null && !sheep.isDead()) sheep.remove();
                expl.cancel();
                return;
            }

            var loc = sheep.getLocation();

            //else location = loc.clone();
            final World w = loc.getWorld();
            if(w==null){
                expl.cancel();
                return;
            }
            w.spawnParticle(Particle.HEART, loc, 2, 2, 2, 2, 1);
            if(ticksRun < max - happyVillager) {
                for (int i = 0; i < radiusPoints; i++) {
                    var angle = 2 * Math.PI * i / radiusPoints;
                    var x = loc.getX() + radius * Math.cos(angle);
                    var z = loc.getZ() + radius * Math.sin(angle);
                    var y = loc.getY() + 0.1;
                    Location particleLoc = new Location(w, x, y, z);
                    w.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 1, 0.0, 0.0, 0.0, 0.0);
                    w.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 1, 0.0, 0.2, 0.0, 0.0);
                    w.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 1, 0.0, 0.4, 0.0, 0.0);

                }
            }
            for (Entity e : w.getNearbyEntities(loc, radius, radius, radius)) {
                if (e instanceof Player player) {
                    var maxHp = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
                    var newHp = Math.min(player.getHealth() + 0.5, maxHp);
                    player.setHealth(newHp);
                    player.spawnParticle(Particle.HEART, loc, 3, 2, 2, 2, 1);
                }
            }
            ticksRun += 10;
        }, 0L, 10L);
    }
}
