package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SetupManager {
    public static final Map<UUID, String> playerSetupArena = new HashMap<>();

    public static int startSetup(Player player, String name) {
        Arena a = ArenaMgr.arCache.get(name);
        if(a==null)
            return 1;
        if(isInSetup(player))
            return 2;
        playerSetupArena.put(player.getUniqueId(), name);

        //WAITINGLOBBY
        Component line1 = LegacyComponentSerializer.legacySection().deserialize("§eVálassz ki egy várakozó lobbynak megfelelő helyet,");
        Component line2 = LegacyComponentSerializer.legacySection().deserialize("§eállj oda majd kattints ezzel az itemmel!");

        ItemStack setwaitinglobby = new ItemBuilder(Material.DARK_OAK_DOOR).setDisplayName("§aVárakozó lobby beállítása").setLore(line1, line2).build();
        ItemStack leave =new ItemBuilder(Material.BARRIER).setDisplayName("§cSetup mód elhagyása").build();

        player.getInventory().clear();
        player.getInventory().setItem(0, setwaitinglobby);
        player.getInventory().setItem(8, leave);
        return 0;
    }

    public static String getSetupArena(Player player) {
        return playerSetupArena.get(player.getUniqueId());
    }
    public static boolean isInSetup(Player player) {
        return playerSetupArena.containsKey(player.getUniqueId());
    }
}
