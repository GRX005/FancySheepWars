package net.nxtresources.listeners;

import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.DataManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathEvent implements Listener {

    @EventHandler
    public void onDeath(PlayerDeathEvent event){
        Player vic = event.getEntity();
        Player killer =vic.getKiller();
        DataManager.get(vic);
        DataManager.addDeath(vic);
        if(!ArenaMgr.isInArena(vic))
            return;
        if(killer!=null && killer!= vic)
            DataManager.addKill(killer);

    }
}
