package net.nxtresources.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.FallingBlock;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.util.Vector;

public class ExplodeEvent implements Listener {
    @EventHandler
    public void onEntityExplode(EntityExplodeEvent e) {
//        e.blockList().forEach(Block::breakNaturally); //TODO Make only for sheep
//        e.setCancelled(true);
        var t = System.currentTimeMillis();
        Vector explCenter = e.getLocation().toVector();
        for (Block block : e.blockList()) {
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
            velocity.normalize().multiply(0.8).setY(0.8);

            fallingBlock.setVelocity(velocity);

            // 3. Set the original block to AIR immediately so it looks like it "detached", physics is disabled, issues with wata and double chests
            block.setType(Material.AIR, false);

            // Optional: Prevent the block from dropping an item when it breaks
            fallingBlock.setDropItem(false);
        }
        System.out.println("Time: " + (System.currentTimeMillis()-t));
        // Clear the block list so the server doesn't try to break them (since we just did)
        e.blockList().clear();
    }

}
