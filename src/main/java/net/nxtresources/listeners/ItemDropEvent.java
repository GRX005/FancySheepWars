package net.nxtresources.listeners;

import net.nxtresources.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ItemDropEvent implements Listener {

    @EventHandler
    public void onDrop(PlayerDropItemEvent event){
        ItemStack dropped = event.getItemDrop().getItemStack();
        ItemMeta meta = dropped.getItemMeta();
        if (meta.getPersistentDataContainer().has(Main.itemData, PersistentDataType.STRING)) event.setCancelled(true);
    }
}
