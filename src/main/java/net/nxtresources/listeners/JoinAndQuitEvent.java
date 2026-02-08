package net.nxtresources.listeners;

import net.nxtresources.Main;
import net.nxtresources.enums.BoardType;
import net.nxtresources.enums.SheepType;
import net.nxtresources.managers.DataMgr;
import net.nxtresources.managers.ItemMgr;
import net.nxtresources.managers.LobbyMgr;
import net.nxtresources.managers.scoreboard.Board;
import net.nxtresources.managers.scoreboard.BoardMgr;
import net.nxtresources.sheeps.FancySheep;
import net.nxtresources.utils.MsgCache;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class JoinAndQuitEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        DataMgr.get(event.getPlayer());
        player.getInventory().clear();
        ItemMgr.lobbyItems(player);
        BoardMgr.setBoard(player, new Board(BoardType.LOBBY));

        FancySheep healing = FancySheep.create(SheepType.HEALING, player);
        healing.giveSheep(player);

        FancySheep explosive = FancySheep.create(SheepType.EXPLOSIVE, player);
        explosive.giveSheep(player);

        if(LobbyMgr.getLobbyLocation() ==null){
            player.sendMessage(Main.color(MsgCache.get("MainLobbyNotSet")));
            return;
        }
        LobbyMgr.tpMainLobby(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event){
        DataMgr.save();
    }
}
