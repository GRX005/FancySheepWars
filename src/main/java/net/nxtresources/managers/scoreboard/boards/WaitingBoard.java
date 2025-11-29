package net.nxtresources.managers.scoreboard.boards;

import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.managers.Arena;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.MsgCache;
import net.nxtresources.managers.scoreboard.BaseBoard;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WaitingBoard extends BaseBoard {

    LinkedHashMap<Integer, String> sbwLines;
    List<String> sbTitles;
    int i=0;
    int taskId = -1;

    @Override
    public void build(Player player) {
        List<String> lines = MsgCache.getList("Scoreboards.Waiting.lines");
        sbTitles = MsgCache.getList("Scoreboards.Waiting.title");

        sbwLines = new LinkedHashMap<>();
        int max = lines.size() - 1;
        for (int i = 0; i < lines.size(); i++)
            sbwLines.put(max - i, lines.get(i));

        title(Main.color(sbTitles.getFirst()));
        set(player);
        if (sbTitles.size() > 1)
            animate();
    }

    @Override
    public void update(Player player) {
        for (Map.Entry<Integer, String> entry : sbwLines.entrySet())
            line(entry.getKey(), replace(entry.getValue(), player), "");
    }

    public void animate() {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {
            i = (i + 1) % sbTitles.size();
            title(Main.color(sbTitles.get(i)));
        }, 5L, 5L).getTaskId();
    }

    private String replace(String line, Player p) {
        String status;
        Arena arena = ArenaMgr.arenas.stream()
                .filter(a -> a.lobbyPlayers.contains(p))
                .findFirst()
                .orElse(null);
        if (arena == null) return line;
        if(arena.stat == ArenaStatus.STARTING) status="Starting: " + arena.toPr;
        else status=MsgCache.get("Arena.WAITING");

        return line
                .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%arena_name%", arena.name)
                .replace("%arena_size%", String.valueOf(arena.size))
                .replace("%arena_status%", status)
                .replace("%players_size%", String.valueOf(arena.lobbyPlayers.size()));
    }
}
