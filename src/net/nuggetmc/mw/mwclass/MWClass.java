package net.nuggetmc.mw.mwclass;

import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public interface MWClass {
    String getName();
    Material getIcon();
    MWClassInfo getInfo();
    Playstyle[] getPlaystyles();
    Diamond[] getDiamonds();

    void assign(Player player);
}
