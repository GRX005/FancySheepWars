package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.enums.TeamType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Arena {

    public transient Set<Player> lobbyPlayers = new HashSet<>();
    public transient Set<Team> teams = new HashSet<>();
    private final Map<TeamType, String> teamSpawns = new HashMap<>();
    //Csak innen lehet hozzaadni, 1 helyen.
    private transient volatile int prog = 0;

    public int getProg(){
        return prog;
    }

    public String name;
    public int size;
    public ArenaStatus stat;
    public String waitingLobbyLocation;
    public String pos1;
    public String pos2;

    public Arena(String name, int size) {
        if(size %2!=0)
            throw new RuntimeException("Arena size can't be uneven.");
        this.name = name;
        this.size = size;
        this.stat = ArenaStatus.WAITING;
    }
//Countdown till start
    @SuppressWarnings("ConstantConditions")
    public BukkitRunnable countdownTask() {
        return new BukkitRunnable(){
            int toPr = 10;
            @Override
            public void run() {
                toPr--;
                for (Player p : lobbyPlayers)
                    p.sendMessage("Az arena indul ennyi mulva: "+toPr);

                if(toPr==0) {//Itt indul az arena.
                    stat = ArenaStatus.STARTED;
                    start();
                    this.cancel();
                }
            }
        };
    }
    @SuppressWarnings("ConstantConditions")
    private BukkitRunnable arenaTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                //noinspection NonAtomicOperationOnVolatileField (Csak 1 helyen szabad modositani)
                prog++;
                if (prog==15) {
                    end();
                    this.cancel();
                }
            }
        };
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

    private void start() {
        //TP, etc
        try {
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
            arenaTask().runTaskTimerAsynchronously(Main.getInstance(),0,20);
        } catch (Exception e) {
            Bukkit.broadcast(Component.text("Hiba tortent (valszeg dög levente hibajabol)"));
            throw new RuntimeException("Hiba tortent (valszeg dög levente hibajabol)");
        }
    }

    public void end() {
        teams.forEach(e->e.tPlayers.forEach(f->{
            SetupMgr.tpToLobby(f);
            ItemMgr.lobbyItems(f);
        }));
        teams.clear();
        prog=0;
        stat=ArenaStatus.WAITING;
    }
//2 teams in 1 arena
    public static final class Team {
        public final Set<Player> tPlayers = new HashSet<>();
        public TeamType type;
        public Team(List<Player> pl, TeamType type) {
            this.tPlayers.addAll(pl);
            this.type = type;
        }
        //stb...
    }
//For Setup.
    public static final class Temp {
        public String name;
        public int size;
        public Location waitingLobby;
        public Map<String, Location> teamSpawns =new HashMap<>();
        public Location pos1;
        public Location pos2;

        public Temp(String name, int size) {
            this.name = name;
            this.size = size;
        }
    }

    public void setWaitingLobby(Location loc) {
        this.waitingLobbyLocation = LocationMgr.set(loc);
    }
    public Location getWaitingLobby() {
        return LocationMgr.get(waitingLobbyLocation);
    }
    public void setTeamSpawn(TeamType type, Location loc) {
        teamSpawns.put(type, LocationMgr.set(loc));
    }
    public Location getTeamSpawn(TeamType type) {
        return teamSpawns.get(type) == null ? null : LocationMgr.get(teamSpawns.get(type));
    }
    public void setPos1(Location loc) {
        this.pos1 = LocationMgr.set(loc);
    }
    public String getPos1() {
        return pos1;
    }
    public void setPos2(Location loc) {
        this.pos2 = LocationMgr.set(loc);
    }
    public String getPos2() {
        return pos2;
    }
}
