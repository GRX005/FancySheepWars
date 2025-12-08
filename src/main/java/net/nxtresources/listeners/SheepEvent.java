package net.nxtresources.listeners;

import net.nxtresources.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.persistence.PersistentDataType;

public class SheepEvent implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent event){
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        var ent = event.getEntity();
        var pdc = ent.getPersistentDataContainer();
        if (!pdc.has(Main.shKey, PersistentDataType.STRING) && !(ent instanceof Player)) return;
        event.setCancelled(true);
    }


//    @EventHandler
//    public void onDamageByPlayer(EntityDamageByEntityEvent event){
//        var pdc = event.getEntity().getPersistentDataContainer();
//        if (!pdc.has(Main.shKey, PersistentDataType.STRING)) return;
//        if (event.getDamager() instanceof Player) event.setCancelled(true); //TODO: team system implementation
//    }
}
