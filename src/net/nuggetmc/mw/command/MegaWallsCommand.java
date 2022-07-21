package net.nuggetmc.mw.command;

import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.MWClassMenu;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MegaWallsCommand implements CommandExecutor, TabCompleter {

    private final MWClassManager manager;
    private final MWClassMenu menu;

    public MegaWallsCommand() {
        MegaWalls plugin = MegaWalls.getInstance();

        this.manager = plugin.getClassManager();
        this.menu = plugin.getMenu();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (MegaWalls.getInstance().getCombatManager().isInCombat(player)&&(!player.isOp())){
                sender.sendMessage(ChatColor.RED+"You cannot do that because you are in combat!");
                return true;
            }
                
            if (args.length > 0) {
                String name = StringUtils.capitalize(args[0].toLowerCase());
                MWClass mwclass = manager.fetch(name);

                if (mwclass != null) {
                    menu.select(player, mwclass);
                    return true;
                }
            }

            menu.openGUI(player);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length != 1) return null;

        List<String> groupnames = new ArrayList<>(manager.getClasses().keySet()).stream().map(String::toLowerCase).collect(Collectors.toList());
        String arg = args[0];

        if (!isEmptyTab(arg)) {
            return autofill(groupnames, arg);
        }

        return groupnames;
    }

    private boolean isEmptyTab(String s) {
        return s == null || s.equals(" ") || s.isEmpty();
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
}
