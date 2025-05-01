package net.nxtresources.menus;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.ItemBuilder;
import net.nxtresources.Main;
import net.nxtresources.enums.ArenaStatus;
import net.nxtresources.enums.TeamType;
import net.nxtresources.managers.Arena;
import net.nxtresources.managers.ArenaMgr;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class ArenaSelectorGui implements Listener {

    public static void open(Player player) {
        int size = ((ArenaMgr.arenas.size() / 9) + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, size, Component.text("Válassz arénát!"));

        for (Arena a : ArenaMgr.arenas) {
            Component line1 = LegacyComponentSerializer.legacySection().deserialize("§fJátékosok: §2" + a.lobbyPlayers.size() + "§8/§2" + a.size);
            Component line2 = LegacyComponentSerializer.legacySection().deserialize("§fÁllapot: " + ArenaStatus.getFormattedName(a.stat));
            ItemStack arena = new ItemBuilder(Material.GREEN_CONCRETE).setDisplayName("§2" + a.name).setLore(line1, line2).setArenaName(a.name).build();
            inv.addItem(arena);

        }
        player.openInventory(inv);

    }

    @EventHandler
    public void OnClick(InventoryClickEvent e) {
        if (!e.getView().title().equals(Component.text("Válassz arénát!"))) return;
        e.setCancelled(true);
        if (e.getCurrentItem() == null || !e.getCurrentItem().hasItemMeta()) return;
        Player player = (Player) e.getWhoClicked();
        ItemStack clicked = e.getCurrentItem();
        ItemMeta meta = clicked.getItemMeta();
        NamespacedKey namespacedKey = new NamespacedKey(Main.getInstance(), "arena_name");
        String name = meta.getPersistentDataContainer().get(namespacedKey, PersistentDataType.STRING);
        if(name ==null){
            player.sendMessage("§cNem sikerült csatlakozni az arénához!");
            player.closeInventory();
            return;
        }
        ItemStack leave = new ItemBuilder(Material.BARRIER).setDisplayName("§eAréna elhagyása").build();
        switch (ArenaMgr.join(name, player)) {
            case 0 -> {
                player.sendMessage("Csatlakoztál a következő arénához: " + name + "!");
                player.getInventory().clear();
                player.getInventory().setItem(8, leave);
                }

            case 1-> player.sendMessage("Nem findoltam az arénát.");
            case 2-> player.sendMessage("Mar arenaban vagy.");
            case 3-> player.sendMessage("Az arena tele.");
            case 4-> player.sendMessage("Az arena mar elindult.");
            case 5-> player.sendMessage("Waitinglobby=null");
            default -> player.sendMessage("random hiba");
        }
        player.closeInventory();
    }
}