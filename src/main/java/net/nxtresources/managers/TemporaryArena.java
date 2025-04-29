package net.nxtresources.managers;

import org.bukkit.Location;

import java.util.HashMap;
import java.util.Map;

public class TemporaryArena {
    public String name;
    public int size;
    public Location waitingLobby;
    public Map<String, Location> teamSpawns =new HashMap<>();

    public TemporaryArena(String name,int size) {
        this.name = name;
        this.size = size;
    }
}
