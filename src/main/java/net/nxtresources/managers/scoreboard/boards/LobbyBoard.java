package net.nxtresources.managers.scoreboard.boards;

import net.nxtresources.Main;
import net.nxtresources.managers.MsgCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import net.nxtresources.managers.scoreboard.BaseBoard;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class LobbyBoard extends BaseBoard{

    LinkedHashMap<Integer, String> sbLines;

    @Override
    public void build(Player player) {
        List<String> lines = MsgCache.getList("Scoreboards.Lobby.lines");
        sbLines = new LinkedHashMap<>();
        int max = lines.size() - 1;
        for (int i = 0; i < lines.size(); i++)
            sbLines.put(max - i, lines.get(i));
        title((Main.color(MsgCache.get("Scoreboards.Lobby.title"))));
        set(player);
        updateB();

    }

    @Override
    public void update(Player player) {
        for (Map.Entry<Integer, String> entry : sbLines.entrySet())
            line(entry.getKey(), replace(entry.getValue()), "");
    }
    String replace(String line) {
        return line.replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()));
    }
}
