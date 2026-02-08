package net.nxtresources.managers.scoreboard;

import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.enums.BoardType;
import net.nxtresources.managers.*;
import net.nxtresources.utils.MsgCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Board extends BaseBoard{

    final String configPath;
    LinkedHashMap<Integer, String> lines;
    List<String> titles = List.of();
    int i = 0;
    int taskId;

    public Board(BoardType type) {
        this.configPath = type.get();
    }

    @Override
    public void build(Player player){
        List<String> rawLines = MsgCache.getList(configPath + ".lines");
        titles = MsgCache.getList(configPath + ".title");
        lines = new LinkedHashMap<>();
        int max = rawLines.size() - 1;
        for (int i = 0; i < rawLines.size(); i++) lines.put(max - i, rawLines.get(i));
        title(Main.color(replace(titles.getFirst(), player)));
        set(player);
        for (Map.Entry<Integer, String> entry : lines.entrySet()) {
            String line = replace(entry.getValue(), player);
            line(entry.getKey(), line, "");
        }
        if (titles.size() > 1) animate(player);
    }

    public void animate(Player player) {
        taskId = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), () -> {
            i = (i + 1) % titles.size();
            title(Main.color(replace(titles.get(i), player)));
        }, 5L, 5L).getTaskId();
    }

    @Override
    public void update(Player player) {
        for (Map.Entry<Integer, String> entry : lines.entrySet()) {
            String line = replace(entry.getValue(), player);
            line(entry.getKey(), line, "");
        }
    }

    public String replace(String line, Player player){

        line = line
                .replace("%online_players%", String.valueOf(Bukkit.getOnlinePlayers().size()))
                .replace("%kills%", String.valueOf(DataMgr.getKills(player)))
                .replace("%deaths%", String.valueOf(DataMgr.getDeaths(player)))
                .replace("%wins%", String.valueOf(DataMgr.getWins(player)));
        String status;
        Arena arena = ArenaMgr.arenas.stream()
                .filter(a -> a.lobbyPlayers.contains(player))
                .findFirst()
                .orElse(null);
        if(arena!=null) {
            if (arena.stat == ArenaStatus.STARTING)
                status = MsgCache.get("Scoreboards.Waiting.placeholder") + " " + arena.toPr;
            else status = MsgCache.get("Arena.WAITING");
            line = line
                    .replace("%arena_name%", arena.name)
                    .replace("%arena_size%", String.valueOf(arena.size))
                    .replace("%arena_status%", status)
                    .replace("%players_size%", String.valueOf(arena.lobbyPlayers.size()));
        }
        var session = SetupMgrNew.sessions.get(player.getUniqueId());
        if(session!=null) {
            Arena.Temp temp = session.temp;

            var mapRegion = temp.pos1 != null && temp.pos2 != null;
            var lobbyRegion = temp.waitingPos1 != null && temp.waitingPos2 != null;
            var lobbySpawn = temp.waitingLobby != null;
            var redTeam = temp.teamSpawns != null && temp.teamSpawns.containsKey("RED");
            var blueTeam = temp.teamSpawns != null && temp.teamSpawns.containsKey("BLUE");
            var redSheep = temp.redSheepSpawns != null && !temp.redSheepSpawns.isEmpty();
            var blueSheep = temp.blueSheepSpawns != null && !temp.blueSheepSpawns.isEmpty();
            boolean[] steps = {mapRegion, lobbyRegion, lobbySpawn, redTeam, blueTeam, redSheep, blueSheep};

            var finished = 0;
            for (boolean step : steps) if (step) finished++;
            var max = steps.length;

            line = line
                    .replace("%map_region%", isDone(mapRegion))
                    .replace("%lobby_region%", isDone(lobbyRegion))
                    .replace("%lobby_spawn%", isDone(lobbySpawn))
                    .replace("%red_team%", isDone(redTeam))
                    .replace("%blue_team%", isDone(blueTeam))
                    .replace("%red_sheep%", isDone(redSheep))
                    .replace("%blue_sheep%", isDone(blueSheep))
                    .replace("%finished%", String.valueOf(finished))
                    .replace("%max%", String.valueOf(max));
        }
        return line;
    }

    public String isDone(boolean done){
        return done ? MsgCache.get("Arena.Icons.PIPE") : MsgCache.get("Arena.Icons.X");
    }
}
