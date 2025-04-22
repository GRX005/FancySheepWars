package net.nxtresources.managers;

import net.nxtresources.enums.ArenaStatus;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;

public class Arena {

    private final String name;
    private final ArenaStatus arenaStatus = ArenaStatus.WAITING;
    private final List<Player> players = new ArrayList<>();
    private BukkitTask bukkitTask;

    public Arena(String name) {
        this.name = name;

    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);

    }

    public String getName() {
        return name;
    }
    public ArenaStatus getState() {
        return arenaStatus;
    }
    public List<Player> getPlayers() {
        return players;
    }
}
