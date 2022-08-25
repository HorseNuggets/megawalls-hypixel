package net.nuggetmc.mw.command;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetMWSpawnCommand implements CommandExecutor {
    private final MWClassManager manager;

    public SetMWSpawnCommand() {
        this.manager = MegaWalls.getInstance().getClassManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            int blockX = ((Player) sender).getLocation().getBlockX();
            int blockY = ((Player) sender).getLocation().getBlockY();
            int blockZ = ((Player) sender).getLocation().getBlockZ();
            MegaWalls.getInstance().spawnx= blockX;
            MegaWalls.getInstance().spawny= blockY;
            MegaWalls.getInstance().spawnz= blockZ;
            MegaWalls.getInstance().getConfig().set("spawnloc.x", blockX);
            MegaWalls.getInstance().getConfig().set("spawnloc.y", blockY);
            MegaWalls.getInstance().getConfig().set("spawnloc.z", blockZ);
            MegaWalls.getInstance().saveConfig();
           sender.sendMessage("The " + ChatColor.GREEN + "Spawn Location" + ChatColor.RESET + " has been set to " + ChatColor.GREEN + blockX+","+blockY+","+blockZ + ChatColor.RESET + "!");
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
