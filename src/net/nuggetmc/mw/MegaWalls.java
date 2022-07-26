package net.nuggetmc.mw;

import jdk.nashorn.internal.objects.annotations.Getter;
import net.nuggetmc.mw.economics.CoinsManager;
import net.nuggetmc.mw.combat.CombatManager;
import net.nuggetmc.mw.command.*;
import net.nuggetmc.mw.economics.SellMenu;
import net.nuggetmc.mw.economics.ShopMenu;
import net.nuggetmc.mw.energy.EnergyManager;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.MWClassMenu;
import net.nuggetmc.mw.mwclass.classes.*;
import net.nuggetmc.mw.utils.ItemUtils;
import net.nuggetmc.mw.utils.MWHealth;
import net.nuggetmc.mw.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.logging.Level;

public class MegaWalls extends JavaPlugin {

    private static MegaWalls INSTANCE;

    private PluginManager pluginManager;
    private MWClassManager mwClassManager;
    private MWClassMenu mwClassMenu;
    private ShopMenu shopMenu;

    @Getter
    public SellMenu getSellMenu() {
        return sellMenu;
    }

    private SellMenu sellMenu;
    private MWHealth mwhealth;
    private EnergyManager energyManager;
    private CoinsManager coinsManager;
    private CombatManager combatManager=new CombatManager();

    public static MegaWalls getInstance() {
        return INSTANCE;
    }

    public MWClassManager getClassManager() {
        return mwClassManager;
    }

    public MWClassMenu getMenu() {
        return mwClassMenu;
    }
    public ShopMenu getShopMenu(){
        return shopMenu;
    }

    public MWHealth getMWHealth() {
        return mwhealth;
    }

    public EnergyManager getEnergyManager() {
        return energyManager;
    }
    @Getter
    public CoinsManager getCoinsManager(){
        return this.coinsManager;
    }
    @Getter
    public CombatManager getCombatManager(){
        return combatManager;
    }
    private boolean isChinese=(getConfig().get("use_chinese").equals(true));
    public int spawnx;
    public int spawny;
    public int spawnz;
    public static boolean OPBYPASSGM=false;
    @Override
    public void onEnable() {
        INSTANCE = this;
        //cfg
        try {
            isChinese=(getConfig().get("use_chinese").equals(true));
        }catch (Exception e){
            getConfig().set("use_chinese",false);
            saveConfig();
        }
        try {
            spawnx= (int) getConfig().get("spawnloc.x");
            spawny= (int) getConfig().get("spawnloc.y");
            spawnz= (int) getConfig().get("spawnloc.z");
        }catch (Exception e){
            getServer().getLogger().log(Level.WARNING,"Failed to get spawn location.Generating a new one.");
            getConfig().set("spawnloc.x",0);
            getConfig().set("spawnloc.y",0);
            getConfig().set("spawnloc.z",0);
            saveConfig();
        }
        try {
            OPBYPASSGM= (boolean) getConfig().get("opbypassgamemode");
        }catch (Exception e){
            getConfig().set("opbypassgamemode",false);
            saveConfig();
        }
        // Create instances
        this.pluginManager = this.getServer().getPluginManager();
        this.mwClassManager = new MWClassManager(this);
        this.energyManager = new EnergyManager();
        this.coinsManager=new CoinsManager();
        this.mwClassMenu = new MWClassMenu(this, "Class Selector");
        this.mwhealth = new MWHealth();
        this.shopMenu=new ShopMenu();
        this.sellMenu=new SellMenu();

        // Register commands
        setExecutor("energy", new EnergyCommand());
        setExecutor("debug", new DebugCommand());
        setExecutor("mwinfo", new InfoCommand());
        setExecutor("mwspawn", new SetMWSpawnCommand());
        setExecutor("echest", new EchestCommand());
        setExecutor("mwcoins", new MWCoinsCommand());
        setExecutor("seeinv", new SeeinvCommand());
        setExecutor("mwshop", new ShopCommand());
        setExecutor("mwsell", new SellCommand());
        setExecutor("coinsmgr", new CoinsmgrCommand());
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
            this.coinsManager,
            this.shopMenu,
            this.sellMenu,
            new WorldUtils()
        );

        this.restore();
        this.initEnergy();

        ItemUtils.tickMWItems();


    }
    public int getOrDefaultFromConfig(String path,int defaulta){
        int result;
        try {
           result= (int) getConfig().get(path);
        }catch (Exception e){
            getConfig().set(path,defaulta);
            result=defaulta;
        }
        saveConfig();
        return result;
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



