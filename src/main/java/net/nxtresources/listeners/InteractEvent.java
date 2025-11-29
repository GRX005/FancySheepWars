package net.nxtresources.listeners;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.managers.*;
import net.nxtresources.menus.ArenaSelectorGui;
import net.nxtresources.sheeps.FancySheep;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

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

        var pdc = meta.getPersistentDataContainer().get(Main.itemData, PersistentDataType.STRING);
        switch (item.getType()) {
            case BOOK -> {
                if ("ArenaSelector".equals(pdc)) ArenaSelectorGui.open(player);
            }
            case DARK_OAK_DOOR -> {
                if ("SetWaitingLobby".equals(pdc)) {
                    String name = SetupMgr.getSetupArena(player);
                    SetupMgr.setWaitingLobby(player);
                    player.sendMessage("§aVárakozó lobby beállítva! (§2" + name + "§a)");
                    event.setCancelled(true);
                }
            }
            case BARRIER -> {
                if("LeaveArena".equals(pdc)){
                    if (!ArenaMgr.isInArena(player)) {
                        player.sendMessage("Nem vagy arenaban.");
                        return;
                    }
                    player.sendMessage("§cKiléptél az arénából.");
                    player.getInventory().clear();
                    ItemMgr.lobbyItems(player);
                    ArenaMgr.leave(player);
                    return;
                }
                if("LeaveSetup".equals(pdc)){
                    SetupMgr.finishSetup(player, false);
                    player.getInventory().clear();
                    player.sendMessage("§cKiléptél a setup módból!");
                }
            }
            case EMERALD_BLOCK -> {
                if ("SaveAndExit".equals(pdc)) {
                    SetupMgr.finishSetup(player, true);
                    player.sendMessage("§aAréna sikeresen létrehozva és mentve!");
                    event.setCancelled(true);
                }
            }
            case BLUE_WOOL -> {
                if ("TeamSelector_Blue".equals(pdc)) {
                    Arena.Temp tempData = SetupMgr.tempdata.get(player.getUniqueId());
                    if (tempData != null)
                        tempData.teamSpawns.put("BLUE", player.getLocation());
                    player.sendMessage("§9Kék §fcsapat beállítva!");
                    event.setCancelled(true);
                }
            }
            case RED_WOOL -> {
                if ("TeamSelector_Red".equals(pdc)) {
                    Arena.Temp tempData = SetupMgr.tempdata.get(player.getUniqueId());
                    if (tempData != null)
                        tempData.teamSpawns.put("RED", player.getLocation());
                    player.sendMessage("§cPiros §fcsapat beállítva!");
                    event.setCancelled(true);
                }
            }
            case PLAYER_HEAD -> {
                var key = item.getItemMeta().getPersistentDataContainer().get(Main.shKey, PersistentDataType.STRING);
                SheepType type;
                type = SheepType.valueOf(key);
                FancySheep sheep = FancySheep.create(type, player);
                sheep.movement();
            }
            case WOODEN_AXE -> {
                if ("MapSelector".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND) return;
                    Arena.Temp temp = SetupMgr.tempdata.get(player.getUniqueId());
                    Location loc = player.getLocation();

                    if (temp.pos1 == null) {
                        temp.pos1 = loc;
                        player.sendMessage("Arena pos1 beállítva! loc: " + loc);
                    } else {
                        temp.pos2 = loc;
                        player.sendMessage("Arena pos2 beállítva! loc: " + loc);
                    }

                    event.setCancelled(true);
                }
            }

            case ORANGE_WOOL -> {
                if("SetSheep".equals(pdc)){
                    Block block = event.getClickedBlock();
                    Player p = event.getPlayer();
                    Location playerLoc = p.getLocation();
                    if(block == null) {
                        player.sendMessage("Blockra kell kattintanod!");
                        return;
                    }
                    Arena.Temp temp = SetupMgr.tempdata.get(p.getUniqueId());
                    Location loc = block.getLocation().add(0.5, 1, 0.5);
                    Location blue = temp.teamSpawns.get("BLUE");
                    Location red = temp.teamSpawns.get("RED");
                    if(blue==null||red==null){
                        player.sendMessage("A csapatok nincsenek megfelelően beállítva! blue: " + blue + " red: " + red);
                        return;
                    }
                    var blued = playerLoc.distanceSquared(blue);
                    var redb = playerLoc.distanceSquared(red);
                    if (blued <= redb) {
                        temp.blueSheepSpawns.add(loc);
                        p.sendMessage("§9Kék csapat barany spawn loc beallitva: " + (int)loc.getX() + "," + (int)loc.getY() + "," + (int)loc.getZ());
                    } else {
                        temp.redSheepSpawns.add(loc);
                        p.sendMessage("§cPiros csapat barany spawn loc beallitva: " + (int)loc.getX() + "," + (int)loc.getY() + "," + (int)loc.getZ());
                    }
                    event.setCancelled(true);
                }
            }
            case GOLDEN_AXE -> {
                if ("WaitingLobbySelector".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND)
                        return;

                    Arena.Temp temp = SetupMgr.tempdata.get(player.getUniqueId());
                    Location loc = player.getLocation();

                    if (temp.waitingPos1 == null && temp.waitingPos2 == null) {
                        temp.waitingPos1 = loc;
                        player.sendMessage("Waiting pos1 beállítva! loc: " + loc);
                    } else {
                        temp.waitingPos2 = loc;
                        player.sendMessage("Waiting pos2 beállítva! loc: " + loc);
                    }
                    event.setCancelled(true);
                }
            }
            default -> {
            }
        }
    }
}