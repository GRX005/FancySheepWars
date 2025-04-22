package net.nxtresources.managers;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class Arena {

    public transient Set<Player> players = new HashSet<>();

    public String name;
    public int size;

    public Arena(String name, int size) {
        if(size %2!=0)
            throw new RuntimeException("Arena size can't be uneven.");
        this.name = name;
        this.size = size;
    }

}
