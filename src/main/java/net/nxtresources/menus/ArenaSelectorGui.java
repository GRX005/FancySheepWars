package net.nxtresources.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.ItemBuilder;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.enums.TeamType;
import net.nxtresources.managers.Arena;
import net.nxtresources.managers.ArenaMgr;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class ArenaSelectorGui {

    public static void open(Player player) {
        int size = ((ArenaMgr.arenas.size() / 9) + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, size, Component.text("Válassz arénát!"));

        for (Arena a : ArenaMgr.arenas) {
            Component line1 = LegacyComponentSerializer.legacySection().deserialize("§fJátékosok: §2" + a.lobbyPlayers.size() + "§8/§2" + a.size);
            Component line2 = LegacyComponentSerializer.legacySection().deserialize("§fÁllapot: " + ArenaStatus.getFormattedName(a.stat));
            ItemStack arena = new ItemBuilder(Material.GREEN_CONCRETE).setDisplayName("§2" + a.name).setLore(line1, line2).build();
            inv.addItem(arena);

        }
        player.openInventory(inv);

    }
}
