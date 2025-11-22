package net.nxtresources.managers;

import com.google.gson.annotations.SerializedName;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.nxtresources.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class WorldMgr {
    //Singleton
    private WorldMgr(){}
    private static final WorldMgr inst = new WorldMgr();
    public static WorldMgr getInst() {
        return inst;
    }
    private record WorldDB(@SerializedName("L") String BlockLoc,@SerializedName("D") String BlockData) {}

    private static final long MAGIC = 0x4172656E61444200L; // "WORLDBUL"
    private static final byte VERSION = 1;

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
            List<WorldDB> result = new ArrayList<>();
            for (Future<Set<WorldDB>> future : futures)
                result.addAll(future.get());
            Thread.ofVirtual().start(()-> {
                try {
                    toDisk(arName,result);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException("WorldRAM SAVE EXCEPT",e);
        }
    }
//Runs every arena end.
    public void toWrld(World wrld, String arName) {
        Thread.ofVirtual().name("ArenaLoad-" + arName).start(() -> {
            try {
                List<WorldDB> blocks = load(arName);

                // Pre-parse everything once on the virtual thread
                record PlacedBlock(int x, int y, int z, BlockData data) {}
                List<PlacedBlock> toPlace = new ArrayList<>(blocks.size());

                for (WorldDB wb : blocks) {
                    String[] parts = wb.BlockLoc.split(","); // still cheap compared to the rest
                    int x = Integer.parseInt(parts[0]);
                    int y = Integer.parseInt(parts[1]);
                    int z = Integer.parseInt(parts[2]);
                    BlockData data = Bukkit.createBlockData(wb.BlockData); // still needed once
                    toPlace.add(new PlacedBlock(x, y, z, data));
                }

                // NOW switch to main thread and blast blocks as fast as possible
                Bukkit.getScheduler().runTask(Main.getInstance(), () -> {

                    for (PlacedBlock pb : toPlace) {
                        Block b = wrld.getBlockAt(pb.x, pb.y, pb.z);
                        // Fastest possible way:
                        b.setBlockData(pb.data, false); // false = no physics
                    }

                    Bukkit.getConsoleSender().sendMessage("Arena '" + arName + "' loaded (" + toPlace.size() + " blocks)");
                });

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static List<WorldDB> load(String arName) throws IOException {
        try (var in = new DataInputStream(new BufferedInputStream(
                new InflaterInputStream(Files.newInputStream(Path.of("plugins\\FancySheepWars\\Arenas"+"\\"+arName+".dat")))))) {

            if (in.readLong() != MAGIC) throw new IOException("Not a WorldDBUltra file");
            if (in.readByte() != VERSION) throw new IOException("Unsupported version");

            int count = in.readInt();
            int tableSize = in.readUnsignedShort();

            String[] table = new String[tableSize];
            for (int i = 0; i < tableSize; i++) {
                int len = in.readUnsignedShort();
                byte[] b = new byte[len];
                in.readFully(b);
                table[i] = new String(b, StandardCharsets.UTF_8);
            }

            var list = new ArrayList<WorldDB>(count);
            int x = 0, y = 0, z = 0;

            for (int i = 0; i < count; i++) {
                x += readSignedVarInt(in);
                y += readSignedVarInt(in);
                z += readSignedVarInt(in);
                int id = readVarInt(in);

                list.add(new WorldDB(x + "," + y + "," + z, table[id]));
            }
            list.trimToSize();
            return List.copyOf(list);
        }
    }

//Start at sheepwars/run
    private void toDisk(String arName, List<WorldDB> wrld) throws IOException{

        wrld.sort((a, b) -> {
            String[] pa = a.BlockLoc.split(",");
            String[] pb = b.BlockLoc.split(",");
            int xa = Integer.parseInt(pa[0]), ya = Integer.parseInt(pa[1]), za = Integer.parseInt(pa[2]);
            int xb = Integer.parseInt(pb[0]), yb = Integer.parseInt(pb[1]), zb = Integer.parseInt(pb[2]);

            int cmp = Integer.compare(xa, xb);
            if (cmp != 0) return cmp;
            cmp = Integer.compare(ya, yb);
            if (cmp != 0) return cmp;
            return Integer.compare(za, zb);
        });

        var stateToId = new Object2IntOpenHashMap<String>();
        stateToId.defaultReturnValue(-1);

        for (WorldDB b : wrld) {
            stateToId.putIfAbsent(b.BlockData, stateToId.size());
        }

        int tableSize = stateToId.size();
        if (tableSize > 65535) throw new IOException("Too many unique block states: " + tableSize);

        var pth = Path.of("plugins\\FancySheepWars\\Arenas");
        if (!Files.exists(pth)) {
            Files.createDirectory(pth);
        }

        pth = pth.resolve(arName+".dat");
        Files.createFile(pth);

        try (var out = new DataOutputStream(new BufferedOutputStream(
                new DeflaterOutputStream(Files.newOutputStream(pth), new Deflater(Deflater.BEST_COMPRESSION))))) {

            out.writeLong(MAGIC);
            out.writeByte(VERSION);
            out.writeInt(wrld.size());
            out.writeShort(tableSize);

            // Write string table
            String[] table = new String[tableSize];
            for (var e : stateToId.object2IntEntrySet()) {
                int id = e.getIntValue();
                String state = e.getKey();
                if (id < 0 || id >= tableSize) throw new IOException("Invalid state id");
                table[id] = state;
            }

            // Write string table in id order (0..tableSize-1)
            for (int i = 0; i < tableSize; i++) {
                String state = table[i];
                if (state == null) throw new IOException("Missing state for id " + i);
                byte[] bytes = state.getBytes(StandardCharsets.UTF_8);
                if (bytes.length > 65535) throw new IOException("Block state too long");
                out.writeShort(bytes.length);
                out.write(bytes);
            }

            // Delta encoding
            int prevX = 0, prevY = 0, prevZ = 0;
            for (WorldDB b : wrld) {
                String[] p = b.BlockLoc.split(",");
                int x = Integer.parseInt(p[0]);
                int y = Integer.parseInt(p[1]);
                int z = Integer.parseInt(p[2]);

                writeSignedVarInt(out, x - prevX);
                writeSignedVarInt(out, y - prevY);
                writeSignedVarInt(out, z - prevZ);

                prevX = x; prevY = y; prevZ = z;

                int id = stateToId.getInt(b.BlockData);
                writeVarInt(out, id);
            }
        }
    }

    private static void writeSignedVarInt(DataOutput out, int value) throws IOException {
        writeVarInt(out, (value << 1) ^ (value >> 31));
    }

    private static int readSignedVarInt(DataInput in) throws IOException {
        int raw = readVarInt(in);
        return (raw >>> 1) ^ -(raw & 1);
    }

    private static void writeVarInt(DataOutput out, int value) throws IOException {
        while (true) {
            if ((value & ~0x7F) == 0) {
                out.writeByte(value);
                return;
            }
            out.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
    }

    private static int readVarInt(DataInput in) throws IOException {
        int result = 0;
        int shift = 0;
        int b;
        while (((b = in.readUnsignedByte()) & 0x80) != 0) {
            result |= (b & 0x7F) << shift;
            shift += 7;
        }
        return result | (b << shift);
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
