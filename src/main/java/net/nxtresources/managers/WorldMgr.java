package net.nxtresources.managers;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

import java.util.HashMap;

public class WorldMgr {
//Singleton
    private WorldMgr(){}
    private static final WorldMgr inst = new WorldMgr();
    public static WorldMgr getInst() {
        return inst;
    }

    public HashMap<BlockVector, BlockData> save(World wrld, BlockVector pos1, BlockVector pos2) {
        HashMap<BlockVector, BlockData> map = new HashMap<>();
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = wrld.getBlockAt(x, y, z);
                    map.put(new BlockVector(x, y, z), block.getBlockData());
                }
            }
        }
        return map;
    }

    public void load(World wrld, HashMap<BlockVector, BlockData> wrldState) {
        for (HashMap.Entry<BlockVector, BlockData> entry : wrldState.entrySet()) {
            BlockVector pos = entry.getKey();
            BlockData data = entry.getValue();
            wrld.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()).setBlockData(data);
        }
    }

}
