package net.nxtresources.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.Main;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.SetupManager;
import net.nxtresources.managers.TemporaryArena;
import net.nxtresources.menus.ArenaSelectorGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class InteractEvent implements Listener {

    @EventHandler
    public void openStatusMenu(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getAction() != Action.RIGHT_CLICK_AIR && !(event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null))
            return;
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR)
            return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || meta.displayName() == null)//TODO Retartalt if check, switch, assert remove
            return;
        Component displayName = meta.displayName();

        if (item.getType() == Material.BOOK) {
            assert displayName != null;
            if ("§eAréna választó".equals(LegacyComponentSerializer.legacySection().serialize(displayName)))
                ArenaSelectorGui.open(player);
        }

        if (item.getType() == Material.DARK_OAK_DOOR) {
            assert displayName != null;
            if ("§aVárakozó lobby beállítása".equals(LegacyComponentSerializer.legacySection().serialize(displayName))) {
                String name = SetupManager.getSetupArena(player);
                SetupManager.setWaitingLobby(player);
                player.sendMessage("§aVárakozó lobby beállítva! (§2" + name + "§a)");
                event.setCancelled(true);
            }
        }
        if(item.getType()==Material.BARRIER){
            assert displayName != null;
            switch (LegacyComponentSerializer.legacySection().serialize(displayName)) {
                case "§eAréna elhagyása" -> {
                    if(!ArenaMgr.isInArena(player)){
                        player.sendMessage("Nem vagy arenaban.");
                        return;
                    }
                    player.sendMessage("§cKiléptél az arénából.");
                    player.getInventory().clear();
                    JoinAndQuitEvent.addLobbyItems(player);
                    ArenaMgr.leave(player);
                }
                case "§cSetup mód elhagyása" -> {
                    SetupManager.finishSetup(player, false);
                    player.getInventory().clear();
                    player.sendMessage("§cKiléptél a setup módból!");
                }
                default -> {
                }
            }
            event.setCancelled(true);
        }
        if(item.getType()==Material.EMERALD_BLOCK){
            assert displayName!=null;
            if("§aMentés és kilépés a setup módból".equals(LegacyComponentSerializer.legacySection().serialize(displayName))){
                SetupManager.finishSetup(player, true);
                player.sendMessage("§aAréna sikeresen létrehozva és mentve!");
                event.setCancelled(true);
            }
        }
        //TEAMS
        if(item.getType()==Material.BLUE_WOOL){
            assert displayName!=null;
            if("§9§lKÉK §fcsapat".equals(LegacyComponentSerializer.legacySection().serialize(displayName))){
                TemporaryArena tempData = SetupManager.tempdata.get(player.getUniqueId());
                if(tempData !=null)
                    tempData.teamSpawns.put("BLUE", player.getLocation());
                player.sendMessage("§9Kék §fcsapat beállítva!");
                event.setCancelled(true);
            }
        }
        if(item.getType()==Material.RED_WOOL){
            assert displayName!=null;
            if("§c§lPIROS §fcsapat".equals(LegacyComponentSerializer.legacySection().serialize(displayName))){
                TemporaryArena tempData = SetupManager.tempdata.get(player.getUniqueId());
                if(tempData !=null)
                    tempData.teamSpawns.put("RED", player.getLocation());
                player.sendMessage("§cPiros §fcsapat beállítva!");
                event.setCancelled(true);
            }
        }
    }
}