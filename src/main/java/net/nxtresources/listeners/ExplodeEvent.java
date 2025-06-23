package net.nxtresources.listeners;

import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

public class ExplodeEvent implements Listener {
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
//        e.blockList().forEach(Block::breakNaturally); //TODO Make only for sheep
//        e.setCancelled(true);
    }
}
