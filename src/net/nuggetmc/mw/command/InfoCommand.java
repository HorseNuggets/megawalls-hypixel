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

public class InfoCommand implements CommandExecutor {

    private final MegaWalls plugin;
    private final MWClassManager manager;

    public InfoCommand() {
        this.plugin = MegaWalls.getInstance();
        this.manager = plugin.getClassManager();
        MegaWalls plugin = MegaWalls.getInstance();

    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            execute(sender);
        });

        return true;
    }

    private void execute(CommandSender sender) {
        Map<String, Set<Player>> values = new HashMap<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            MWClass mwclass = manager.get(player);
            if (mwclass == null) continue;

            String name = mwclass.getColor() + mwclass.getName();

            if (values.containsKey(name)) {
                values.get(name).add(player);
            } else {
                values.put(name, Sets.newHashSet(player));
            }
        }

        if (values.isEmpty()) {
            sender.sendMessage("No MegaWalls classes are currently selected by anyone!");
            return;
        }

        List<String> lines = new ArrayList<>();

        values.forEach((key, value) -> {
            lines.add(key);

            for (Player player : value) {
                lines.add(ChatUtils.BULLET_FORMATTED + ChatColor.RESET + player.getName());
            }

            lines.add("");
        });

        int n = lines.size();

        if (lines.get(n - 1).isEmpty()) {
            lines.remove(n - 1);
        }

        sender.sendMessage(ChatUtils.LINE);
        lines.forEach(sender::sendMessage);
        sender.sendMessage(ChatUtils.LINE);
    }
}
