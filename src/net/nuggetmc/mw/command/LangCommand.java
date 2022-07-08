package net.nuggetmc.mw.command;

import com.google.common.collect.Sets;
import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.utils.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

import static sun.security.krb5.SCDynamicStoreConfig.getConfig;

public class LangCommand implements CommandExecutor {

    private final MegaWalls plugin;
    private final MWClassManager manager;

    public LangCommand() {
        this.plugin = MegaWalls.getInstance();
        this.manager = plugin.getManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 && sender instanceof Player) {
            if (!plugin.isChinese()){
                plugin.setChinese(true);
                sender.sendMessage("Language Switch to Chinese");
                plugin.getConfig().set("use_chinese",true);
            } else if (plugin.isChinese()) {
                plugin.setChinese(false);
                sender.sendMessage("Language Switch to English");
                plugin.getConfig().set("use_chinese",false);
            }
            plugin.saveConfig();
            return true;
        }




        return true;
    }


}
