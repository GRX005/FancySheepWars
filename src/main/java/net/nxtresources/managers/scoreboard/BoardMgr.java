package net.nxtresources.managers.scoreboard;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoardMgr {

    private static final Map<UUID, BaseBoard> boards = new HashMap<>();

    public static void setBoard(Player player, BaseBoard board) {
        UUID uuid = player.getUniqueId();
        if (boards.containsKey(uuid))
            boards.get(uuid).cancel();
        boards.put(uuid, board);
        board.build(player);
    }
}
