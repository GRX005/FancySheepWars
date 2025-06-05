package net.nxtresources.managers.scoreboard.boards;

import net.nxtresources.Main;
import net.nxtresources.managers.DataMgr;
import net.nxtresources.managers.MsgCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.nxtresources.managers.scoreboard.BaseBoard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LobbyBoard extends BaseBoard{

    LinkedHashMap<Integer, String> sbLines;
    List<String> sbTitles;
    int i=0;
    int taskId = -1;

    @Override
    public void build(Player player) {
        List<String> lines = MsgCache.getList("Scoreboards.Lobby.lines");
        sbTitles = MsgCache.getList("Scoreboards.Lobby.title");

        sbLines = new LinkedHashMap<>();
        int max = lines.size() - 1;
        for (int i = 0; i < lines.size(); i++)
            sbLines.put(max - i, lines.get(i));
        title(Main.color(sbTitles.getFirst()));
        set(player);
        updateB();
        if (sbTitles.size() > 1)
            animate();

    }

    public void animate() {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {
            i = (i + 1) % sbTitles.size();
            title(Main.color(sbTitles.get(i)));
        }, 5L, 5L).getTaskId();
    }

    public void cancelAnimation() {
        super.cancel();
        Bukkit.getScheduler().cancelTask(taskId);
    }

    @Override
    public void update(Player player) {
        for (Map.Entry<Integer, String> entry : sbLines.entrySet())
            line(entry.getKey(), replace(entry.getValue(), player), "");
    }
    String replace(String line, Player p) {
        return line
                .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%kills%", String.valueOf(DataMgr.getKills(p)))
                .replace("%deaths%", String.valueOf(DataMgr.getDeaths(p)))
                .replace("%wins%", String.valueOf(DataMgr.getWins(p)));
    }
}
