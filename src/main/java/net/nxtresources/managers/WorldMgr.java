package net.nxtresources.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.stream.Stream;

public class WorldMgr {

    public static File server = Bukkit.getWorldContainer();

    public WorldMgr(){
        throw new UnsupportedOperationException("Utility class");
    }

    public static void copy(String name, String temp){
        Bukkit.broadcast(Component.text("c1"));
        File offWorld = new File(server, name);
        File tempWorld = new File(server, temp);
        Bukkit.broadcast(Component.text("c2"));
        Bukkit.broadcast(Component.text("Looking for: " + offWorld.getAbsolutePath()));
        if(!offWorld.exists())
            return;
        Bukkit.broadcast(Component.text("c3"));
        try {
            if (tempWorld.exists()) {
                Bukkit.broadcast(Component.text("c3"));
                try (Stream<Path> walk = Files.walk(tempWorld.toPath())) {
                    Bukkit.broadcast(Component.text("c4"));
                    walk.sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(f -> {
                                Bukkit.broadcast(Component.text("c5"));
                                if (!f.delete())
                                    throw new RuntimeException("File delete error: " + f.getAbsolutePath());
                                Bukkit.broadcast(Component.text("c6"));
                            });
                }
            }
            try (Stream<Path> walk = Files.walk(offWorld.toPath())) {
                walk.forEach(path -> {
                    try {
                        Path relative = offWorld.toPath().relativize(path);
                        Path destination = tempWorld.toPath().resolve(relative);
                        if (path.getFileName().toString().equals("session.lock") || path.getFileName().toString().equals("uid.dat"))
                            return;
                        if (Files.isDirectory(path))
                            Files.createDirectories(destination);
                        else
                            Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        throw new RuntimeException("Copy failed for: " + path, e);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void load(String name){
        File file = new File(server, name);
        if(!file.exists())
            return;
        Bukkit.createWorld(new WorldCreator(name));
    }

    public static boolean delete(String name) {
        World world = Bukkit.getWorld(name);
        if(world !=null)
            Bukkit.unloadWorld(name, false);
        File file = new File(server, name);
        try (Stream<Path> walk = Files.walk(file.toPath())) {
            walk.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(f -> {
                        if (!f.delete())
                            throw new RuntimeException("File delete error: " + f.getAbsolutePath());
                    });
            return true;
        }catch (IOException e){
            throw new RuntimeException("WorldManager -> WorldDeleteError: " + e);
        }
    }
}
