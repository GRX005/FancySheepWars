package net.nxtresources.commands;

import net.nxtresources.Main;
import net.nxtresources.managers.Arena;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.SetupManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CMDHandler implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage("Ingame only!");
            return false;
        }
//        if(!(sender.hasPermission("fancysheepwars.use"))) {
//            sender.sendMessage("§cNincs jogosultságod a parancs használatához!");
//            return false;
//        }
        if(args.length == 0){
            sender.sendMessage("fancy sheepwars help: ");
            sender.sendMessage("/sheepwars create <name> ");
            sender.sendMessage("/sheepwars delete <name> ");
            sender.sendMessage("/sheepwars list");
            sender.sendMessage("/sheepwars join <name> ");
            sender.sendMessage("/sheepwars leave ");
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "c","create" ->{
                if(args.length< 3){
                    sender.sendMessage("Használat: /sheepwars create <név> <meret(csak paros)>");
                    return false;
                }
                String name =args[1];
                int size;
                try {
                    size = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Szamot adj meg.");
                    return false;
                }
                if(size%2!=0) {
                    sender.sendMessage("Csak paros lehet");
                    return false;
                }
                if(size<1) {
                    sender.sendMessage("Nem lehet minus");
                    return false;
                }
                for(Arena a : ArenaMgr.arenas) {
                    if(Objects.equals(a.name, name)) {
                        sender.sendMessage("Mar van arena mely az adott nevre hallgat.");
                        return false;
                    }
                }
                Arena arena =ArenaMgr.make(name, size);
                ArenaMgr.saveArena(arena);
                Main.saveArenaConfig();
                sender.sendMessage("aréna létrehozva a következő néven: " + name + " es merettel: " + size);
                return true;
            }
            case "d","delete" ->{
                if(args.length< 2){
                    sender.sendMessage("Használat: /sheepwars delete <név>");
                    return false;
                }
                String name =args[1];
                switch (ArenaMgr.del(name)) {
                    case 0-> {
                        sender.sendMessage("aréna törölve a következő néven: " + name);
                        return true;
                    }
                    case 1->sender.sendMessage("Nincs ilyen arena");
                    case 2-> sender.sendMessage("Vannak az arenaban, nem torolheted.");
                }
                return false;


            }
            case "l","list" -> {
                sender.sendMessage("Elérhető arénák: ");
                ArenaMgr.arenas.forEach(a->{
                    sender.sendMessage("Arena neve: "+a.name+", merete: "+ a.size+", LobbyPlayerek: "+a.lobbyPlayers + " Teamek: ");
                    a.teams.forEach(t->{
                        sender.sendMessage(t.type+": ");
                        t.tPlayers.forEach(p->sender.sendMessage(p.toString()));
                    });
                });
                return true;
            }

            case "join","j" -> {
                if(args.length <2){
                    sender.sendMessage("Használat: /sheepwars join <név>");
                    return false;
                }
                String name = args[1];

                switch (ArenaMgr.join(name,player)) {
                    case 0-> {
                        sender.sendMessage("Csatlakoztál a következő arénához: " + name + "!");
                        return true;
                    }
                    case 1-> sender.sendMessage("Nem findoltam az arénát.");
                    case 2-> sender.sendMessage("Mar arenaban vagy.");
                    case 3-> sender.sendMessage("Az arena tele.");
                    case 4-> sender.sendMessage("Az arena mar elindult.");
                }
                return false;

            }
            case "leave","le" -> {
                if(!ArenaMgr.isInArena(player)) {
                    sender.sendMessage("Nem vagy arenaban.");
                    return false;
                }
                ArenaMgr.leave(player);
                sender.sendMessage("Kiléptél az arénából!");
                return true;

            }

            case "setlobby" -> {
                Main.setupManager.setMainLobby(player);
                sender.sendMessage("MainLobby sikeresen beállítva!");
                return true;
            }
            case "lobby" -> {
                Main.setupManager.getMainLobby(player);
                sender.sendMessage("MainLobbyra teleportáltál!");
                return true;
            }
            case "setup" -> {
                if(args.length < 2) {
                    sender.sendMessage("Használat: /sheepwars setup <név>");
                    return false;
                }

                String name = args[1];
                sender.sendMessage("Éppen beállítod A(z) " + name + " arénát!");
                return true;

            }
            //TESZT DOLGOK AMIKET MAJD TÖRÖLNI FOGOK
            case "setwaitinglobby" ->{
                if(args.length <2){
                    sender.sendMessage("Használat: /sheepwars setwaitinglobby <név>");
                    return false;
                }
                String name = args[1];
                SetupManager.setWaitingLobby(player, name);
                return true;

            }

            case "reload" -> {
                if (!sender.hasPermission("sheepwars.*") && !sender.hasPermission("sheepwars.reload")) {
                    sender.sendMessage("Nincs jogod ehhez!");
                    return false;
                }
                if(args.length < 2) {
                    sender.sendMessage("Plugin újratöltése folyamatban...");
                    long started = System.currentTimeMillis();
                    Main.getInstance().reload();
                    long endTime = System.currentTimeMillis();
                    long completed = endTime - started;
                    sender.sendMessage("§aPlugin sikeresen újratöltve! §2(§a" + completed + " ms§2)");
                    return true;
                }
            }
            default -> sender.sendMessage("Érvénytelen argumentum!");
        }
        return false;
    }
}
