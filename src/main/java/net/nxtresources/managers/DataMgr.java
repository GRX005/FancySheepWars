package net.nxtresources.managers;

import com.google.common.reflect.TypeToken;
import net.nxtresources.Main;
import org.bukkit.entity.Player;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static net.nxtresources.Main.gson;

public class DataMgr {

    public static final Map<UUID, Map<String, Integer>> stats = new HashMap<>();

    public static Map<String, Integer> get(Player player) {
        return stats.computeIfAbsent(player.getUniqueId(), k -> {
            Map<String, Integer> s = new HashMap<>();
            s.put("kills", 0);
            s.put("deaths", 0);
            s.put("wins", 0);
            return s;
        });
    }

    public static void save() {
        for (Map.Entry<UUID, Map<String, Integer>> entry : stats.entrySet())
            Main.dataConfig.set("players." + entry.getKey().toString(), gson.toJson(entry.getValue()));
        Main.saveDataConfig();
    }

    public static void load() {
        if (Main.dataConfig.contains("players")) {
            for (String uuidStr : Objects.requireNonNull(Main.dataConfig.getConfigurationSection("players")).getKeys(false)) {
                String json = Main.dataConfig.getString("players." + uuidStr);
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Type type = new TypeToken<Map<String, Integer>>(){}.getType();
                    Map<String, Integer> map = gson.fromJson(json, type);
                    stats.put(uuid, map);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void set(Player player, String key) {
        Map<String, Integer> map = get(player);
        if (!map.containsKey(key))
            map.put(key, 1);
        else
            map.put(key, map.get(key) + 1);
        save(); //TODO: Potencialis teljesitmenyproblema javitasa -Levi
    }
    public static int get(Player player, String key) {
        Map<String, Integer> map = get(player);
        if (!map.containsKey(key))
            return 0;
        return map.get(key);
    }

    public static void addKill(Player player) {
        set(player, "kills");
    }

    public static void addDeath(Player player) {
        set(player, "deaths");
    }

    public static void addWin(Player player) {
        set(player, "wins");
    }

    public static int getKills(Player player) {
        return get(player, "kills");
    }

    public static int getDeaths(Player player) {
        return get(player, "deaths");
    }

    public static int getWins(Player player) {
        return get(player, "wins");
    }
}
