package net.nxtresources.listeners;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

import java.util.Objects;

public class InteractEvent implements Listener {

    @EventHandler
    public void openStatusMenu(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        if(!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;
        if (item.getType() == Material.AIR) return;
        if(meta ==null || !meta.hasDisplayName()) return;

        String displayName = LegacyComponentSerializer.legacySection().serialize(Objects.requireNonNull(meta.displayName()));

        switch (item.getType()) {
            case BOOK -> {
                if (displayName.equals("§eAréna választó")) {
                    ArenaSelectorGui.open(player);
                }
            }
            case DARK_OAK_DOOR -> {
                if (displayName.equals("§aVárakozó lobby beállítása")) {
                    String name = SetupManager.getSetupArena(player);
                    SetupManager.setWaitingLobby(player);
                    player.sendMessage("§aVárakozó lobby beállítva! (§2" + name + "§a)");
                    event.setCancelled(true);
                }
            }
            case BARRIER -> {
                switch (displayName) {
                    case "§eAréna elhagyása" -> {
                        if (!ArenaMgr.isInArena(player)) {
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
                }
                event.setCancelled(true);
            }
            case EMERALD_BLOCK -> {
                if (displayName.equals("§aMentés és kilépés a setup módból")) {
                    SetupManager.finishSetup(player, true);
                    player.sendMessage("§aAréna sikeresen létrehozva és mentve!");
                    event.setCancelled(true);
                }
            }
            case BLUE_WOOL -> {
                if (displayName.equals("§9§lKÉK §fcsapat")) {
                    TemporaryArena tempData = SetupManager.tempdata.get(player.getUniqueId());
                    if (tempData != null)
                        tempData.teamSpawns.put("BLUE", player.getLocation());
                    player.sendMessage("§9Kék §fcsapat beállítva!");
                    event.setCancelled(true);
                }
            }
            case RED_WOOL -> {
                if (displayName.equals("§c§lPIROS §fcsapat")) {
                    TemporaryArena tempData = SetupManager.tempdata.get(player.getUniqueId());
                    if (tempData != null)
                        tempData.teamSpawns.put("RED", player.getLocation());
                    player.sendMessage("§cPiros §fcsapat beállítva!");
                    event.setCancelled(true);
                }
            }
            default -> {
            }
        }
    }
}