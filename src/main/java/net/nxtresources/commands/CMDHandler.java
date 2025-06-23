package net.nxtresources.commands;

import net.nxtresources.Main;
import net.nxtresources.managers.Arena;
import net.nxtresources.managers.ArenaMgr;
import net.nxtresources.managers.MsgCache;
import net.nxtresources.managers.SetupMgr;
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
            sender.sendMessage(Main.color(
                    MsgCache.get("No-Permission").replace("%prefix%", prefix)
            ));
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
                    sender.sendMessage(Main.color(MsgCache.get("Usage").replace("%usage%", "/sheepwars create <name> <size(Pairs only)>")));
                    return false;
                }
                String name =args[1];
                int size;
                try {
                    size = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(Main.color(MsgCache.get("Arena.NumberOnly")));
                    return false;
                }
                if(size%2!=0) {
                    sender.sendMessage(Main.color(MsgCache.get("Arena.ParisOnly")));
                    return false;
                }
                if(size<1) {
                    sender.sendMessage(Main.color(MsgCache.get("Arena.CanPositive")));
                    return false;
                }
                for(Arena a : ArenaMgr.arenas) {
                    if(Objects.equals(a.name, name)) {
                        sender.sendMessage(Main.color(MsgCache.get("Arena.ItAlreadyExists")));
                        return false;
                    }
                }
                for (Arena a : SetupMgr.temporaryArenas) {
                    if (a.name.equalsIgnoreCase(name)) {
                        sender.sendMessage(Main.color(MsgCache.get("Arena.Setup.AlreadyInProgress")));
                        return false;
                    }
                }
                switch (SetupMgr.startSetup(player, name, size, true)) {
                    case 0 -> sender.sendMessage(Main.color(MsgCache.get("Arena.Create").replace("%arena_name%", name)));
                    case 1 -> sender.sendMessage(Main.color(MsgCache.get("Arena.NoSuchArena")));
                    case 2 -> sender.sendMessage(Main.color(MsgCache.get("Arena.Setup.AlreadySettingUp")));
                }
                return false;
            }
            case "d","delete" ->{
                if(args.length< 2){
                    sender.sendMessage(Main.color(MsgCache.get("Usage").replace("%usage%", "/sheepwars delete <name>")));
                    return false;
                }
                String name =args[1];
                switch (ArenaMgr.del(name)) {
                    case 0-> {
                        sender.sendMessage(Main.color(MsgCache.get("Arena.Delete").replace("%arena_name%", name)));
                        return true;
                    }
                    case 1-> sender.sendMessage(Main.color(MsgCache.get("Arena.NoSuchArena")));
                    case 2-> sender.sendMessage(Main.color(MsgCache.get("Arena.PlayersInside")));
                }
                return false;
            }
            case "l","list" -> {
                sender.sendMessage(Main.color(MsgCache.get("Arena.Arenas")));
                ArenaMgr.arenas.forEach(a->{
                    sender.sendMessage("§6Arena name: §e"+a.name+"§6, Size: §e"+ a.size+"§6, LobbyPlayers: §e"+a.lobbyPlayers +"§6, Status: §e"+ a.stat+"§6 Prog: "+a.getProg() +"§6 Teams: " );
                    a.teams.forEach(t->{
                        sender.sendMessage("§a"+t.type+": ");
                        t.tPlayers.forEach(p->sender.sendMessage("§9"+p.toString()));
                    });
                });
                return true;
            }

            case "join","j" -> {
                if(args.length <2){
                    sender.sendMessage(Main.color(MsgCache.get("Usage").replace("%usage%","/sheepwars join <name>")));
                    return false;
                }
                String name = args[1];

                switch (ArenaMgr.join(name,player)) {
                    case 0-> {
                        sender.sendMessage(Main.color(MsgCache.get("Arena.Join").replace("%arena_name%", name)));
                        return true;
                    }
                    case 1-> sender.sendMessage(Main.color(MsgCache.get("Arena.NoSuchArena")));
                    case 2-> sender.sendMessage(Main.color(MsgCache.get("Arena.AlreadyInArena")));
                    case 3-> sender.sendMessage(Main.color(MsgCache.get("Arena.ArenaIsFull")));
                    case 4-> sender.sendMessage(Main.color(MsgCache.get("Arena.ArenaStarted")));
                    case 5-> sender.sendMessage("Waitinglobby=null");
                }
                return false;

            }
            case "leave","le" -> {
                if(!ArenaMgr.isInArena(player)) {
                    sender.sendMessage(Main.color(MsgCache.get("Arena.NotInAnArena")));
                    return false;
                }
                ArenaMgr.leave(player);
                sender.sendMessage(Main.color(MsgCache.get("Arena.Leave")));
                return true;

            }

            case "setlobby" -> {
                SetupMgr.setMainLobby(player);
                sender.sendMessage(Main.color(MsgCache.get("SetMainLobby")));
                return true;
            }
            case "lobby" -> {
                SetupMgr.tpToLobby(player);
                sender.sendMessage(Main.color(MsgCache.get("GetMainLobby")));
                return true;
            }

            case "rl","reload" -> {
                if (!sender.hasPermission("sheepwars.*") && !sender.hasPermission("sheepwars.reload")) {
                    sender.sendMessage(Main.color(MsgCache.get("No-Permission")));
                    return false;
                }
                if(args.length < 2) {
                    sender.sendMessage(Main.color(MsgCache.get("Reloading")));
                    long started = System.currentTimeMillis();
                    Main.getInstance().reload();
                    long endTime = System.currentTimeMillis();
                    long completed = endTime - started;
                    sender.sendMessage(Main.color(MsgCache.get("Reloaded").replace("%ms%", String.valueOf(completed))));
                    return true;
                }
            }

            default -> sender.sendMessage(Main.color(MsgCache.get("Invalid-argument")));
        }
        return false;
    }
}
