package net.nxtresources.menus;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class ArenaSelectorGui {

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, Component.text("Válassz arénát!"));
        player.openInventory(inv);

    }
}
