package net.nxtresources.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SheepWarsCommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String @NotNull [] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Ingame only!");
            return false;
        }
        if(!(sender.hasPermission("fancysheepwars.use"))) {
            sender.sendMessage("§cNincs jogosultságod a parancs használatához!");
            return false;
        }
        if(args.length == 0){
            sender.sendMessage("fancy sheepwars help: ");
            sender.sendMessage("/sheepwars arena create <name> ");
            sender.sendMessage("/sheepwars arena delete <name> ");
            sender.sendMessage("/sheepwars arena list");
            sender.sendMessage("/sheepwars join <name> ");
            sender.sendMessage("/sheepwars leave ");
            return false;
        }
        switch (cmd.getName().toLowerCase()){
            case "arena" -> {
                switch (args[1].toLowerCase()) {
                    case "create" ->{
                        if(args.length< 3){
                            sender.sendMessage("Használat: /sheepwars arena create <név>");
                            return false;
                        }
                        String name =args[2];
                        sender.sendMessage("aréna létrehozva a következő néven: " + name);
                        return true;
                    }
                    case "delete" ->{
                        if(args.length< 3){
                            sender.sendMessage("Használat: /sheepwars arena delete <név>");
                            return false;
                        }
                        String name =args[2];
                        sender.sendMessage("aréna törölve a következő néven: " + name);
                        return true;

                    }
                    case "list" -> sender.sendMessage("Elérhető arénák: ");
                    default -> sender.sendMessage("Ismeretlen argument!");
                }

            }
            case "join" -> {
                if(args.length <2){
                    sender.sendMessage("Használat: /sheepwars join <név>");
                    return false;
                }
                String name = args[1];
                sender.sendMessage("Csatlakoztál a következő arénához: " + name + "!");
                return true;

            }
            case "leave" -> {
                sender.sendMessage("Kiléptél az arénából!");
                return true;

            }

        }


        return false;
    }
}
