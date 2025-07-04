package net.nxtresources.managers;

import com.google.common.reflect.TypeToken;
import com.google.gson.annotations.SerializedName;
import net.nxtresources.Main;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private record WorldDB(@SerializedName("L") String BlockLoc,@SerializedName("D") String BlockData) {}
//TODO HashSet vs ArrayList, Per-Chunk saving,Replace Gson?,Replace String BlockLoc with packed long or ints,Remove hashmap presize,Chunk snapshots async?,
    public void saveAsync(World wrld,String arName, BlockVector pos1, BlockVector pos2) {
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
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<Set<WorldDB>>> futures = new ArrayList<>();

            for (ChunkSnapshot snapshot : snapshots) {
                futures.add(executor.submit(() -> {
                    int chunkX = snapshot.getX() * 16;
                    int chunkZ = snapshot.getZ() * 16;
                    int startX = Math.max(minX, chunkX);
                    int endX = Math.min(maxX, chunkX + 15);
                    int startZ = Math.max(minZ, chunkZ);
                    int endZ = Math.min(maxZ, chunkZ + 15);
                    Set<WorldDB> chunkMap = new HashSet<>();

                    for (int x = startX; x <= endX; x++) {
                        for (int y = minY; y <= maxY; y++) {
                            for (int z = startZ; z <= endZ; z++) {
                                BlockData blockData = snapshot.getBlockData(x & 15, y, z & 15);
                                //OPT: No new blocks will be created.
                                if(blockData.getMaterial()== Material.AIR)
                                    continue;
                                chunkMap.add(new WorldDB(x+","+y+","+z,blockData.getAsString()));
                            }
                        }
                    }
                    return chunkMap;
                }));
            }

            // Combine results
            Set<WorldDB> result = new HashSet<>();
            for (Future<Set<WorldDB>> future : futures)
                result.addAll(future.get());
            Thread.ofVirtual().start(()->toDisk(arName,result));
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("WorldRAM SAVE EXCEPT",e);
        }
    }
//Runs every arena end.
    public void load(World wrld, String arName) {
        Thread.ofVirtual().start(()->{
            try {
                String json = Files.readString(Path.of("plugins\\FancySheepWars\\Arenas", arName + ".dat"));
                Set<WorldDB> wrldState = Main.gson.fromJson(json, new TypeToken<Set<WorldDB>>() {}.getType());
                Bukkit.getScheduler().runTask(Main.getInstance(),()-> wrldState.forEach(w-> {
                    String[] coords = w.BlockLoc.split(",");
                    wrld.getBlockAt(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2])).setBlockData(Bukkit.createBlockData(w.BlockData));
                }));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
//Start at sheepwars/run
    private void toDisk(String arName, Set<WorldDB> wrld) {
        Path dir = Path.of("plugins\\FancySheepWars\\Arenas");
        try {
            if(!Files.exists(dir))
                Files.createDirectory(dir);
            Files.write(dir.resolve(arName+".dat"), Main.gson.toJson(wrld).getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void rmLobby(World wrld,BlockVector pos1,BlockVector pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        Bukkit.getScheduler().runTask(Main.getInstance(),()->{
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        // false = don't run block physics (much faster when clearing large areas)
                        wrld.getBlockAt(x, y, z).setType(Material.AIR, false);
                    }
                }
            }
        });
    }

}
