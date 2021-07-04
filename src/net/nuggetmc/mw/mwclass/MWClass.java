package net.nuggetmc.mw.mwclass;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class MWClass implements Listener {

    protected final MegaWalls plugin;

    protected String name;
    protected Material icon;
    protected ChatColor color;
    protected Playstyle[] playstyles;
    protected Diamond[] diamonds;
    protected MWClassInfo classInfo;

    public MWClass() {
        this.plugin = MegaWalls.getInstance();
    }

    public String getName() {
        return name;
    }
    public Material getIcon() {
        return icon;
    }
    public ChatColor getColor() {
        return color;
    }
    public Playstyle[] getPlaystyles() {
        return playstyles;
    }
    public Diamond[] getDiamonds() {
        return diamonds;
    }
    public MWClassInfo getInfo() {
        return classInfo;
    }

    abstract public void ability(Player player);
    abstract public void assign(Player player);
}
