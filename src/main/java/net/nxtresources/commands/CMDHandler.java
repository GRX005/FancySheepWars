package net.nxtresources.commands;

import net.nxtresources.Main;
import net.nxtresources.managers.*;
import net.nxtresources.utils.MsgCache;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class CMDHandler implements CommandExecutor {

    String prefix = ConfigMgr.messagesConfig.getString("Prefix");

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Main.color(MsgCache.get("IngameOnly")));
            return false;
        }

        if(args.length == 0){
            if(!(player.hasPermission("fancysheepwars.admin"))){
                List<String> playerHelp = MsgCache.getList("Help.Player");
                for(String line : playerHelp) player.sendMessage(Main.color(line));
            } else {
                List<String> adminHelp = MsgCache.getList("Help.Admin");
                for(String line : adminHelp) player.sendMessage(Main.color(line));
            }
            return false;
        }
        switch (args[0].toLowerCase()) {
            case "c","create" ->{
                if(!player.hasPermission("fancysheepwars.create") && !player.hasPermission("fancysheepwars.admin")){
                    sender.sendMessage(Main.color(MsgCache.get("No-Permission").replace("%prefix%", prefix)));
                    return true;
                }

                if(args.length< 3){
                    sender.sendMessage(Main.color(MsgCache.get("Usage").replace("%usage%", "/sheepwars create <ArenaName> <size(Pairs only)>")));
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
                    sender.sendMessage(Main.color(MsgCache.get("Arena.PairsOnly")));
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
                if(SetupMgr.isInSetup(player)){
                    sender.sendMessage(Main.color(MsgCache.get("Arena.Setup.AlreadyInProgress")));
                    return false;

                }
                SetupMgr setup = new SetupMgr();
                switch (setup.start(player, name, size, true)) {
                    case 0 -> sender.sendMessage(Main.color(MsgCache.get("Arena.CreateTemp").replace("%arena_name%", name)));
                    case 1 -> sender.sendMessage(Main.color(MsgCache.get("Arena.Setup.AlreadySettingUp")));
                }
                return false;
            }
            case "d","delete" ->{
                if(!player.hasPermission("fancysheepwars.delete") && !player.hasPermission("fancysheepwars.admin")){
                    sender.sendMessage(Main.color(MsgCache.get("No-Permission").replace("%prefix%", prefix)));
                    return true;
                }
                if(args.length< 2){
                    sender.sendMessage(Main.color(MsgCache.get("Usage").replace("%usage%", "/sheepwars delete <ArenaName>")));
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
                if(!player.hasPermission("fancysheepwars.list") && !player.hasPermission("fancysheepwars.admin")){
                    sender.sendMessage(Main.color(MsgCache.get("No-Permission").replace("%prefix%", prefix)));
                    return true;
                }
                if(ArenaMgr.arenas.isEmpty()){
                    player.sendMessage(Main.color(MsgCache.get("Arena.NoCreatedArena")));
                    return true;
                }
                sender.sendMessage(Main.color(MsgCache.get("Arena.Arenas")));
                ArenaMgr.arenas.forEach(a->{
                    sender.sendMessage("§6Arena name: §e"+a.name+"§6, Size: §e"+ a.size+"§6, LobbyPlayers: §e"+a.lobbyPlayers +"§6, Status: §e"+ a.stat+"§6 Prog: "+a.prog +"§6 Teams: " );
                    a.teams.forEach(t->{
                        sender.sendMessage("§a"+t.type+": ");
                        t.tPlayers.forEach(p->sender.sendMessage("§9"+p.toString()));
                    });
                });
                return true;
            }

            case "join","j" -> {
                if(args.length <2){
                    sender.sendMessage(Main.color(MsgCache.get("Usage").replace("%usage%","/sheepwars join <ArenaName>")));
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
                LobbyMgr.setMainLobby(player);
                sender.sendMessage(Main.color(MsgCache.get("SetMainLobby")));
                return true;
            }
            case "lobby" -> {
                if(LobbyMgr.getLobbyLocation() ==null){
                    player.sendMessage(Main.color(MsgCache.get("MainLobbyNotSet")));
                    return true;
                }
                LobbyMgr.tpMainLobby(player);
                sender.sendMessage(Main.color(MsgCache.get("GetMainLobby")));
                return true;
            }
            case "rl","reload" -> {
                if(!player.hasPermission("fancysheepwars.reload") && !player.hasPermission("fancysheepwars.admin")){
                    sender.sendMessage(Main.color(MsgCache.get("No-Permission").replace("%prefix%", prefix)));
                    return true;
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
