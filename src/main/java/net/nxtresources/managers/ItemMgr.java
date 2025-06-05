package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ItemMgr {

    public static void lobbyItems(Player player){
        ItemStack arenaselector = new ItemBuilder(Material.BOOK)
                .setDisplayName("§eAréna választó")
                .setAmount(1)
                .build();
        player.getInventory().setItem(4, arenaselector);

    }

    public static void waitingItems(Player player){
        ItemStack returnToLobby = new ItemBuilder(Material.BARRIER)
                .setDisplayName("Return to lobby")
                .setAmount(1)
                .build();
        player.getInventory().setItem(8, returnToLobby);

    }

    public static void arenaItems(Player player){}

    //setup items
    public static ItemStack blue = new ItemBuilder(Material.BLUE_WOOL)
            .setDisplayName("§9§lKÉK §fcsapat")
            .setLore(LegacyComponentSerializer.legacySection().deserialize("§eEzzel kattintva elkezded setupolni az adott csapatot"))
            .build();
    public static ItemStack red = new ItemBuilder(Material.RED_WOOL)
            .setDisplayName("§c§lPIROS §fcsapat")
            .setLore(LegacyComponentSerializer.legacySection().deserialize("§eEzzel kattintva elkezded setupolni az adott csapatot"))
            .build();
    public static ItemStack leaveSetup = new ItemBuilder(Material.BARRIER)
            .setDisplayName("§cSetup mód elhagyása")
            .build();
    public static ItemStack saveAndExit = new ItemBuilder(Material.EMERALD_BLOCK)
            .setDisplayName("§aMentés és kilépés a setup módból")
            .build();
    public static ItemStack setwaitinglobby = new ItemBuilder(Material.DARK_OAK_DOOR)
            .setDisplayName("§aVárakozó lobby beállítása")
            .setLore(
                    LegacyComponentSerializer.legacySection().deserialize("§eVálassz ki egy várakozó lobbynak megfelelő helyet,"),
                    LegacyComponentSerializer.legacySection().deserialize("§eállj oda majd kattints ezzel az itemmel!"))
            .build();
    public static ItemStack selectorTool =new ItemBuilder(Material.WOODEN_AXE)
            .setDisplayName("§AKijelölő")
            .build();

    //default items
    public static ItemStack leaveArena = new ItemBuilder(Material.BARRIER)
            .setDisplayName("§eAréna elhagyása")
            .build();

}
