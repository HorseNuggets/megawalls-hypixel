package net.nuggetmc.mw;

import net.nuggetmc.mw.admin.DebugCommand;
import net.nuggetmc.mw.admin.EnergyCommand;
import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.MWClassMenu;
import net.nuggetmc.mw.mwclass.classes.*;
import net.nuggetmc.mw.utils.MWHealth;
import net.nuggetmc.mw.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MegaWalls extends JavaPlugin {

    private static MegaWalls instance;

    private MWClassManager mwClassManager;
    private MWClassMenu menu;

    public static MegaWalls getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        this.mwClassManager = new MWClassManager(this);
        this.menu = new MWClassMenu("Class Selector", mwClassManager);

        getCommand("energy").setExecutor(new EnergyCommand());
        getCommand("debug").setExecutor(new DebugCommand());

        PluginCommand command = getCommand("megawalls");
        command.setExecutor(menu);
        command.setTabCompleter(this);

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(mwClassManager, this);
        manager.registerEvents(menu, this);
        manager.registerEvents(new Energy(), this);
        manager.registerEvents(new MWHealth(this), this);
        manager.registerEvents(new WorldUtils(), this);

        registerClasses();

        for (Player player : Bukkit.getOnlinePlayers()) {
            Energy.set(player, 0);
        }

        for (Map.Entry<String, MWClass> entry : MWClassManager.getClasses().entrySet()) {
            manager.registerEvents(entry.getValue(), this);
        }

        for (String key : getConfig().getKeys(false)) {
            String name = getConfig().getString(key);

            getConfig().set(key, null);

            Player player = Bukkit.getPlayer(key);
            if (player == null || !player.isOnline()) continue;

            MWClass mwclass = MWClassManager.fetch(name);
            if (mwclass == null) continue;

            MWClassManager.getActive().put(player, mwclass);
        }

        saveConfig();

        Energy.flash();
    }

    private void registerClasses() {
        MWClassManager.register(
            new MWCreeper(),
            new MWDreadlord(),
            new MWEnderman(),
            new MWGolem(),
            new MWHerobrine(),
            new MWSkeleton(),
            new MWSpider(),
            new MWZombie()
        );
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equals("megawalls")) return null;
        if (args.length != 1) return null;

        List<String> groupnames = new ArrayList<>(MWClassManager.getClasses().keySet()).stream().map(String::toLowerCase).collect(Collectors.toList());
        String arg = args[0];

        if (!isEmptyTab(arg)) {
            return autofill(groupnames, arg);
        }

        return groupnames;
    }

    private boolean isEmptyTab(String s) {
        return s == null || s.equals("") || s.equals(" ") || s.isEmpty();
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
