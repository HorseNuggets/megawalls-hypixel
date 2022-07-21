package net.nuggetmc.mw.command;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.energy.EnergyManager;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class EnergyCommand implements CommandExecutor {

    private final MWClassManager manager;
    private final EnergyManager energyManager;

    public EnergyCommand() {
        MegaWalls plugin = MegaWalls.getInstance();

        this.manager = plugin.getClassManager();
        this.energyManager = plugin.getEnergyManager();
    }

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
        if (manager.isMW(player)) {
            energyManager.add(player, 100);

            player.sendMessage("Your " + ChatColor.GREEN + "ENERGY" + ChatColor.RESET + " has been set to " + ChatColor.GREEN + "100" + ChatColor.RESET + "!");
        }
    }
}
