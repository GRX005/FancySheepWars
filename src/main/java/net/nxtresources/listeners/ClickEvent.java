package net.nxtresources.listeners;

import net.nxtresources.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ClickEvent implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event){
        ItemStack clicked = event.getCurrentItem();
        if(clicked == null)
            return;
        ItemMeta meta = clicked.getItemMeta();
        if(meta == null)
            return;
        if (meta.getPersistentDataContainer().has(Main.itemData, PersistentDataType.STRING)) event.setCancelled(true);

    }

    @EventHandler
    public void onCreativeClick(InventoryCreativeEvent event){
        ItemStack clicked = event.getCurrentItem();
        if(clicked == null)
            return;
        ItemMeta meta = clicked.getItemMeta();
        if(meta == null)
            return;
        if (meta.getPersistentDataContainer().has(Main.itemData, PersistentDataType.STRING)) event.setCancelled(true);

    }
}
