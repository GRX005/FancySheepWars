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

    String prefix = Main.messagesConfig.getString("Prefix");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage("Ingame only!");
            return false;
        }
        if(!(sender.hasPermission("fancysheepwars.use"))) {
            sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("No-Permission")).replace("%prefix%", prefix)));
            return false;
        }
        if(args.length == 0){
            if(!(player.hasPermission("fancysheepwars.use"))){
                sender.sendMessage("Player help: ");
                sender.sendMessage("/sheepwars join");
                sender.sendMessage("/sheepwars leave");

            } else {
            sender.sendMessage("fancy sheepwars help: ");
            sender.sendMessage("/sheepwars create <name> ");
            sender.sendMessage("/sheepwars delete <name> ");
            sender.sendMessage("/sheepwars list");
            sender.sendMessage("/sheepwars join <name> ");
            sender.sendMessage("/sheepwars leave ");
            }
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "c","create" ->{
                if(args.length< 3){
                    sender.sendMessage(Main.translateColorCodes(
                            Objects.requireNonNull(Main.messagesConfig.getString("Usage"))
                                    .replace("%prefix%", prefix)
                                    .replace("%usage%", "/sheepwars create <name> <size(Pairs only)>")
                    ));
                    return false;
                }
                String name =args[1];
                int size;
                try {
                    size = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.NumberOnly")).replace("%prefix%", prefix)));
                    return false;
                }
                if(size%2!=0) {
                    sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.ParisOnly")).replace("%prefix%", prefix)));
                    return false;
                }
                if(size<1) {
                    sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.CanPositive")).replace("%prefix%", prefix)));
                    return false;
                }
                for(Arena a : ArenaMgr.arenas) {
                    if(Objects.equals(a.name, name)) {
                        sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.ItAlreadyExists")).replace("%prefix%", prefix)));
                        return false;
                    }
                }
                for (Arena a : SetupManager.temporaryArenas) {
                    if (a.name.equalsIgnoreCase(name)) {
                        sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.Setup.AlreadyInProgress")).replace("%prefix%", prefix)));
                        return false;
                    }
                }
                switch (SetupManager.startSetup(player, name, size, true)) {
                    case 0 -> sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.Setup.Reloaded")).replace("%arena_name%", name).replace("%prefix%", prefix)));
                    case 1 -> sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.NoSuchArena")).replace("%prefix%", prefix)));
                    case 2 -> sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.Setup.AlreadySettingUp")).replace("%prefix%", prefix)));
                }
                return false;
            }
            case "d","delete" ->{
                if(args.length< 2){
                    sender.sendMessage(Main.translateColorCodes(
                            Objects.requireNonNull(Main.messagesConfig.getString("Usage"))
                                    .replace("%prefix%", prefix)
                                    .replace("%usage%", "/sheepwars delete <name>")
                    ));
                    return false;
                }
                String name =args[1];
                switch (ArenaMgr.del(name)) {
                    case 0-> {
                        sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.Delete")).replace("%arena_name%", name).replace("%prefix%", prefix)));
                        return true;
                    }
                    case 1->sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.NoSuchArena")).replace("%prefix%", prefix)));
                    case 2-> sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.PlayersInside")).replace("%prefix%", prefix)));
                }
                return false;


            }
            case "l","list" -> {
                sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.Arenas")).replace("%prefix%", prefix)));
                ArenaMgr.arenas.forEach(a->{
                    sender.sendMessage("§6Arena name: §e"+a.name+"§6, Size: §e"+ a.size+"§6, LobbyPlayers: §e"+a.lobbyPlayers +"§6, Status: §e"+ a.stat +"§6 Teams: ");
                    a.teams.forEach(t->{
                        sender.sendMessage("§a"+t.type+": ");
                        t.tPlayers.forEach(p->sender.sendMessage("§9"+p.toString()));
                    });
                });
                return true;
            }

            case "join","j" -> {
                if(args.length <2){
                    sender.sendMessage(Main.translateColorCodes(
                            Objects.requireNonNull(Main.messagesConfig.getString("Usage"))
                                    .replace("%prefix%", prefix)
                                    .replace("%usage%", "/sheepwars join <name>")
                    ));
                    return false;
                }
                String name = args[1];

                switch (ArenaMgr.join(name,player)) {
                    case 0-> {
                        sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.Join")).replace("%arena_name%", name).replace("%prefix%", prefix)));
                        return true;
                    }
                    case 1-> sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.NoSuchArena")).replace("%prefix%", prefix)));
                    case 2-> sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.AlreadyInArena")).replace("%prefix%", prefix)));
                    case 3-> sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.ArenaIsFull")).replace("%prefix%", prefix)));
                    case 4-> sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.ArenaStarted")).replace("%prefix%", prefix)));
                    case 5-> sender.sendMessage("Waitinglobby=null");
                }
                return false;

            }
            case "leave","le" -> {
                if(!ArenaMgr.isInArena(player)) {
                    sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.NotInAnArena")).replace("%prefix%", prefix)));
                    return false;
                }
                ArenaMgr.leave(player);
                sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Arena.Leave")).replace("%prefix%", prefix)));
                return true;

            }

            case "setlobby" -> {
                SetupManager.setMainLobby(player);
                sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("SetMainLobby")).replace("%prefix%", prefix)));
                return true;
            }
            case "lobby" -> {
                SetupManager.getMainLobby(player);
                sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("GetMainLobby")).replace("%prefix%", prefix)));
                return true;
            }

            case "rl","reload" -> {
                if (!sender.hasPermission("sheepwars.*") && !sender.hasPermission("sheepwars.reload")) {
                    sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("No-Permission")).replace("%prefix%", prefix)));
                    return false;
                }
                if(args.length < 2) {
                    sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Reloading")).replace("%prefix%", prefix)));
                    long started = System.currentTimeMillis();
                    Main.getInstance().reload();
                    long endTime = System.currentTimeMillis();
                    long completed = endTime - started;
                    sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Reloaded")).replace("%ms%", completed + "").replace("%prefix%", prefix)));
                    return true;
                }
            }
            default -> sender.sendMessage(Main.translateColorCodes(Objects.requireNonNull(Main.messagesConfig.getString("Invalid-argument")).replace("%prefix%", prefix)));
        }
        return false;
    }
}
