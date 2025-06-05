package net.nxtresources.managers.scoreboard.boards;

/*import net.nxtresources.Main;
import net.nxtresources.managers.DataMgr;
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
        updateB();
    }

    @Override
    public void update(Player player) {
        for (Map.Entry<Integer, String> entry : sbwLines.entrySet()) {
            line(entry.getKey(), replace(entry.getValue(), player), "");
        }
    }

    private String replace(String line, Player p) {
        return line
                .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%kills%", String.valueOf(DataMgr.getKills(p)))
                .replace("%deaths%", String.valueOf(DataMgr.getDeaths(p)))
                .replace("%wins%", String.valueOf(DataMgr.getWins(p)));
    }

    @Override
    public void cancel() {
        super.cancel();
    }
}*/
