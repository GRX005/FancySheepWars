package net.nxtresources.managers.scoreboard;

import net.nxtresources.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BoardMgr {

//    private static final Map<UUID, BaseBoard> boards = new HashMap<>();
    private static final Map<UUID, BaseBoard> boards = new HashMap<>();
    private static final Map<Class<? extends BaseBoard>, Runnable> updateTasks = new HashMap<>();

    public static void setBoard(Player player, BaseBoard board) {
        UUID uuid = player.getUniqueId();
        if (boards.containsKey(uuid))
            boards.get(uuid).cancel();
        boards.put(uuid, board);
        board.build(player);
        startUpdater(board.getClass(), 20L);
    }

    public static void startUpdater(Class<? extends BaseBoard> type, long ticks) {
        if (updateTasks.containsKey(type))
            return;

        Runnable task = () -> {
            for (Map.Entry<UUID, BaseBoard> entry : boards.entrySet()) {
                if (entry.getValue().getClass().equals(type)) {
                    Player p = Bukkit.getPlayer(entry.getKey());
                    if (p != null &&p.isOnline())
                        entry.getValue().update(p);
                }
            }
        };
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), task, 0L, ticks);
        updateTasks.put(type, task);
    }
}
