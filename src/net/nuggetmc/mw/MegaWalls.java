package net.nuggetmc.mw;

import net.nuggetmc.mw.command.DebugCommand;
import net.nuggetmc.mw.command.EnergyCommand;
import net.nuggetmc.mw.command.MegaWallsCommand;
import net.nuggetmc.mw.energy.EnergyManager;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.MWClassMenu;
import net.nuggetmc.mw.mwclass.classes.*;
import net.nuggetmc.mw.utils.MWHealth;
import net.nuggetmc.mw.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class MegaWalls extends JavaPlugin {

    private static MegaWalls INSTANCE;

    private PluginManager pluginManager;
    private MWClassManager mwClassManager;
    private MWClassMenu mwClassMenu;
    private MWHealth mwhealth;
    private EnergyManager energyManager;

    public static MegaWalls getInstance() {
        return INSTANCE;
    }

    public MWClassManager getManager() {
        return mwClassManager;
    }

    public MWClassMenu getMenu() {
        return mwClassMenu;
    }

    public MWHealth getMWHealth() {
        return mwhealth;
    }

    public EnergyManager getEnergyManager() {
        return energyManager;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;

        // Create instances
        this.pluginManager = this.getServer().getPluginManager();
        this.mwClassManager = new MWClassManager(this);
        this.energyManager = new EnergyManager();
        this.mwClassMenu = new MWClassMenu(this, "Class Selector");
        this.mwhealth = new MWHealth();

        // Register commands
        setExecutor("energy", new EnergyCommand());
        setExecutor("debug", new DebugCommand());
        setExecutorAndTabCompleter("megawalls", new MegaWallsCommand());

        this.registerClasses(
            new MWCreeper(),
            new MWDreadlord(),
            new MWEnderman(),
            new MWGolem(),
            new MWHerobrine(),
            new MWSkeleton(),
            new MWSpider(),
            new MWZombie())
        ;

        this.registerEvents(
            this.mwClassManager,
            this.mwClassMenu,
            this.mwhealth,
            this.energyManager,
            new WorldUtils()
        );

        this.initEnergy();
        this.restoreClasses();
    }

    private void initEnergy() {
        Bukkit.getOnlinePlayers().forEach(energyManager::clear);
        energyManager.flash();
    }

    private void restoreClasses() {
        for (String key : getConfig().getKeys(false)) {
            String name = getConfig().getString(key);

            getConfig().set(key, null);

            Player player = Bukkit.getPlayer(key);
            if (player == null || !player.isOnline()) continue;

            MWClass mwclass = mwClassManager.fetch(name);
            if (mwclass == null) continue;

            mwClassManager.getActive().put(player, mwclass);
        }

        this.saveConfig();
    }

    private void setExecutor(String name, CommandExecutor executor) {
        getCommand(name).setExecutor(executor);
    }

    private void setExecutorAndTabCompleter(String name, Object obj) {
        PluginCommand command = getCommand(name);

        command.setExecutor((CommandExecutor) obj);
        command.setTabCompleter((TabCompleter) obj);
    }

    private void registerEvents(Listener... listeners) {
        Arrays.stream(listeners).forEach(c -> pluginManager.registerEvents(c, this));
    }

    private void registerClasses(MWClass... mwclasses) {
        mwClassManager.register(mwclasses);
        Arrays.stream(mwclasses).forEach(this::registerEvents);
    }
}
