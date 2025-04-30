package net.nxtresources.managers;

import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.enums.TeamType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Arena {

    public transient Set<Player> lobbyPlayers = new HashSet<>();
    public transient Set<Team> teams = new HashSet<>();
    private final Map<String, String> teamSpawns = new HashMap<>();


    public String name;
    public int size;
    public ArenaStatus stat;
    private BukkitTask task;
    public String waitingLobbyLocation;

    public Arena(String name, int size) {
        if(size %2!=0)
            throw new RuntimeException("Arena size can't be uneven.");
        this.name = name;
        this.size = size;
        this.stat = ArenaStatus.WAITING;
    }

    public void countdown(Runnable task) {
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(),task,0L,20L);
    }

    public void cancelCount() {
        this.task.cancel();
    }

    public TeamType getTeam(Player pl) {
        for(Team t : teams) {
            for(Player p : t.tPlayers) {
                if(p==pl)
                    return t.type;
            }
        }
        return null;
    }

    public void start() {
        //TP, etc
        List<Player> copy = new ArrayList<>(lobbyPlayers); // to safely split
        int midpoint = copy.size() / 2;

        List<Player> bluePlayers =new ArrayList<>(copy.subList(0, midpoint));
        List<Player> redPlayers = new ArrayList<>(copy.subList(midpoint, copy.size()));
        Team blueTeam = new Team(bluePlayers, TeamType.BLUE);
        Team redTeam = new Team(redPlayers, TeamType.RED);
        teams.add(blueTeam);
        teams.add(redTeam);
        for(Player player : bluePlayers)
            player.teleportAsync(getTeamSpawn(TeamType.BLUE));
        for(Player player : redPlayers)
            player.teleportAsync(getTeamSpawn(TeamType.RED));
        lobbyPlayers.clear();
    }

    public static final class Team {
        public final Set<Player> tPlayers = new HashSet<>();
        public TeamType type;
        public Team(List<Player> pl, TeamType type) {
            this.tPlayers.addAll(pl);
            this.type = type;
        }
        //stb...
    }

    public void setWaitingLobby(Location loc) {
        this.waitingLobbyLocation = LocationManager.set(loc);
    }
    public Location getWaitingLobby() {
        return LocationManager.get(waitingLobbyLocation);
    }
    public void setTeamSpawn(TeamType type, Location loc) {
        teamSpawns.put(type.name(), LocationManager.set(loc));
    }
    public Location getTeamSpawn(TeamType type) {
        return teamSpawns.get(type.name()) == null ? null : LocationManager.get(teamSpawns.get(type.name()));
    }

}
