package net.nuggetmc.mw;

import com.google.common.eventbus.Subscribe;
import net.minecraft.server.v1_8_R3.BlockAir;
import net.minecraft.server.v1_8_R3.Items;
import net.nuggetmc.mw.command.*;
import net.nuggetmc.mw.energy.EnergyManager;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.MWClassMenu;
import net.nuggetmc.mw.mwclass.classes.*;
import net.nuggetmc.mw.utils.ItemUtils;
import net.nuggetmc.mw.utils.MWHealth;
import net.nuggetmc.mw.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
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
    private boolean isChinese;

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
        setExecutor("mwinfo", new InfoCommand());
        setExecutorAndTabCompleter("megawalls", new MegaWallsCommand());

        this.registerClasses(
            new MWCreeper(),
            new MWDreadlord(),
            new MWEnderman(),
            new MWGolem(),
            new MWHerobrine(),
            new MWSkeleton(),
            new MWSpider(),
            new MWSquid(),
            new MWZombie()
        );

        this.registerEvents(
            this.mwClassManager,
            this.mwClassMenu,
            this.mwhealth,
            this.energyManager,
            new WorldUtils()
        );

        this.restore();
        this.initEnergy();

        ItemUtils.tickMWItems();
        //Create config
        try {
            isChinese=(getConfig().get("use_chinese").equals(true));
        }catch (Exception e){
            getConfig().set("use_chinese",false);
            saveConfig();
        }

    }

    private void initEnergy() {
        energyManager.flash();

        Bukkit.getOnlinePlayers().forEach(p -> {
            if (energyManager.get(p) == 0) {
                energyManager.clear(p);
            }
        });
    }

    private void restore() {
        ConfigurationSection section = getConfig().getConfigurationSection("active_classes");
        if (section == null) return;

        ConfigurationSection sectionEnergy = getConfig().getConfigurationSection("energy");
        boolean checkEnergy = sectionEnergy != null;

        for (String key : section.getKeys(false)) {
            String name = section.getString(key);

            section.set(key, null);

            Player player = Bukkit.getPlayer(key);
            if (player == null || !player.isOnline()) continue;

            MWClass mwclass = mwClassManager.fetch(name);
            if (mwclass == null) continue;

            mwClassManager.assign(player, mwclass, false);

            if (checkEnergy) {
                if (sectionEnergy.contains(key)) {
                    int energy = sectionEnergy.getInt(key);

                    energyManager.set(player, energy);

                    sectionEnergy.set(key, null);
                }
            }
        }

        saveConfig();
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

    public boolean isChinese() {
        return isChinese;
    }

    public void setChinese(boolean chinese) {
        isChinese = chinese;
    }
  //  @EventHandler
    //public void onClearPot(PlayerDropItemEvent e){

                //        e.getPlayer().getInventory().remove(Material.GLASS_BOTTLE);
         //               return;
    //Why not working?
    }



