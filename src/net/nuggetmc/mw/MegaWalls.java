package net.nuggetmc.mw;

import net.nuggetmc.mw.mwclass.MWClassMenu;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.classes.MWHerobrine;
import org.bukkit.plugin.java.JavaPlugin;

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
        getServer().getPluginManager().registerEvents(menu, this);

        registerClasses();
    }

    private void registerClasses() {
        MWClassManager.register(new MWHerobrine());
    }
}
