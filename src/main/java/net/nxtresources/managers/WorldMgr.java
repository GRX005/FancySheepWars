package net.nxtresources.managers;

import org.bukkit.Chunk;
import org.bukkit.ChunkSnapshot;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class WorldMgr {
    //Singleton
    private WorldMgr(){}
    private static final WorldMgr inst = new WorldMgr();
    public static WorldMgr getInst() {
        return inst;
    }

    public HashMap<BlockVector, BlockData> saveAsync(World wrld, BlockVector pos1, BlockVector pos2) {
        // Calculate region bounds
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        // Determine chunk boundaries
        int minChunkX = minX >> 4;
        int maxChunkX = maxX >> 4;
        int minChunkZ = minZ >> 4;
        int maxChunkZ = maxZ >> 4;

        // Collect chunk snapshots on the main thread
        List<ChunkSnapshot> snapshots = new ArrayList<>();
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                Chunk chunk = wrld.getChunkAt(chunkX, chunkZ);
                snapshots.add(chunk.getChunkSnapshot());
            }
        }

        // Process snapshots in parallel with all threads
        try (ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            List<Future<HashMap<BlockVector, BlockData>>> futures = new ArrayList<>();

            for (ChunkSnapshot snapshot : snapshots) {
                futures.add(executor.submit(() -> {
                    int chunkX = snapshot.getX() * 16;
                    int chunkZ = snapshot.getZ() * 16;
                    int startX = Math.max(minX, chunkX);
                    int endX = Math.min(maxX, chunkX + 15);
                    int startZ = Math.max(minZ, chunkZ);
                    int endZ = Math.min(maxZ, chunkZ + 15);
                    int blocksInChunk = (endX - startX + 1) * (maxY - minY + 1) * (endZ - startZ + 1);
                    HashMap<BlockVector, BlockData> chunkMap = new HashMap<>(blocksInChunk);

                    for (int x = startX; x <= endX; x++) {
                        for (int y = minY; y <= maxY; y++) {
                            for (int z = startZ; z <= endZ; z++) {
                                BlockData blockData = snapshot.getBlockData(x & 15, y, z & 15);
                                chunkMap.put(new BlockVector(x, y, z), blockData);
                            }
                        }
                    }
                    return chunkMap;
                }));
            }

            // Combine results
            long totalBlocks = (long)(maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
            int mapCapacity = (int) Math.min(totalBlocks, Integer.MAX_VALUE - 8);
            HashMap<BlockVector, BlockData> result = new HashMap<>(mapCapacity);
            for (Future<HashMap<BlockVector, BlockData>> future : futures) {
                result.putAll(future.get());
            }

            return result;
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("WorldRAM SAVE EXCEPT",e);
        }
    }

    public void load(World wrld, HashMap<BlockVector, BlockData> wrldState) {
        for (HashMap.Entry<BlockVector, BlockData> entry : wrldState.entrySet()) {
            BlockVector pos = entry.getKey();
            BlockData data = entry.getValue();
            wrld.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()).setBlockData(data);
        }
    }

}
