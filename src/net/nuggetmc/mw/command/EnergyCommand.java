package net.nuggetmc.mw.command;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnergyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 && sender instanceof Player) {
            maxEnergy((Player) sender);
            return true;
        }

        Player player = Bukkit.getPlayer(args[0]);

        if (player != null) {
            maxEnergy(player);
        }

        return true;
    }

    private void maxEnergy(Player player) {
        if (MWClassManager.isMW(player)) {
            Energy.add(player, 100);

            player.sendMessage("Your " + ChatColor.GREEN + "ENERGY" + ChatColor.RESET + " has been set to " + ChatColor.GREEN + "100" + ChatColor.RESET + "!");
        }
    }
}
