package net.nxtresources.listeners;

import net.nxtresources.Main;
import net.nxtresources.enums.SheepType;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.ItemMgr;
import net.nxtresources.managers.SetupMgrNew;
import net.nxtresources.menus.ArenaSelectorGui;
import net.nxtresources.sheeps.FancySheep;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
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

import static org.bukkit.Material.PLAYER_HEAD;

public class InteractEvent implements Listener {

    @EventHandler
    public void openStatusMenu(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack item = player.getInventory().getItemInMainHand();
        ItemMeta meta = item.getItemMeta();

        if (item.getType() == Material.AIR) return;
        if(meta ==null || !meta.hasDisplayName()) return;

        if (item.getType()==PLAYER_HEAD) {//Handle sheep here
            var key = item.getItemMeta().getPersistentDataContainer().get(Main.shKey, PersistentDataType.STRING);
            SheepType type;
            type = SheepType.valueOf(key);
            FancySheep sheep = FancySheep.create(type, player);
            sheep.movement(action.isRightClick());
            player.playSound(
                    player.getLocation(),
                    Sound.ENTITY_SHEEP_AMBIENT, // or another sheep-related sound
                    1.0f,                       // volume
                    1.0f                        // pitch
            );
            return;
        }

        if(!(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)) return;

        var pdc = meta.getPersistentDataContainer().get(Main.itemData, PersistentDataType.STRING);
        switch (item.getType()) {
            case BOOK -> {
                if ("ArenaSelector".equals(pdc)) ArenaSelectorGui.open(player);
            }
            case DARK_OAK_DOOR -> {
                if ("SetWaitingLobby".equals(pdc)) {
                    //String name = SetupMgr.getSetupArena(player);
                    var session = SetupMgrNew.sessions.get(player.getUniqueId());
                    session.temp.waitingLobby = player.getLocation();
                    Main.getSetupMgr().checkStep(player);
                    player.sendMessage("§aVárakozó lobby beállítva! (§2" + session.arenaName + "§a)");
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
                    SetupMgrNew.finish(player, false);
                    player.sendMessage("§cKiléptél a setup módból!");
                }
            }
            case EMERALD_BLOCK -> {
                if ("SaveAndExit".equals(pdc)) {
                    SetupMgrNew.finish(player, true);
                    player.sendMessage("§aAréna sikeresen létrehozva és mentve!");
                    event.setCancelled(true);
                }
            }
            case BLUE_WOOL -> {
                if ("TeamSelector_Blue".equals(pdc)) {
                    var session = SetupMgrNew.sessions.get(player.getUniqueId());
                    session.temp.teamSpawns.put("BLUE", player.getLocation());
                    player.sendMessage("§9Kék §fcsapat beállítva!");
                    Main.getSetupMgr().checkStep(player);
                    event.setCancelled(true);
                }
            }
            case RED_WOOL -> {
                if ("TeamSelector_Red".equals(pdc)) {
                    var session = SetupMgrNew.sessions.get(player.getUniqueId());
                    session.temp.teamSpawns.put("RED", player.getLocation());
                    player.sendMessage("§cPiros §fcsapat beállítva!");
                    Main.getSetupMgr().checkStep(player);
                    event.setCancelled(true);
                }
            }
            case WOODEN_AXE -> {
                if ("MapSelector".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND) return;
                    var session = SetupMgrNew.sessions.get(player.getUniqueId());
                    var temp = session.temp;
                    Location loc = player.getLocation();

                    if (temp.pos1 == null) {
                        temp.pos1 = loc;
                        player.sendMessage("Arena pos1 beállítva! loc: " + loc);
                    } else {
                        temp.pos2 = loc;
                        player.sendMessage("Arena pos2 beállítva! loc: " + loc);
                    }
                    Main.getSetupMgr().checkStep(player);
                    event.setCancelled(true);
                }
            }
//TODO Make based on team arena
            case RED_CONCRETE -> {
                if("SetRedSheep".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND) return;
                    var session = SetupMgrNew.sessions.get(player.getUniqueId());
                    var temp = session.temp;
                    Block block = event.getClickedBlock();
                    if (block == null) {
                        player.sendMessage("Blockra kell kattintanod!");
                        return;
                    }
                    Location loc = block.getLocation().add(0.5, 1, 0.5);
                    temp.redSheepSpawns.add(loc);
                    player.sendMessage("§cPiros csapat barany spawn loc beallitva: " + (int)loc.getX() + "," + (int)loc.getY() + "," + (int)loc.getZ());
                    event.setCancelled(true);
                }
            }

            case BLUE_CONCRETE -> {
                if("SetBlueSheep".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND) return;
                    var session = SetupMgrNew.sessions.get(player.getUniqueId());
                    var temp = session.temp;
                    Block block = event.getClickedBlock();
                    if (block == null) {
                        player.sendMessage("Blockra kell kattintanod!");
                        return;
                    }
                    Location loc = block.getLocation().add(0.5, 1, 0.5);
                    temp.blueSheepSpawns.add(loc);
                    player.sendMessage("§9Kék csapat barany spawn loc beallitva: " + (int)loc.getX() + "," + (int)loc.getY() + "," + (int)loc.getZ());
                    event.setCancelled(true);
                }
            }

            case GOLDEN_AXE -> {
                if ("WaitingLobbySelector".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND) return;
                    var session = SetupMgrNew.sessions.get(player.getUniqueId());
                    var temp = session.temp;
                    Location loc = player.getLocation();

                    if (temp.waitingPos1 == null && temp.waitingPos2 == null) {
                        temp.waitingPos1 = loc;
                        player.sendMessage("Waiting pos1 beállítva! loc: " + loc);
                    } else {
                        temp.waitingPos2 = loc;
                        player.sendMessage("Waiting pos2 beállítva! loc: " + loc);
                    }
                    Main.getSetupMgr().checkStep(player);
                    event.setCancelled(true);
                }
            }
            default -> {}
        }
    }
}