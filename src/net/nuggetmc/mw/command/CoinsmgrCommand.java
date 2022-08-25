package net.nuggetmc.mw.command;

import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.economics.CoinsManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoinsmgrCommand implements CommandExecutor , TabCompleter {
    CoinsManager coinsManager= MegaWalls.getInstance().getCoinsManager();
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender.isOp()||sender.hasPermission("mw.admin"))){
            return true;
        }
        if (args.length==0||(args.length==1&& args[0].equalsIgnoreCase("help"))){
            sendTheHelp(sender);
            return true;
        }
        if (args.length==3&&args[0].equalsIgnoreCase("add")){
            try{
                coinsManager.add(Bukkit.getPlayer(args[1]), Integer.parseInt(args[2]));
            }catch (Exception e){
                sendTheHelp(sender);
                return true;
            }
            sender.sendMessage("added "+args[2]+" coins to "+args[1]);
            return true;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("clear")) {
            try {
                coinsManager.clear(Bukkit.getPlayer(args[1]));
            }catch (Exception e){
                sendTheHelp(sender);
                return true;
            }
            sender.sendMessage("You have cleared "+args[1]+"'s coins.");
            Bukkit.getPlayer(args[1]).sendMessage("Your coins have been cleared by the admin!");
            return true;
        }else if (args.length==3&&args[0].equalsIgnoreCase("reduce")){
            try{
                coinsManager.add(Bukkit.getPlayer(args[1]), -Integer.parseInt(args[2]));
            }catch (Exception e){
                sendTheHelp(sender);
                return true;
            }
            sender.sendMessage("reduced "+args[2]+" coins of "+args[1]);
            return true;
        }
        sendTheHelp(sender);
        return true;

    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) return null;

        List<String> groupnames = new ArrayList<>();
        groupnames.add("help");
        groupnames.add("add");
        groupnames.add("clear");
        groupnames.add("reduce");
        String arg = args[0];

        if (!isEmptyTab(arg)) {
            return autofill(groupnames, arg);
        }

        return groupnames;
    }

    private void sendTheHelp(CommandSender sender) {
        sendHelpMSG(sender,"----------CoinsManager Help----------");
        sendHelpMSG(sender,"/coinsmgr help ---Display this.");
        sendHelpMSG(sender,"/coinsmgr add <player> <amount> ---Add coins to a player.");
        sendHelpMSG(sender,"/coinsmgr clear <player> ---Add coins to a player.");
        sendHelpMSG(sender,"/coinsmgr reduce <player> ---Reduce a player's coin.");
    }


    private void sendHelpMSG(CommandSender sender, String message){
        sender.sendMessage(ChatColor.AQUA+message);
    }
    private List<String> autofill(List<String> groupnames, String input) {
        List<String> list = new ArrayList<>();

        for (String entry : groupnames) {
            if (entry.length() >= input.length()) {
                if (input.equalsIgnoreCase(entry.substring(0, input.length()))) {
                    list.add(entry);
                }
            }
        }

        if (list.isEmpty()) {
            return groupnames;
        }

        return list;
    }
    private boolean isEmptyTab(String s) {
        return s == null || s.equals(" ") || s.isEmpty();
    }
}
