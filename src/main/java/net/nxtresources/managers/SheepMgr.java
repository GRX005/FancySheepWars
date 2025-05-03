package net.nxtresources.managers;

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

import java.util.Collection;

public class SheepMgr {

    public static void giveSheep(Player p) {
        ItemStack sheep = new ItemBuilder(Material.PLAYER_HEAD)
                .setDisplayName("§aExplosive Sheep")
                .setPD("expl")
                .setSkin("ewogICJ0aW1lc3RhbXAiIDogMTcwOTE2NTY1MjEwNywKICAicHJvZmlsZUlkIiA6ICJlZTg4M2RmMjM0ZWI0YWM1YTFlNDEwODhhYzZkZWIxNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJUdW5lc0Jsb2NrIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdjNjZjZmQ5ZjNhNzY1YThjZTUwZDdkNDEzZGZjMjRmMjg2NTU2YzJiNmFkNTA5Njg5OGNmYTkzODBjMzkxNjkiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==","q7WLkV0hjPTeKSU5hCQV3y38EBZGL6T2c/mN7ugqo21+wylRGUHPv1If9ubgDZpGYc7hYfoa/TCcPCMkuWZ2Df3o8TIUFmj+vlnduOyrOekITr9crqjioGu6O3DDngB719XYzDeVzrr5420oBPe5TiuqAg3osUjppDr0eCJUXCen69decD9hErmKJvsQn/XCkVOQZSA0h+hgj9zViJxOo57ITmwuV29Ha2zr4g0bNlEoCr9wP/mrA1yWBWvqFmcd1Yh2EWd/22uMSjsbwt7vUnltqyU7++mS6PXcBNsKgE18zUlA2aP/4J+//6g0rxQNjnG8samC75RI5qyIAX4C2el4od7iH/EoPmbnR0BchRzdSVOU19nVhfRyFpzY55vUznmX/FG7Tn+VqO1qhjyfc3ZOto9v5lM5SXGzb1FR1j3j9m+Fef71dYPNZn5bSOve6K0wi1jmuZYL4B1HMiDApckWQXlV7NQPAumrz7PsLaoiE8F01GJUr7/AW8Zeh7Kk5uh4zoJoczYQ7eR1cjgwErUJR3GlwerB2fHB9zO7ffDWwlsqWJcuFvaNzOHuzdItOJ22ULqD22HYJG4vO5gQzvXqg/FkzBTnaQ0k8HUAonpwO97FihG5WLZuA1/uH7RKljMrVosaUV5Kgtd9UIYSa8fUjxVWcOPWTESuFkVgdm0=")
                .build();
        p.getInventory().addItem(sheep);

    }

    public static void shootSheep(Location loc, Player p) {
        final World world = loc.getWorld();
        world.spawn(loc.add(loc.getDirection().normalize().multiply(1.5)), Sheep.class, sh -> {
            sh.setColor(DyeColor.RED);
            sh.customName(Component.text("Explosive Sheep", NamedTextColor.RED));
            sh.setCustomNameVisible(true); //Name visible all the time, not just when entity in aim
            Bukkit.getMobGoals().removeAllGoals(sh); //Instead of setAI false, so it can still move but it won't
            sh.setGravity(false);
            var dir = loc.getDirection().normalize();
            final var sp = 1.0;
            final var nmsSh = ((CraftEntity) sh).getHandle(); //We check block collisions via NMS
            Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task -> {
                if(sh.isDead() || !sh.isValid())
                    task.cancel();
                Collection<Entity> hits = world.getNearbyEntities(sh.getBoundingBox()); //Get entity collisions
                if(nmsSh.horizontalCollision || nmsSh.verticalCollision || hits.size() != 1 && !hits.contains(p)) {
                    sh.remove();
//                    int blockRadius = 3;
//                    // Manually break blocks in a sphere without visual effects
//                    for (int x = -blockRadius; x <= blockRadius; x++)
//                        for (int y = -blockRadius; y <= blockRadius; y++)
//                            for (int z = -blockRadius; z <= blockRadius; z++)
//                                if (x*x + y*y + z*z <= blockRadius * blockRadius)
//                                    world.getBlockAt(sh.getLocation().clone().add(x, y, z)).breakNaturally(false);
                    sh.getLocation().createExplosion(sh,3F, false);
                    world.spawnParticle(Particle.EXPLOSION, sh.getLocation(), 1);
                    world.playSound(sh.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 4F,0.7F); //In MC pitch is random betw: 0.56-0.84
                    task.cancel();
                }
                sh.setVelocity(dir.multiply(sp));
            },0L, 1L);
        });
    }

}
