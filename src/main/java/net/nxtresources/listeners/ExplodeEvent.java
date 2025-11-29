package net.nxtresources.listeners;

import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

import java.util.EnumSet;

public class ExplodeEvent implements Listener {

    private static final EnumSet<Material> REMOVABLE_TYPES = EnumSet.noneOf(Material.class);

    static {
        // Add specific materials
        REMOVABLE_TYPES.add(Material.CHEST);
        REMOVABLE_TYPES.add(Material.CONDUIT);
        REMOVABLE_TYPES.add(Material.DECORATED_POT);

        REMOVABLE_TYPES.addAll(Tag.BANNERS.getValues());
        REMOVABLE_TYPES.addAll(Tag.BEDS.getValues());
        REMOVABLE_TYPES.addAll(Tag.DOORS.getValues());
        REMOVABLE_TYPES.addAll(Tag.COPPER_GOLEM_STATUES.getValues());
        REMOVABLE_TYPES.addAll(Tag.ITEMS_HEAD_ARMOR.getValues());
        REMOVABLE_TYPES.addAll(Tag.ITEMS_SKULLS.getValues());
        REMOVABLE_TYPES.addAll(Tag.ALL_SIGNS.getValues());
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
//        e.blockList().forEach(Block::breakNaturally); //TODO Make only for sheep
//        e.setCancelled(true);
        Vector explCenter = e.getLocation().toVector();
        for (Block block : e.blockList()) {
            if (REMOVABLE_TYPES.contains(block.getType())) {
                block.setType(Material.AIR, false);
                continue;
            }
            // 1. Spawn the FallingBlock entity at the block's center
            FallingBlock fallingBlock = block.getWorld().spawn(
                    block.getLocation().add(0.5, 0, 0.5), // Location
                    FallingBlock.class,                   // Entity Class
                    fb -> fb.setBlockData(block.getBlockData()) // Consumer to configure the entity
            );

            // 2. Calculate velocity based on distance from explosion center
            Vector blockCenter = block.getLocation().toVector().add(new Vector(0.5, 0.5, 0.5));

            // Direction from explosion to block
            Vector velocity = blockCenter.subtract(explCenter);

            // Normalize and scale the velocity (adjust 0.5 or 1.2 to change "blast force")
            // You might want to clamp the Y value so they go up slightly
            velocity.normalize().multiply(0.8).setY(0.5);

            fallingBlock.setVelocity(velocity);

            // 3. Set the original block to AIR immediately so it looks like it "detached", physics is disabled, issues with wata and double chests
            block.setType(Material.AIR, false);

            // Optional: Prevent the block from dropping an item when it breaks
            fallingBlock.setDropItem(false);
        }
        // Clear the block list so the server doesn't try to break them (since we just did)
        e.blockList().clear();
    }

}
