package net.nxtresources.managers;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.nxtresources.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
    private record WorldDB(int x, int y, int z, String BlockData) {}

    private static final long MAGIC = 0x4172656E61444200L; // "ArenaDB"
    private static final byte VERSION = 1;

//TODO HashSet vs ArrayList, Per-Chunk saving,Replace String BlockLoc with packed long or ints,Remove hashmap presize,Chunk snapshots async?,
    public void saveAsync(World wrld,String arName, BlockVector pos1, BlockVector pos2) {
        // Calculate region bounds
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());//AsdTest
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());
        // Determine chunk boundaries
        List<ChunkSnapshot> snapshots = new ArrayList<>();
        for (int cx = minX>>4; cx <= (maxX>>4); cx++)
            for (int cz = minZ>>4; cz <= (maxZ>>4); cz++)
                snapshots.add(wrld.getChunkAt(cx, cz).getChunkSnapshot());

        // Process snapshots in parallel with all threads
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<List<WorldDB>>> futures = new ArrayList<>();

            for (ChunkSnapshot snapshot : snapshots) {
                futures.add(executor.submit(() -> {
                    int chunkX = snapshot.getX() * 16;
                    int chunkZ = snapshot.getZ() * 16;
                    int startX = Math.max(minX, chunkX);
                    int endX = Math.min(maxX, chunkX + 15);
                    int startZ = Math.max(minZ, chunkZ);
                    int endZ = Math.min(maxZ, chunkZ + 15);
                    List<WorldDB> chunkMap = new ArrayList<>();

                    for (int x = startX; x <= endX; x++)
                        for (int y = minY; y <= maxY; y++)
                            for (int z = startZ; z <= endZ; z++) {
                                BlockData blockData = snapshot.getBlockData(x & 15, y, z & 15);
                                //OPT: No new blocks will be created.
                                if (blockData.getMaterial().isAir())
                                    continue;
                                chunkMap.add(new WorldDB(x, y, z, blockData.getAsString()));
                            }
                    return chunkMap;
                }));
            }

            // Combine results
            List<WorldDB> result = new ArrayList<>();
            for (Future<List<WorldDB>> future : futures)
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

    //Start at sheepwars/arenaFinish
    private void toDisk(String arName, List<WorldDB> wrld) throws IOException{

        wrld.sort(Comparator.comparingInt(WorldDB::x)
                .thenComparingInt(WorldDB::y)
                .thenComparingInt(WorldDB::z));

        var stateToId = new Object2IntOpenHashMap<String>();
        stateToId.defaultReturnValue(-1);

        for (WorldDB b : wrld)
            stateToId.putIfAbsent(b.BlockData, stateToId.size());

        int tableSize = stateToId.size();
        if (tableSize > 65535) throw new IOException("Too many unique block states: " + tableSize);

        var pth = Path.of("plugins","FancySheepWars","Arenas");
        if (!Files.exists(pth))
            Files.createDirectory(pth);

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

                writeSignedVarInt(out, b.x - prevX);
                writeSignedVarInt(out, b.y - prevY);
                writeSignedVarInt(out, b.z - prevZ);

                prevX = b.x; prevY = b.y; prevZ = b.z;

                int id = stateToId.getInt(b.BlockData);
                writeVarInt(out, id);
            }
        }
    }

//Runs every arena end.
    public void toWrld(World wrld, String arName) {//TODO ENABLE PYSIC UPDATE ON WATER PLACEMENT?
        Thread.ofVirtual().name("ArenaLoad-" + arName).start(() -> {
            var start = System.currentTimeMillis();
            try {
                List<WorldDB> blocks = load(arName);

                // Pre-parse everything once on the virtual thread
                record PlacedBlock(int x, int y, int z, BlockData data) {}
                List<PlacedBlock> toPlace = new ArrayList<>(blocks.size());

                for (WorldDB wb : blocks) {
                    BlockData data = Bukkit.createBlockData(wb.BlockData); // still needed once
                    toPlace.add(new PlacedBlock(wb.x, wb.y, wb.z, data));
                }

                // NOW switch to main thread and blast blocks as fast as possible
                final long timeBudgetNs = 3_000_000L; // 3 ms per tick budget (tune this: 2-6 ms typical)
                long startAll = System.currentTimeMillis();
                final int[] index = {0};
                Bukkit.getScheduler().runTaskTimer(Main.getInstance(), t-> {
                    long tickStart = System.nanoTime();
                    while (index[0] < toPlace.size()) {
                        // stop if we've reached time budget for this tick
                        if (System.nanoTime() - tickStart > timeBudgetNs) break;

                        PlacedBlock pb = toPlace.get(index[0]++);
                        // If you can, ensure chunk is loaded beforehand to avoid chunk-load lag here
                        Block b = wrld.getBlockAt(pb.x, pb.y, pb.z);
                        b.setBlockData(pb.data, false);
                    }

                    // finished
                    if (index[0] >= toPlace.size()) {
                        t.cancel();
                        long totalMs = System.currentTimeMillis() - startAll;
                        Main.getInstance().getLogger().info("AllTime: " + totalMs + "ms");
                    }
                }, 0L,1L); //Run every tick

                Main.getInstance().getLogger().info("AllTime: "+(System.currentTimeMillis()-start));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static List<WorldDB> load(String arName) throws IOException {
        try (var in = new DataInputStream(new BufferedInputStream(
                new InflaterInputStream(Files.newInputStream(Path.of("plugins","FancySheepWars","Arenas", arName + ".dat")))))) {

            if (in.readLong() != MAGIC) throw new IOException("Not an ArenaDB file");
            if (in.readByte() != VERSION) throw new IOException("Unsupported ArenaDB file version");

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

                list.add(new WorldDB(x, y, z, table[id]));
            }
            list.trimToSize();
            return List.copyOf(list);
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
            for (int x = minX; x <= maxX; x++)
                for (int y = minY; y <= maxY; y++)
                    for (int z = minZ; z <= maxZ; z++) {
                        // false = don't run block physics (much faster when clearing large areas)
                        wrld.getBlockAt(x, y, z).setType(Material.AIR, false);
                    }
        });
    }

}
