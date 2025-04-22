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


        return false;
    }
}
