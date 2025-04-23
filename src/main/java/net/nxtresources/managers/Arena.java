package net.nxtresources.managers;

import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.Set;

public class Arena {

    public transient Set<Player> players = new HashSet<>();

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

    public void setWaitingLobby(Location loc) {
        this.waitingLobbyLocation = LocationManager.locToString(loc);
    }
    public Location getWaitingLobby() {
        return LocationManager.stringToLoc(waitingLobbyLocation);
    }

}
