package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.enums.BoardType;
import net.nxtresources.enums.SheepType;
import net.nxtresources.enums.TeamType;
import net.nxtresources.managers.scoreboard.Board;
import net.nxtresources.managers.scoreboard.BoardMgr;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;

import java.util.*;

import static net.nxtresources.enums.TeamType.BLUE;
import static net.nxtresources.enums.TeamType.RED;

public class Arena{

    public transient Set<Player> lobbyPlayers = new HashSet<>();
    public transient Set<Team> teams = new HashSet<>();
    private final Set<String> REDsheepSpawns = new HashSet<>();
    private final Set<String> BLUEsheepSpawns = new HashSet<>();
    private final Set<String> REDteamSpawns = new HashSet<>();
    private final Set<String> BLUEteamSpawns = new HashSet<>();
    //Csak innen lehet hozzaadni, 1 helyen.
    public transient volatile long prog = 0;
    private BukkitTask dTask;

    public String name;
    public int size;
    public transient ArenaStatus stat;
    public String waitingLobbyLocation;
    public BlockVector pos1, pos2, waitingPos1, waitingPos2;
    public String wName; //Needs to be str, as gson can't save World.
    public int toPr = -1;

    public Arena(String name, int size) {
        if(size %2!=0)
            throw new RuntimeException("Arena size can't be uneven.");
        this.name = name;
        this.size = size;
        this.stat = ArenaStatus.WAITING;
    }
//Countdown till start
    public void countdownTask() {
        stat = ArenaStatus.STARTING;
        toPr = 5;

        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(),t->{
            toPr--;
            for (Player p : lobbyPlayers)
                p.sendMessage("Az arena indul ennyi mulva: "+toPr);

            if(toPr==0) {//Itt indul az arena.
                stat = ArenaStatus.STARTED;
                Bukkit.getScheduler().runTask(Main.getInstance(), Arena.this::start);
                t.cancel();
            }
        },0L,20L);
    }

    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    private void arenaTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), t->{
            prog++;
            if (prog== 3600L || !sufficientPlayers()) {
                Bukkit.getScheduler().runTask(Main.getInstance(), Arena.this::end);
                t.cancel();
            }
        },0,20);
    }

    private BukkitRunnable dropTask() {
        return new BukkitRunnable() {
            final Random r = new Random();
            private final List<SheepType> sheeps =List.of(SheepType.EXPLOSIVE,SheepType.HEALING);
            @Override
            public void run() {
                var redAvailable = SheepMgr.getFreeSheepSpawns(getRedSheepSpawns());
                var blueAvailable = SheepMgr.getFreeSheepSpawns(getBlueSheepSpawns());
                if (!redAvailable.isEmpty() && !blueAvailable.isEmpty()) {
                    var redLoc = redAvailable.get(r.nextInt(redAvailable.size()));
                    var blueLoc = blueAvailable.get(r.nextInt(blueAvailable.size()));
                    var redSheep = sheeps.get(r.nextInt(sheeps.size()));
                    var blueSheep = sheeps.get(r.nextInt(sheeps.size()));
                    SheepMgr.spawnSheep(redLoc, redSheep);
                    SheepMgr.spawnSheep(blueLoc, blueSheep);
                }
                if(stat==ArenaStatus.WAITING)
                    this.cancel();
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

    private boolean sufficientPlayers() {
        for (Team t : teams)
            if(t.tPlayers.isEmpty())
                return false;
        return true;
    }

    private void start() {
        //TP, etc
        try {
            var players = new ArrayList<>(lobbyPlayers);
            int mid = players.size() / 2;
            var blue = new ArrayList<>(players.subList(0, mid));
            var red = new ArrayList<>(players.subList(mid, players.size()));
            teams.addAll(List.of(new Team(blue, BLUE), new Team(red, RED)));
            var blueTeam = getBlueTeamSpawns().iterator();
            var redTeam = getRedTeamSpawns().iterator();

            for(var player : red){
                if(!redTeam.hasNext()) redTeam = getBlueTeamSpawns().iterator();
                player.teleportAsync(redTeam.next());
                BoardMgr.setBoard(player, new Board(BoardType.INGAME));
            }
            for(var player : blue){
                if(!blueTeam.hasNext()) blueTeam = getBlueTeamSpawns().iterator();
                player.teleportAsync(blueTeam.next());
                BoardMgr.setBoard(player, new Board(BoardType.INGAME));
            }

            WorldMgr.getInst().rmLobby(Bukkit.getWorld(wName),waitingPos1,waitingPos2);
            lobbyPlayers.clear();
            dTask=dropTask().runTaskTimer(Main.getInstance(), 20L, 200L);
            arenaTask();
        } catch (Exception e) {
            Bukkit.broadcast(Component.text("Hiba tortent (valszeg dög levente hibajabol)"));
            System.out.println("Exception: "+e);
            throw new RuntimeException("Hiba tortent (valszeg dög levente hibajabol)");
        }
    }

    public void end() {
        teams.forEach(e-> e.tPlayers.forEach(p -> {
            ItemMgr.lobbyItems(p);
            LobbyMgr.tpMainLobby(p);
        }));
        teams.clear();
        prog=0;
        //Prog ==0 and status != waiting -> Arena is restoring.
        var wrld = Objects.requireNonNull(Bukkit.getWorld(wName));
        WorldMgr.getInst().toWrld(wrld,name);
        stat=ArenaStatus.WAITING;
        if(dTask!=null) {
            dTask.cancel();
            dTask=null;
        }
        SheepMgr.rmSheeps(wrld);
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

        public Set<Location> redSheepSpawns = new HashSet<>();
        public Set<Location> blueSheepSpawns = new HashSet<>();

        public Set<Location> redTeamSpawns = new HashSet<>();
        public Set<Location> blueTeamSpawns = new HashSet<>();

        public Location pos1, pos2, waitingLobby, waitingPos1, waitingPos2;

        public Temp(String name, int size) {
            this.name = name;
            this.size = size;
        }
    }

    /*
     *FOR ARENA REGION
     * */
    public void setPos1(Location loc) {
        this.pos1 = new BlockVector(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
    }
    public void setPos2(Location loc) {
        this.pos2 = new BlockVector(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
    }
    /*
     *FOR WAITING LOBBY REGION
     * */

    public void tpWaitingLobby(Player player, String name){
        Arena arena = ArenaMgr.getByName(name);
        if(arena == null) return;
        Location location = arena.getWaitingLobby();
        player.teleportAsync(location);
    }

    public void setWaitingLobby(Location loc) {
        this.waitingLobbyLocation = LocationMgr.set(loc);
    }
    public Location getWaitingLobby() {
        return LocationMgr.get(waitingLobbyLocation);
    }

    public void setWaitingPos1(Location loc) {
        this.waitingPos1 = new BlockVector(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
    }
    public void setWaitingPos2(Location loc) {
        this.waitingPos2 = new BlockVector(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
    }
    /*
    *FOR SHEEPS
    * */
    public void setRedSheepSpawns(Set<Location> locs) {
        REDsheepSpawns.clear();
        for (Location loc : locs)
            REDsheepSpawns.add(LocationMgr.set(loc));
    }

    public void setBlueSheepSpawns(Set<Location> locs) {
        BLUEsheepSpawns.clear();
        for (Location loc : locs)
            BLUEsheepSpawns.add(LocationMgr.set(loc));
    }

    public Set<Location> getRedSheepSpawns() {
        Set<Location> locs = new HashSet<>();
        for (String s : REDsheepSpawns)
            locs.add(LocationMgr.get(s));
        return locs;
    }

    public Set<Location> getBlueSheepSpawns() {
        Set<Location> locs = new HashSet<>();
        for (String s : BLUEsheepSpawns)
            locs.add(LocationMgr.get(s));
        return locs;
    }

    /*
     *FOR TEAMS
     * */
    public void setRedTeamSpawns(Set<Location> locs) {
        REDteamSpawns.clear();
        for (Location loc : locs)
            REDteamSpawns.add(LocationMgr.set(loc));
    }

    public void setBlueTeamSpawns(Set<Location> locs) {
        BLUEteamSpawns.clear();
        for (Location loc : locs)
            BLUEteamSpawns.add(LocationMgr.set(loc));
    }

    public Set<Location> getRedTeamSpawns() {
        Set<Location> locs = new HashSet<>();
        for (String s : REDteamSpawns)
            locs.add(LocationMgr.get(s));
        return locs;
    }

    public Set<Location> getBlueTeamSpawns() {
        Set<Location> locs = new HashSet<>();
        for (String s : BLUEteamSpawns)
            locs.add(LocationMgr.get(s));
        return locs;
    }
}
