package net.nuggetmc.mw.mwclass.classes;

import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

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

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        if (!Energy.isValid(event)) {
            return;
        }

        Player player = (Player) event.getDamager();

        Energy.add(player, 25);
    }

    @Override
    public void assign(Player player) {
        Map<Integer, ItemStack> items;

        if (MWKit.contains(this)) {
            items = MWKit.fetch(this);
        }

        else {
            Map<Enchantment, Integer> swordEnch = new HashMap<>();
            swordEnch.put(Enchantment.DURABILITY, 10);

            Map<Enchantment, Integer> armorEnch = new HashMap<>();
            armorEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            armorEnch.put(Enchantment.DURABILITY, 10);
            armorEnch.put(Enchantment.WATER_WORKER, 1);

            ItemStack sword = MWItem.createSword(this, Material.DIAMOND_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack helmet = MWItem.createArmor(this, Material.IRON_HELMET, armorEnch);

            items = MWKit.generate(this, sword, bow, tool, 2, 7, 2, helmet, null, null, null);
        }

        MWKit.assignItems(player, items);
    }
}
