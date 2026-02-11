package net.nxtresources.listeners;

import net.nxtresources.Main;
import net.nxtresources.enums.SetupStep;
import net.nxtresources.enums.SheepType;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.ItemMgr;
import net.nxtresources.managers.SetupMgr;
import net.nxtresources.menus.ArenaSelectorGui;
import net.nxtresources.sheeps.FancySheep;
import net.nxtresources.utils.MsgCache;
import net.nxtresources.utils.Utils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

import static org.bukkit.Material.PLAYER_HEAD;

public class InteractEvent implements Listener {

    Map<String, String> formattedList = Map.of(
            "RED", "Red",
            "BLUE", "Blue"
    );

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
            SheepType type = SheepType.valueOf(key);
            FancySheep sheep = FancySheep.create(type, player);
            sheep.shoot(action.isRightClick());
            player.playSound(
                    player.getLocation(),
                    Sound.ENTITY_SHEEP_AMBIENT, // or another sheep-related sound
                    1.0f,                       // volume
                    1.0f                        // pitch
            );
            event.setCancelled(true);
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
                    var session = SetupMgr.sessions.get(player.getUniqueId());
                    session.temp.waitingLobby = player.getLocation();
                    if(session.step != SetupStep.MAP_REGION){
                        if(Utils.isOutsideRegion(player.getLocation(), session.temp.pos1, session.temp.pos2)) {
                            player.sendMessage(Main.color(MsgCache.get("Arena.Setup.InsideOnly").replace("%step%", "MAP_REGION")));
                            event.setCancelled(true);
                            return;
                        }
                        if(Utils.isOutsideRegion(player.getLocation(), session.temp.waitingPos1, session.temp.waitingPos2)) {
                            player.sendMessage(Main.color(MsgCache.get("Arena.Setup.InsideOnly").replace("%step%", "WAITING_LOBBY_REGION")));
                            event.setCancelled(true);
                            return;
                        }
                    }
                    Main.getSetupMgr().checkStep(player);
                    player.sendMessage(Main.color(MsgCache.get("Arena.Setup.setWaitingLobby").replace("%arena_name%", session.arenaName)));
                    event.setCancelled(true);
                }
            }
            case BARRIER -> {
                if("LeaveArena".equals(pdc)){
                    if (!ArenaMgr.isInArena(player)) {
                        player.sendMessage(Main.color(MsgCache.get("Arena.NotInAnArena")));
                        return;
                    }
                    player.sendMessage(Main.color(MsgCache.get("Arena.Leave")));
                    player.getInventory().clear();
                    ItemMgr.lobbyItems(player);
                    ArenaMgr.leave(player);
                    return;
                }
                if("LeaveSetup".equals(pdc)){
                    SetupMgr.finish(player, false);
                    player.sendMessage(Main.color(MsgCache.get("Arena.Setup.LeaveSetup")));
                }
            }
            case EMERALD_BLOCK -> {
                var session = SetupMgr.sessions.get(player.getUniqueId());
                if (Utils.clickTick(player)) {
                    event.setCancelled(true);
                    return;
                }
                if(!SetupMgr.isSetupComplete(session.temp)){
                    player.sendMessage(Main.color(MsgCache.get("Arena.Setup.StepsNotComplete")));
                    return;
                }
                if ("SaveAndExit".equals(pdc)) {
                    SetupMgr.finish(player, true);
                    player.sendMessage(Main.color(MsgCache.get("Arena.Setup.ArenaCreated").replace("%arena_name%", session.arenaName)));
                    event.setCancelled(true);
                }
            }
            case BLUE_WOOL -> {
                if (Utils.clickTick(player)) {
                    event.setCancelled(true);
                    return;
                }
                if ("TeamSelector_Blue".equals(pdc)) {
                    var session = SetupMgr.sessions.get(player.getUniqueId());
                    session.temp.teamSpawns.put("BLUE", player.getLocation());
                    if(session.step != SetupStep.MAP_REGION){
                        if(Utils.isOutsideRegion(player.getLocation(), session.temp.pos1, session.temp.pos2)) {
                            player.sendMessage(Main.color(MsgCache.get("Arena.Setup.InsideOnly").replace("%step%", "MAP_REGION")));
                            event.setCancelled(true);
                            return;
                        }
                    }
                    player.sendMessage(Main.color(MsgCache.get("Arena.Setup.setTeamSpawn").replace("%team%", formattedList.get("BLUE"))));
                    Main.getSetupMgr().checkStep(player);
                    event.setCancelled(true);
                }
            }
            case RED_WOOL -> {
                if (Utils.clickTick(player)) {
                    event.setCancelled(true);
                    return;
                }
                if ("TeamSelector_Red".equals(pdc)) {
                    var session = SetupMgr.sessions.get(player.getUniqueId());
                    session.temp.teamSpawns.put("RED", player.getLocation());
                    if(session.step != SetupStep.MAP_REGION){
                        if(Utils.isOutsideRegion(player.getLocation(), session.temp.pos1, session.temp.pos2)) {
                            player.sendMessage(Main.color(MsgCache.get("Arena.Setup.InsideOnly").replace("%step%", "MAP_REGION")));
                            event.setCancelled(true);
                            return;
                        }
                    }
                    player.sendMessage(Main.color(MsgCache.get("Arena.Setup.setTeamSpawn").replace("%team%", formattedList.get("RED"))));
                    Main.getSetupMgr().checkStep(player);
                    event.setCancelled(true);
                }
            }
            case WOODEN_AXE -> {
                if (Utils.clickTick(player)) {
                    event.setCancelled(true);
                    return;
                }
                var session = SetupMgr.sessions.get(player.getUniqueId());
                if ("MapSelector".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND) return;
                    var temp = session.temp;
                    Location loc = player.getLocation();

                    if (temp.pos1 == null) {
                        temp.pos1 = loc;
                        player.sendMessage(Main.color(MsgCache.get("Arena.Setup.MapRegionSelectorPOS1")
                                .replace("%x%", String.format("%.2f", player.getX()))
                                .replace("%y%", String.format("%.2f", player.getY()))
                                .replace("%z%", String.format("%.2f", player.getZ()))
                        ));
                    } else {
                        temp.pos2 = loc;
                        player.sendMessage(Main.color(MsgCache.get("Arena.Setup.MapRegionSelectorPOS2")
                                .replace("%x%", String.format("%.2f", player.getX()))
                                .replace("%y%", String.format("%.2f", player.getY()))
                                .replace("%z%", String.format("%.2f", player.getZ()))
                        ));
                        Utils.drawDust(
                                temp.pos1, temp.pos2,
                                Color.RED, Color.PURPLE,
                                Math.min(temp.pos1.getY(), temp.pos2.getY()),
                                Math.max(temp.pos1.getY(), temp.pos2.getY()), player.getUniqueId());
                    }
                    Main.getSetupMgr().checkStep(player);
                    event.setCancelled(true);
                }
            }
//TODO Make based on team arena
            case RED_CONCRETE -> {
                if (Utils.clickTick(player)) {
                    event.setCancelled(true);
                    return;
                }
                if("SetRedSheep".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND) return;
                    var session = SetupMgr.sessions.get(player.getUniqueId());
                    var temp = session.temp;
                    if(session.step != SetupStep.MAP_REGION){
                        if(Utils.isOutsideRegion(player.getLocation(), session.temp.pos1, session.temp.pos2)) {
                            player.sendMessage(Main.color(MsgCache.get("Arena.Setup.InsideOnly").replace("%step%", "MAP_REGION")));
                            event.setCancelled(true);
                            return;
                        }
                    }
                    Location playerLoc = player.getLocation().add(0.5, 1, 0.5);
                    temp.redSheepSpawns.add(playerLoc);
                    player.sendMessage(Main.color(MsgCache.get("Arena.Setup.setSheepSpawn").replace("%team%", MsgCache.get("Arena.Teams.Red"))));
                    event.setCancelled(true);
                }
            }

            case BLUE_CONCRETE -> {
                if (Utils.clickTick(player)) {
                    event.setCancelled(true);
                    return;
                }
                if("SetBlueSheep".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND) return;
                    var session = SetupMgr.sessions.get(player.getUniqueId());
                    var temp = session.temp;
                    if(session.step != SetupStep.MAP_REGION){
                        if(Utils.isOutsideRegion(player.getLocation(), session.temp.pos1, session.temp.pos2)) {
                            player.sendMessage(Main.color(MsgCache.get("Arena.Setup.InsideOnly").replace("%step%", "MAP_REGION")));
                            event.setCancelled(true);
                            return;
                        }
                    }
                    Location playerLoc = player.getLocation().add(0.5, 1, 0.5);
                    temp.blueSheepSpawns.add(playerLoc);
                    player.sendMessage(Main.color(MsgCache.get("Arena.Setup.setSheepSpawn").replace("%team%", MsgCache.get("Arena.Teams.Blue"))));
                    event.setCancelled(true);
                }
            }

            case GOLDEN_AXE -> {
                if (Utils.clickTick(player)) {
                    event.setCancelled(true);
                    return;
                }
                if ("WaitingLobbySelector".equals(pdc)) {
                    if (event.getHand() != EquipmentSlot.HAND) return;
                    var session = SetupMgr.sessions.get(player.getUniqueId());
                    var temp = session.temp;
                    Location loc = player.getLocation();

                    if(session.step != SetupStep.MAP_REGION){
                        if(Utils.isOutsideRegion(player.getLocation(), session.temp.pos1, session.temp.pos2)) {
                            player.sendMessage(Main.color(MsgCache.get("Arena.Setup.InsideOnly").replace("%step%", "MAP_REGION")));
                            return;
                        }
                    }

                    if (temp.waitingPos1 == null && temp.waitingPos2 == null) {
                        temp.waitingPos1 = loc;
                        player.sendMessage(Main.color(MsgCache.get("Arena.Setup.WaitingLobbyRegionSelectorPOS1")
                                .replace("%x%", String.format("%.2f", player.getX()))
                                .replace("%y%", String.format("%.2f", player.getY()))
                                .replace("%z%", String.format("%.2f", player.getZ()))
                        ));
                    } else {
                        temp.waitingPos2 = loc;

                        if(session.step != SetupStep.MAP_REGION){
                            if(Utils.isOutsideRegion(player.getLocation(), session.temp.pos1, session.temp.pos2)) {
                                player.sendMessage(Main.color(MsgCache.get("Arena.Setup.InsideOnly")));
                                return;
                            }
                        }

                        player.sendMessage(Main.color(MsgCache.get("Arena.Setup.WaitingLobbyRegionSelectorPOS2")
                                .replace("%x%", String.format("%.2f", player.getX()))
                                .replace("%y%", String.format("%.2f", player.getY()))
                                .replace("%z%", String.format("%.2f", player.getZ()))
                        ));
                        Utils.drawDust(
                                temp.waitingPos1, temp.waitingPos2,
                                Color.ORANGE, Color.YELLOW,
                                Math.min(temp.waitingPos1.getY(), temp.waitingPos2.getY()),
                                Math.max(temp.waitingPos1.getY(), temp.waitingPos2.getY()), player.getUniqueId());
                    }
                    Main.getSetupMgr().checkStep(player);
                    event.setCancelled(true);
                }
            }
            default -> {}
        }
    }
}