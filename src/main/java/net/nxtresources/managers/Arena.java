package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.enums.TeamType;
import net.nxtresources.sheeps.ExplSheep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.BlockVector;

import java.util.*;

import static net.nxtresources.enums.TeamType.BLUE;
import static net.nxtresources.enums.TeamType.RED;

public class Arena {

    private static final Logger log = LogManager.getLogger(Arena.class);
    public transient Set<Player> lobbyPlayers = new HashSet<>();
    public transient Set<Team> teams = new HashSet<>();
    private final Map<TeamType, String> teamSpawns = new HashMap<>();
    private final Set<String> REDsheepSpawns = new HashSet<>();
    private final Set<String> BLUEsheepSpawns = new HashSet<>();
    //Csak innen lehet hozzaadni, 1 helyen.
    private transient volatile long prog = 0;

    private transient HashMap<BlockVector, BlockData> worldRAM;

    BukkitTask droptask;

    public long getProg(){
        return prog;
    }

    public String name;
    public int size;
    public ArenaStatus stat;
    public String waitingLobbyLocation;
    public BlockVector pos1;
    public BlockVector pos2;
    public String wName; //Temp atm

    public Arena(String name, int size) {
        if(size %2!=0)
            throw new RuntimeException("Arena size can't be uneven.");
        this.name = name;
        this.size = size;
        this.stat = ArenaStatus.WAITING;
    }
//Countdown till start
    public BukkitRunnable countdownTask() {
        return new BukkitRunnable(){
            int toPr = 5;
            @Override
            public void run() {
                toPr--;
                for (Player p : lobbyPlayers)
                    p.sendMessage("Az arena indul ennyi mulva: "+toPr);

                if(toPr==0) {//Itt indul az arena.
                    var tim = System.currentTimeMillis();
                    worldRAM = WorldMgr.getInst().saveAsync(Bukkit.getWorld(wName), pos1,pos2);
                    System.out.println("WMGR Save: "+(System.currentTimeMillis()-tim)+" ms");
                    stat = ArenaStatus.STARTED;
                    start();
                    this.cancel();
                }
            }
        };
    }

    private BukkitRunnable arenaTask() {
        return new BukkitRunnable() {
            @Override
            public void run() {
                //noinspection NonAtomicOperationOnVolatileField (Csak 1 helyen szabad modositani)
                prog++;
                if (prog== 3600L || !sufficientPlayers()) {
                    end();
                    this.cancel();
                }
            }
        };
    }

    private BukkitRunnable dropTask() {
        return new BukkitRunnable() {
            final Random r = new Random();
            @Override
            public void run() {
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
                var redAvailable = ExplSheep.getFreeSheepSpawns(getRedSheepSpawns());
                var blueAvailable = ExplSheep.getFreeSheepSpawns(getBlueSheepSpawns());
                    if (!redAvailable.isEmpty() && !blueAvailable.isEmpty()) {
                        var redLoc = redAvailable.get(r.nextInt(redAvailable.size()));
                        var blueLoc = blueAvailable.get(r.nextInt(blueAvailable.size()));
                        ExplSheep.spawnSheep(redLoc);
                        ExplSheep.spawnSheep(blueLoc);
                    }
                });
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
            blue.forEach(p -> p.teleportAsync(getTeamSpawn(BLUE)));
            red.forEach(p -> p.teleportAsync(getTeamSpawn(RED)));
            lobbyPlayers.clear();
            droptask =dropTask().runTaskTimerAsynchronously(Main.getInstance(), 20L, 200L);
            arenaTask().runTaskTimerAsynchronously(Main.getInstance(),0,20);
        } catch (Exception e) {
            Bukkit.broadcast(Component.text("Hiba tortent (valszeg dög levente hibajabol)"));
            log.error("Exception: ", e);
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
        //Prog ==0 and status != waiting -> Arena is restoring.
        Bukkit.getScheduler().runTask(Main.getInstance(),()->WorldMgr.getInst().load(Bukkit.getWorld(wName), worldRAM));
        stat=ArenaStatus.WAITING;
        if(droptask!=null) {
            droptask.cancel();
            droptask=null;
        }
        Bukkit.getScheduler().runTask(Main.getInstance(),()->{
            World world = Bukkit.getWorld(wName);
            if(world!=null)
                ExplSheep.removeSheeps(world);
        });
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
        public Map<String, Location> teamSpawns =new HashMap<>();
        public Set<Location> redSheepSpawns = new HashSet<>();
        public Set<Location> blueSheepSpawns = new HashSet<>();
        public Location pos1, pos2, waitingLobby;

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
        this.pos1 = new BlockVector(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
    }
    public BlockVector getPos1() {
        return pos1;
    }
    public void setPos2(Location loc) {
        this.pos2 = new BlockVector(loc.getBlockX(),loc.getBlockY(),loc.getBlockZ());
    }
    public BlockVector getPos2() {
        return pos2;
    }
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
}
