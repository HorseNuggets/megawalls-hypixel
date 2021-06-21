package net.nuggetmc.mw.mwclass.classes;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.ParticleUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MWGolem implements MWClass {

    private final String NAME;
    private final Material ICON;
    private final ChatColor COLOR;
    private final Playstyle[] PLAYSTYLES;
    private final Diamond[] DIAMONDS;
    private final MWClassInfo CLASS_INFO;

    public MWGolem() {
        NAME = "Golem";
        ICON = Material.IRON_CHESTPLATE;
        COLOR = ChatColor.WHITE;

        PLAYSTYLES = new Playstyle[]
        {
            Playstyle.TANK,
            Playstyle.CONTROL
        };

        DIAMONDS = new Diamond[]
        {
            Diamond.CHESTPLATE,
            Diamond.BOOTS
        };

        CLASS_INFO = new MWClassInfo
        (
            "Iron Punch",
            "Punch the ground, forming a hexagon shockwave which deals &a6 &rdamage to opponents in a 4.5 block radius and pulls them inwards.",
            "Iron Heart",
            "Upon killing a player, you gain Absorption II for &a10 &rseconds.",
            "Iron Constitution",
            "You have a passive resistance against incoming true damage, allowing you to take &a20% &rless true damage.\nYou gain Resistance I as well for &a9 &rseconds when hit by an arrow.",
            "Momentum",
            "An Iron Block will be dropped after every &a4 &rwooden logs chopped."
        );

        CLASS_INFO.addEnergyGainType("Melee", 10);
        CLASS_INFO.addEnergyGainType("Bow", 10);
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
    public ChatColor getColor() {
        return COLOR;
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
    public void ability(Player player) {
        Energy.clear(player);
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = Energy.validate(event);
        if (player == null) return;

        if (MWClassManager.get(player) != this) return;

        Energy.add(player, 10);
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

            Map<Enchantment, Integer> chestplateEnch = new HashMap<>();
            chestplateEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            chestplateEnch.put(Enchantment.DURABILITY, 10);

            Map<Enchantment, Integer> bootsEnch = new HashMap<>();
            bootsEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            bootsEnch.put(Enchantment.DURABILITY, 10);

            ItemStack sword = MWItem.createSword(this, Material.IRON_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack toolAxe = MWItem.createTool(this, Material.IRON_AXE);
            ItemStack chestplate = MWItem.createArmor(this, Material.DIAMOND_CHESTPLATE, chestplateEnch);
            ItemStack boots = MWItem.createArmor(this, Material.DIAMOND_BOOTS, bootsEnch);

            List<ItemStack> potions = new ArrayList<>();

            potions.add(MWPotions.createRegenerationPotions(NAME, COLOR, 3, 12, 10));
            potions.add(MWPotions.createSlowSplash(NAME, COLOR));

            items = MWKit.generate(this, sword, bow, tool, toolAxe, null, potions, null, chestplate, null, boots);
        }

        MWKit.assignItems(player, items);
    }
}
