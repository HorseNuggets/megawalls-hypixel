package net.nuggetmc.mw.command;

import net.nuggetmc.mw.MegaWalls;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class MWBalTopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        sender.sendMessage(MegaWalls.getInstance().getCoinsManager().getBalTop());
        return true;
    }
}
