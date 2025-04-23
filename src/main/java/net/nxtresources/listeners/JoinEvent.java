package net.nxtresources.listeners;

import net.nxtresources.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

public class JoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        addLobbyItems(player);

    }

    public void addLobbyItems(Player player) {

        ItemStack arenaselector = new ItemBuilder(Material.BOOK).setDisplayName("§eAréna választó").setAmount(1).build();
        player.getInventory().setItem(4, arenaselector);


    }
}
