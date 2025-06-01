package net.nxtresources.sheeps;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.nxtresources.ItemBuilder;
import net.nxtresources.Main;
import org.bukkit.*;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.Collection;

public class ExplSheep implements FancySheep {
    @Override
    public void giveSheep(Player p) {
        ItemStack sheep = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("§aExplosive Sheep")
                .setPD("expl")
                .setSkin("ewogICJ0aW1lc3RhbXAiIDogMTcwOTE2NTY1MjEwNywKICAicHJvZmlsZUlkIiA6ICJlZTg4M2RmMjM0ZWI0YWM1YTFlNDEwODhhYzZkZWIxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUdW5lc0Jsb2NrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdjNjZjZmQ5ZjNhNzY1YThjZTUwZDdkNDEzZGZjMjRmMjg2NTU2YzJiNmFkNTA5Njg5OGNmYTkzODBjMzkxNjkiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==","q7WLkV0hjPTeKSU5hCQV3y38EBZGL6T2c/mN7ugqo21+wylRGUHPv1If9ubgDZpGYc7hYfoa/TCcPCMkuWZ2Df3o8TIUFmj+vlnduOyrOekITr9crqjioGu6O3DDngB719XYzDeVzrr5420oBPe5TiuqAg3osUjppDr0eCJUXCen69decD9hErmKJvsQn/XCkVOQZSA0h+hgj9zViJxOo57ITmwuV29Ha2zr4g0bNlEoCr9wP/mrA1yWBWvqFmcd1Yh2EWd/22uMSjsbwt7vUnltqyU7++mS6PXcBNsKgE18zUlA2aP/4J+//6g0rxQNjnG8samC75RI5qyIAX4C2el4od7iH/EoPmbnR0BchRzdSVOU19nVhfRyFpzY55vUznmX/FG7Tn+VqO1qhjyfc3ZOto9v5lM5SXGzb1FR1j3j9m+Fef71dYPNZn5bSOve6K0wi1jmuZYL4B1HMiDApckWQXlV7NQPAumrz7PsLaoiE8F01GJUr7/AW8Zeh7Kk5uh4zoJoczYQ7eR1cjgwErUJR3GlwerB2fHB9zO7ffDWwlsqWJcuFvaNzOHuzdItOJ22ULqD22HYJG4vO5gQzvXqg/FkzBTnaQ0k8HUAonpwO97FihG5WLZuA1/uH7RKljMrVosaUV5Kgtd9UIYSa8fUjxVWcOPWTESuFkVgdm0=")
                .build();
        p.getInventory().addItem(sheep);

    }

    @Override
    public void shootSheep(Player p) {//TODO ADD DEFAULT FOR COMMON?
        final Location loc = p.getLocation();
        final World world = loc.getWorld();
        final Vector dir = loc.getDirection().normalize();
        world.spawn(loc.add(dir.multiply(1.5)), Sheep.class, sh -> {
            sh.setColor(DyeColor.RED);
            sh.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
            sh.setCustomNameVisible(true); //Name visible all the time, not just when entity in aim
            Bukkit.getMobGoals().removeAllGoals(sh); //Instead of setAI false, so it can still move but it won't
            sh.setGravity(false);
            final var sp = 1.0;
            final var nmsSh = ((CraftEntity) sh).getHandle(); //We check block collisions via NMS
            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task -> {
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
                world.spawnParticle(Particle.FLAME, shLoc, 1);
                sh.setVelocity(dir.multiply(sp));
            },0L, 1L);
        });
    }
}
