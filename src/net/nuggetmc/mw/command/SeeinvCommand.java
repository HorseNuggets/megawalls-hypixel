package net.nuggetmc.mw.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SeeinvCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player){
            Player player=(Player) sender;
            if (args.length==1){
                try {
                    player.openInventory(Bukkit.getPlayer(args[0]).getInventory());
                }catch (Exception e){
                    sender.sendMessage("Not Found!");
                }
            }
        }
        return true;
    }
}
