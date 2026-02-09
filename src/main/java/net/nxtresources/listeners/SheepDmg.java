package net.nxtresources.listeners;

import org.bukkit.entity.Player;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;

public class SheepDmg implements Listener {

    @EventHandler
    public void SheepExpl(EntityDamageByEntityEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION ||
                !(e.getEntity() instanceof Player p) ||
                !(e.getDamager() instanceof Sheep s)) return;

        Vector v = p.getLocation().toVector().subtract(s.getLocation().toVector());
        double dist = v.length();
        e.setCancelled(true);
        // Combined Normalization + Scaling: v * (val * 0.4 / d)
        if (dist > 0.01) {
            // 1. Multiplied the horizontal scaling logic by 0.7 to reduce outward force
            // 2. Increased .setY() from 0.5 to 0.9 to increase upward force
            p.setVelocity(v.multiply((Math.max(1.0, 3.5 - dist * 0.5) / dist)).setY(0.7));
        } else {
            // Updated fallback to match new vertical strength
            p.setVelocity(new Vector(0, 0.7, 0));
        }
        var dmg = Math.max(1, Math.min(5, 6.2 - dist));
        // Damage: Buffer of 6.2 ensures you actually hit 5.0 damage
        // even when colliding with the sheep (approx dist 1.2)
        p.damage(dmg);
        p.sendMessage(String.valueOf(dmg));
    }

}
