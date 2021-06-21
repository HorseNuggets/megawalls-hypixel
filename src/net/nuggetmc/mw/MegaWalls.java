package net.nuggetmc.mw;

import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassMenu;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.classes.MWHerobrine;
import net.nuggetmc.mw.utils.MWHealth;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;

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

        getCommand("megawalls").setExecutor(menu);

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
        MWClassManager.register(new MWHerobrine());
    }
}
