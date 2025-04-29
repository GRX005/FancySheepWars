package net.nxtresources.listeners;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ItemDropEvent implements Listener {

    String[] nodrop = {
            "§eAréna választó", //this
            "§aVárakozó lobby beállítása", //setup
            "§cSetup mód elhagyása", //setup
            "§aMentés és kilépés a setup módból" //setup
    };

    @EventHandler
    public void onDropLobbyInventoryItems(PlayerDropItemEvent event) {
        ItemStack droppeditem = event.getItemDrop().getItemStack();

        if (droppeditem.hasItemMeta() && droppeditem.getItemMeta().hasDisplayName()) {

            for (String noallowed : nodrop) {
                if (LegacyComponentSerializer.legacySection().serialize(Objects.requireNonNull(droppeditem.getItemMeta().displayName())).equals(noallowed))
                    event.setCancelled(true);
            }
        }
    }
}
