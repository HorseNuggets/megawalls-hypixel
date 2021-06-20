package net.nuggetmc.mw.mwclass.classes;

import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MWHerobrine implements MWClass {

    private final String NAME;
    private final Material ICON;
    private final Playstyle[] PLAYSTYLES;
    private final Diamond[] DIAMONDS;
    private final MWClassInfo CLASS_INFO;

    public MWHerobrine() {
        NAME = "Herobrine";
        ICON = Material.DIAMOND_SWORD;

        PLAYSTYLES = new Playstyle[]
        {
            Playstyle.DAMAGE,
            Playstyle.CONTROL
        };

        DIAMONDS = new Diamond[]
        {
            Diamond.SWORD
        };

        CLASS_INFO = new MWClassInfo
        (
            "Wrath",
            "Unleash the wrath of Herobrine, striking all nearby enemies in a 5 block radius for &a4.5 &rdamage.",
            "Power",
            "Killing an emeny grants you Strength I for 6 seconds.",
            "Flurry",
            "Every &a3 &rattacks will grant you Speed II for 3 seconds and Regeneration I for 5 seconds.",
            "Treasure Hunter",
            "Increases the chance to find treasure chests by &a300% &rwhen mining."
        );

        CLASS_INFO.addEnergyGainType("Melee", 25);
        CLASS_INFO.addEnergyGainType("Bow", 25);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public Material getIcon() {
        return ICON;
    }

    @Override
    public Playstyle[] getPlaystyles() {
        return PLAYSTYLES;
    }

    @Override
    public Diamond[] getDiamonds() {
        return DIAMONDS;
    }

    @Override
    public MWClassInfo getInfo() {
        return CLASS_INFO;
    }

    @Override
    public void assign(Player player) {
        player.sendMessage("ASSIGN");
    }
}
