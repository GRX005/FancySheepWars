package net.nxtresources.listeners;

import net.nxtresources.ItemBuilder;
import net.nxtresources.managers.DataMgr;
import net.nxtresources.managers.ItemMgr;
import net.nxtresources.managers.SetupMgr;
import net.nxtresources.managers.SheepMgr;
import net.nxtresources.sheeps.ExplSheep;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class JoinAndQuitEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DataMgr.get(event.getPlayer());
        player.getInventory().clear();
        ItemMgr.lobbyItems(player);
        SetupMgr.tpToLobby(player);
        SheepMgr.giveSheep(new ExplSheep(), player);

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        DataMgr.save();
    }

}
