package net.nuggetmc.mw.command;

import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMWSpawnCommand implements CommandExecutor {
    private final MWClassManager manager;

    public SetMWSpawnCommand() {
        this.manager = MegaWalls.getInstance().getManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            MegaWalls.getInstance().spawnx=((Player) sender).getLocation().getBlockX();
            MegaWalls.getInstance().spawny=((Player) sender).getLocation().getBlockY();
            MegaWalls.getInstance().spawnz=((Player) sender).getLocation().getBlockZ();

        }

        /*if (manager.getKitLock()) {
            manager.setKitLock(false);
            Bukkit.broadcastMessage("Kit items are now " + ChatColor.BLUE + "STACKABLE" + ChatColor.RESET + ".");
        } else {
            manager.setKitLock(true);
            Bukkit.broadcastMessage("Kit items are no longer " + ChatColor.BLUE + "STACKABLE" + ChatColor.RESET + ".");
        }*/
        return true;
    }
}
