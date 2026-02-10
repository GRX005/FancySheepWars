package net.nxtresources.managers;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.nxtresources.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChunkSnapshot;
import org.bukkit.Material;
import org.bukkit.World;
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
import java.util.logging.Level;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public final class WorldMgr {
    private WorldMgr() {}
    private static final WorldMgr inst = new WorldMgr();
    public static WorldMgr getInst() { return inst; }

    private static final long MAGIC = 0x4172656E61444200L; //"ArenaDB"
    private static final byte VERSION = 1;
    private static final Path ARENA_DIR = Path.of("plugins", "FancySheepWars", "Arenas");

    // Packed block: x(26 bits) | y(16 bits) | z(26 bits) stored as long, plus palette id
    private record BlockEntry(int x, int y, int z, int paletteId) {}

    // ─── SAVE ────────────────────────────────────────────────────────────

    public void saveAsync(World wrld, String arName, BlockVector pos1, BlockVector pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        // Snapshot chunks on main thread
        int cxMin = minX >> 4, cxMax = maxX >> 4, czMin = minZ >> 4, czMax = maxZ >> 4;
        List<ChunkSnapshot> snapshots = new ArrayList<>((cxMax - cxMin + 1) * (czMax - czMin + 1));
        for (int cx = cxMin; cx <= cxMax; cx++)
            for (int cz = czMin; cz <= czMax; cz++)
                snapshots.add(wrld.getChunkAt(cx, cz).getChunkSnapshot());

        // Build palette + entries off main thread in parallel
        Thread.ofVirtual().name("ArenaSave-" + arName).start(() -> {
            try {
                // Shared palette built single-pass per chunk, merged after
                var globalPalette = new Object2IntOpenHashMap<String>(64);
                globalPalette.defaultReturnValue(-1);

                record RawBlock(int x, int y, int z, String state) {}
                try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    List<Future<List<RawBlock>>> futures = new ArrayList<>(snapshots.size());

                    for (ChunkSnapshot snap : snapshots) {
                        futures.add(executor.submit(() -> {
                            int cx = snap.getX() << 4, cz = snap.getZ() << 4;
                            int sx = Math.max(minX, cx),     ex = Math.min(maxX, cx + 15);
                            int sz = Math.max(minZ, cz),     ez = Math.min(maxZ, cz + 15);
                            List<RawBlock> out = new ArrayList<>();

                            for (int x = sx; x <= ex; x++)
                                for (int y = minY; y <= maxY; y++)
                                    for (int z = sz; z <= ez; z++) {
                                        var bd = snap.getBlockData(x & 15, y, z & 15);
                                        if (!bd.getMaterial().isAir())
                                            out.add(new RawBlock(x, y, z, bd.getAsString()));
                                    }
                            return out;
                        }));
                    }

                    // Merge + build palette + sort in one pass
                    List<RawBlock> allRaw = new ArrayList<>();
                    for (var f : futures) allRaw.addAll(f.get());

                    allRaw.sort(Comparator.comparingInt(RawBlock::x)
                            .thenComparingInt(RawBlock::y)
                            .thenComparingInt(RawBlock::z));

                    // Build palette and final entries
                    List<BlockEntry> entries = new ArrayList<>(allRaw.size());
                    for (RawBlock rb : allRaw) {
                        int id = globalPalette.getInt(rb.state);
                        if (id == -1) {
                            id = globalPalette.size();
                            globalPalette.put(rb.state, id);
                        }
                        entries.add(new BlockEntry(rb.x, rb.y, rb.z, id));
                    }

                    // Build ordered string table
                    int tableSize = globalPalette.size();
                    if (tableSize > 65535) throw new IOException("Too many unique block states: " + tableSize);
                    String[] table = new String[tableSize];
                    for (var e : globalPalette.object2IntEntrySet()) table[e.getIntValue()] = e.getKey();

                    toDisk(arName, entries, table);
                }
            } catch (ExecutionException | InterruptedException | IOException e) {
                Main.getInstance().getLogger().log(Level.SEVERE, "ArenaSave failed: " + arName, e);
            }
        });
    }

    private void toDisk(String arName, List<BlockEntry> blocks, String[] table) throws IOException {
        Files.createDirectories(ARENA_DIR);
        Path pth = ARENA_DIR.resolve(arName + ".dat");

        try (var out = new DataOutputStream(new BufferedOutputStream(
                new DeflaterOutputStream(Files.newOutputStream(pth), new Deflater(Deflater.BEST_COMPRESSION)), 1 << 16))) {

            out.writeLong(MAGIC);
            out.writeByte(VERSION);
            out.writeInt(blocks.size());
            out.writeShort(table.length);

            for (String state : table) {
                byte[] bytes = state.getBytes(StandardCharsets.UTF_8);
                out.writeShort(bytes.length);
                out.write(bytes);
            }

            int prevX = 0, prevY = 0, prevZ = 0;
            for (BlockEntry b : blocks) {
                writeSignedVarInt(out, b.x - prevX);
                writeSignedVarInt(out, b.y - prevY);
                writeSignedVarInt(out, b.z - prevZ);
                prevX = b.x; prevY = b.y; prevZ = b.z;
                writeVarInt(out, b.paletteId);
            }
        }
    }

    // ─── LOAD ────────────────────────────────────────────────────────────

    public void toWrld(World wrld, String arName) {
        Thread.ofVirtual().name("ArenaLoad-" + arName).start(() -> {
            try {
                // Parse file + create BlockData off main thread
                record PlacedBlock(int x, int y, int z, BlockData data) {}

                List<PlacedBlock> toPlace;
                try (var in = new DataInputStream(new BufferedInputStream(
                        new InflaterInputStream(Files.newInputStream(ARENA_DIR.resolve(arName + ".dat"))), 1 << 16))) {

                    if (in.readLong() != MAGIC) throw new IOException("Not an ArenaDB file");
                    if (in.readByte() != VERSION) throw new IOException("Unsupported version");

                    int count = in.readInt();
                    int tableSize = in.readUnsignedShort();

                    // Read palette and pre-parse BlockData once
                    BlockData[] palette = new BlockData[tableSize];
                    for (int i = 0; i < tableSize; i++) {
                        byte[] b = new byte[in.readUnsignedShort()];
                        in.readFully(b);
                        palette[i] = Bukkit.createBlockData(new String(b, StandardCharsets.UTF_8));
                    }

                    toPlace = new ArrayList<>(count);
                    int x = 0, y = 0, z = 0;
                    for (int i = 0; i < count; i++) {
                        x += readSignedVarInt(in);
                        y += readSignedVarInt(in);
                        z += readSignedVarInt(in);
                        toPlace.add(new PlacedBlock(x, y, z, palette[readVarInt(in)]));
                    }
                }

                // Place blocks on main thread, time-sliced
                final long BUDGET_NS = 3_000_000L;
                final int[] idx = {0};
                var t = System.currentTimeMillis();
                Bukkit.getScheduler().runTaskTimer(Main.getInstance(), task -> {
                    long tickStart = System.nanoTime();
                    while (idx[0] < toPlace.size()) {
                        if ((idx[0] & 63) == 0 && System.nanoTime() - tickStart > BUDGET_NS) return;
                        PlacedBlock pb = toPlace.get(idx[0]++);
                        wrld.getBlockAt(pb.x, pb.y, pb.z).setBlockData(pb.data, false);
                    }
                    task.cancel();
                    Main.getInstance().getLogger().info(arName + " loaded in " + (System.currentTimeMillis() - t) + "ms");
                }, 0L, 1L);

            } catch (Exception e) {
                Main.getInstance().getLogger().log(Level.SEVERE, "ArenaLoad failed: " + arName, e);
            }
        });
    }

    // ─── CLEAR ───────────────────────────────────────────────────────────

    public void rmLobby(World wrld, BlockVector pos1, BlockVector pos2) {
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX()), maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY()), maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ()), maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
            for (int x = minX; x <= maxX; x++)
                for (int y = minY; y <= maxY; y++)
                    for (int z = minZ; z <= maxZ; z++)
                        wrld.getBlockAt(x, y, z).setType(Material.AIR, false);
        });
    }

    // ─── VARINT ──────────────────────────────────────────────────────────

    private static void writeSignedVarInt(DataOutput out, int v) throws IOException {
        writeVarInt(out, (v << 1) ^ (v >> 31));
    }

    private static int readSignedVarInt(DataInput in) throws IOException {
        int r = readVarInt(in);
        return (r >>> 1) ^ -(r & 1);
    }

    private static void writeVarInt(DataOutput out, int v) throws IOException {
        while ((v & ~0x7F) != 0) { out.writeByte((v & 0x7F) | 0x80); v >>>= 7; }
        out.writeByte(v);
    }

    private static int readVarInt(DataInput in) throws IOException {
        int result = 0, shift = 0, b;
        while (((b = in.readUnsignedByte()) & 0x80) != 0) { result |= (b & 0x7F) << shift; shift += 7; }
        return result | (b << shift);
    }
}