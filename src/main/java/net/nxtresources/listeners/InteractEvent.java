package net.nxtresources.listeners;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.nxtresources.menus.ArenaSelectorGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class InteractEvent implements Listener {

    @EventHandler
    public void openStatusMenu(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock() != null)) {
            ItemStack statsItem = player.getInventory().getItemInMainHand();
            if (statsItem.getType() == Material.BOOK&& statsItem.hasItemMeta() && statsItem.getItemMeta().hasDisplayName()) {
                if ( LegacyComponentSerializer.legacySection().serialize(Objects.requireNonNull(statsItem.getItemMeta().displayName())).equals("§eAréna választó")) {
                    ArenaSelectorGui.open(player);
                }
            }
        }
    }
}


