package net.nuggetmc.mw;

import net.nuggetmc.mw.admin.EnergyCommand;
import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.MWClassMenu;
import net.nuggetmc.mw.mwclass.classes.*;
import net.nuggetmc.mw.utils.MWHealth;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MegaWalls extends JavaPlugin {

    private static MegaWalls instance;

    private MWClassMenu menu;

    public static MegaWalls getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        menu = new MWClassMenu("Class Selector");

        getCommand("energy").setExecutor(new EnergyCommand());

        PluginCommand command = getCommand("megawalls");
        command.setExecutor(menu);
        command.setTabCompleter(this);

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(new Energy(), this);
        manager.registerEvents(new MWClassManager(this), this);
        manager.registerEvents(new MWHealth(this), this);
        manager.registerEvents(menu, this);

        registerClasses();

        for (Map.Entry<String, MWClass> entry : MWClassManager.getClasses().entrySet()) {
            manager.registerEvents(entry.getValue(), this);
        }

        Energy.flash();
    }

    private void registerClasses() {
        MWClassManager.register(new MWCreeper());
        MWClassManager.register(new MWEnderman());
        MWClassManager.register(new MWGolem());
        MWClassManager.register(new MWHerobrine());
        MWClassManager.register(new MWSkeleton());
        MWClassManager.register(new MWSpider());
        MWClassManager.register(new MWZombie());
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        if (!cmd.getName().equals("megawalls")) return null;
        if (args.length != 1) return null;

        List<String> groupnames = new ArrayList<>(MWClassManager.getClasses().keySet()).stream().map(String::toLowerCase).collect(Collectors.toList());
        String arg = args[0];

        if (tabConditional(arg)) {
            return autofill(groupnames, arg);
        }

        return groupnames;
    }

    private boolean tabConditional(String argument) {
        if (argument == null || argument == "" || argument == " " || argument.isEmpty()) {
            return false;
        }
        return true;
    }

    private List<String> autofill(List<String> groupnames, String input) {
        List<String> newlist = new ArrayList<>();
        for (String entry : groupnames) {
            if (entry.length() >= input.length()) {
                if (input.equalsIgnoreCase(entry.substring(0, input.length()))) {
                    newlist.add(entry);
                }
            }
        }

        if (newlist.isEmpty()) {
            return groupnames;
        }

        return newlist;
    }
}
