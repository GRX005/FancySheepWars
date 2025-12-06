package net.nxtresources.menus;

import net.kyori.adventure.text.Component;
import net.nxtresources.ItemBuilder;
import net.nxtresources.Main;
import net.nxtresources.managers.Arena;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.MsgCache;
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

import static net.nxtresources.managers.ItemMgr.leaveArena;

public class ArenaSelectorGui implements Listener {

    public static void open(Player player) {
        int size = ((ArenaMgr.arenas.size() / 9) + 1) * 9;
        Inventory inv = Bukkit.createInventory(null, size, Main.color(MsgCache.get("Guis.ArenaSelector.title")));

        for (Arena a : ArenaMgr.arenas) {
            var status = MsgCache.get(a.stat.getConfig());
            var lore = MsgCache.getList("Guis.ArenaSelector.lore");
            var title = MsgCache.get("Guis.ArenaSelector.name").replace("%arena_name%", a.name);
            Component fixTitle = Main.color(title);

            Component[] loreComponents = lore.stream().map(line -> {
                        var replaced = line
                                .replace("%players_size%", String.valueOf(a.lobbyPlayers.size()))
                                .replace("%arena_name%", a.name)
                                .replace("%arena_size%", String.valueOf(a.size))
                                .replace("%arena_status%", status);
                        return Main.color(replaced);
            }).toArray(Component[]::new);

            ItemStack arena = new ItemBuilder(Material.GREEN_CONCRETE)
                    .setDisplayName(fixTitle)
                    .setLore(loreComponents)
                    .setArenaName(a.name)
                    .build();
            inv.addItem(arena);
        }
        player.openInventory(inv);

    }

    @EventHandler
    public void OnClick(InventoryClickEvent e) {
        if (!e.getView().title().equals(Main.color(MsgCache.get("Guis.ArenaSelector.title")))) return;
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
        switch (ArenaMgr.join(name, player)) {
            case 0 -> {
                player.sendMessage("Csatlakoztál a következő arénához: " + name + "!");
                player.getInventory().clear();
                player.getInventory().setItem(8, leaveArena);
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