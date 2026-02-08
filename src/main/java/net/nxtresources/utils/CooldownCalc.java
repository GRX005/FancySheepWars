package net.nxtresources.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownCalc {

    private static final Map<UUID, Integer> lastTick = new HashMap<>();

    public static boolean clickTick(Player player){
        Integer last = lastTick.get(player.getUniqueId());
        if (last != null && last == Bukkit.getCurrentTick()) return true;
        lastTick.put(player.getUniqueId(), Bukkit.getCurrentTick());
        return false;
    }
}