package net.nuggetmc.mw.command;

import net.nuggetmc.mw.MegaWalls;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player){
            Player player= (Player) sender;
            MegaWalls.getInstance().getShopMenu().openGUI(player);
        }
        return true;
    }
}
