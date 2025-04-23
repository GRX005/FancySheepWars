package net.nxtresources.managers;

import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.enums.TeamType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Arena {

    public transient Set<Player> lobbyPlayers = new HashSet<>();
    public transient Set<Team> teams = new HashSet<>();


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

        teams.add(new Team(new ArrayList<>(copy.subList(0, midpoint)), TeamType.BLUE));
        teams.add(new Team(new ArrayList<>(copy.subList(midpoint, copy.size())), TeamType.RED));

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
        this.waitingLobbyLocation = LocationManager.locToString(loc);
    }
    public Location getWaitingLobby() {
        return LocationManager.stringToLoc(waitingLobbyLocation);
    }

}
