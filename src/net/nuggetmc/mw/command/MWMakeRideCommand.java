package net.nuggetmc.mw.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MWMakeRideCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player=(Player) sender;
        if (args.length!=1){
            sender.sendMessage("invaild syntax!usage:/mwride <player>");
        }else {
            Player player1;
            try {
                player1=Bukkit.getPlayer(args[0]);
            }catch (Exception e){
                sender.sendMessage("wrong playername!");
                return true;
            }
            if (player1==null){
                sender.sendMessage("wrong playername!");
                return true;
            }
            if (player1.getUniqueId()==player.getUniqueId()){
                sender.sendMessage("you cannot ride on yourself!");
                return true;
            }
            player.setPassenger(player1);
        }
        return true;
    }
}
