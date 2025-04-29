package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.ItemBuilder;
import net.nxtresources.listeners.JoinAndQuitEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class SetupManager {
    public static final Map<UUID, String> playerSetupArena = new HashMap<>();
    public static final Set<Arena> temporaryArenas = new HashSet<>();
    public static final Map<UUID, TemporaryArena> tempdata = new HashMap<>();

    public static int startSetup(Player player, String name, int size, boolean isTemporary){
        if(isInSetup(player))
            return 1;
        Arena arena;
        if(isTemporary){
            arena = new Arena(name,size);
            temporaryArenas.add(arena);
            tempdata.put(player.getUniqueId(), new TemporaryArena(name, size));
        }
        playerSetupArena.put(player.getUniqueId(), name);
        Component line1 = LegacyComponentSerializer.legacySection().deserialize("§eVálassz ki egy várakozó lobbynak megfelelő helyet,");
        Component line2 = LegacyComponentSerializer.legacySection().deserialize("§eállj oda majd kattints ezzel az itemmel!");

        ItemStack setwaitinglobby = new ItemBuilder(Material.DARK_OAK_DOOR).setDisplayName("§aVárakozó lobby beállítása").setLore(line1, line2).build();
        ItemStack leave = new ItemBuilder(Material.BARRIER).setDisplayName("§cSetup mód elhagyása").build();
        ItemStack saveAndExit = new ItemBuilder(Material.EMERALD_BLOCK).setDisplayName("§aMentés és kilépés a setup módból").build();

        player.getInventory().clear();
        player.getInventory().setItem(0, setwaitinglobby);
        player.getInventory().setItem(7, saveAndExit);
        player.getInventory().setItem(8, leave);
        return 0;
    }

    public static void finishSetup(Player player, boolean succ){
        String name = playerSetupArena.remove(player.getUniqueId());
        TemporaryArena tempData = tempdata.remove(player.getUniqueId());
        if(name == null ||tempData==null)
            return;
        if(succ) {
            Arena arena = new Arena(tempData.name, tempData.size);
            if(tempData.waitingLobby!=null)
                arena.setWaitingLobby(tempData.waitingLobby);
            ArenaMgr.arenas.add(arena);
            ArenaMgr.arCache.put(arena.name, arena);
            ArenaMgr.saveArena(arena);
            player.getInventory().clear();
            JoinAndQuitEvent.addLobbyItems(player);
        } else{
            player.getInventory().clear();
            JoinAndQuitEvent.addLobbyItems(player);
            temporaryArenas.removeIf(a -> a.name.equals(name));
        }
    }


    public static String getSetupArena(Player player) {
        return playerSetupArena.get(player.getUniqueId());
    }
    public static boolean isInSetup(Player player) {
        return playerSetupArena.containsKey(player.getUniqueId());
    }
}
