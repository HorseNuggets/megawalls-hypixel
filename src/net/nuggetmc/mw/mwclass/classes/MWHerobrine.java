package net.nuggetmc.mw.mwclass.classes;

import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.ActionBar;
import net.nuggetmc.mw.utils.MWHealth;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import net.md_5.bungee.api.ChatColor;

import java.util.*;

public class MWHerobrine implements MWClass {

    private final String NAME;
    private final Material ICON;
    private final ChatColor COLOR;
    private final Playstyle[] PLAYSTYLES;
    private final Diamond[] DIAMONDS;
    private final MWClassInfo CLASS_INFO;

    public MWHerobrine() {
        NAME = "Herobrine";
        ICON = Material.DIAMOND_SWORD;
        COLOR = ChatColor.YELLOW;

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
            "Unleash the wrath of Herobrine, striking all nearby enemies in a 5 block radius for &a4.5 &rtrue damage.",
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
        World world = player.getWorld();

        boolean pass = false;

        Set<Player> cache = new HashSet<>();

        for (Player victim : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != victim.getWorld()) continue;

            Location loc = victim.getLocation();

            if (player.getLocation().distance(loc) <= 5 && player != victim && !victim.isDead()) {
                world.strikeLightningEffect(loc);
                pass = true;

                cache.add(victim);
            }
        }

        if (pass) {
            Energy.clear(player);

            for (Player victim : cache) {
                MWHealth.trueDamage(victim, 4.5, player);
            }

            world.playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 1, (float) 0.5);
            return;
        }

        ActionBar.send(player, "No players within " + ChatColor.RED + 5 + ChatColor.RESET + " meters!");
    }

    private final Map<Player, Integer> INCREMENT = new HashMap<>();

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = Energy.validate(event);
        if (player == null) return;

        if (MWClassManager.get(player) != this) return;

        if (!INCREMENT.containsKey(player)) {
            INCREMENT.put(player, 0);
        } else {
            INCREMENT.put(player, (INCREMENT.get(player) + 1) % 3);
        }

        if (INCREMENT.get(player) == 0) {
            String name = player.getName();

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + name + " speed 3 1");
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + name + " regeneration 5 0");
        }

        Energy.add(player, 25);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player player = victim.getKiller();

        if (player == null || victim == player) return;
        if (!MWClassManager.isMW(player)) return;

        if (MWClassManager.get(player) == this) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + player.getName() + " strength 6");
        }
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

            List<ItemStack> potions = MWPotions.createBasic(this, 2, 7, 2);

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, helmet, null, null, null, null);
        }

        MWKit.assignItems(player, items);
    }
}
