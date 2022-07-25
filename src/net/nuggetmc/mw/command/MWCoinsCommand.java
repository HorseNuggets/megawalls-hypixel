package net.nuggetmc.mw.command;

import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.MWClassMenu;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MWCoinsCommand implements CommandExecutor{



    public MWCoinsCommand() {

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length==0){

                player.sendMessage(MegaWalls.getInstance().getCoinsManager().get(player)+" coins");
            } else if (args.length==1) {
                if(!(player.isOp()||player.hasPermission("mw.otherscoin"))){
                    player.sendMessage("You don't have permission to use this command!");
                }else {
                    try {
                        player.sendMessage(MegaWalls.getInstance().getCoinsManager().get(Bukkit.getPlayer(args[0])) + " coins");
                    } catch (Exception e) {
                        player.sendMessage("Player Not found!");
                    }
                }
            }


        }




        return true;
    }




}
