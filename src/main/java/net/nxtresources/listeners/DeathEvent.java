package net.nxtresources.listeners;

import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.DataMgr;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        Player vic = event.getEntity();
        Player killer =vic.getKiller();
        DataMgr.get(vic);
        DataMgr.addDeath(vic);
        if(!ArenaMgr.isInArena(vic))
            return;
        if(killer!=null && killer!= vic)
            DataMgr.addKill(killer);

    }

//    @EventHandler
//    public void onSheepDeath(EntityDeathEvent e) {
//        if(e.getEntityType()==EntityType.SHEEP) {
//            e.setCancelled(true);
//        }
//    }
}
