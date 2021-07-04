package net.nuggetmc.mw.mwclass.classes;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.ActionBar;
import net.nuggetmc.mw.utils.PotionUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class MWHerobrine extends MWClass {

    private final Map<Player, Integer> increment = new HashMap<>();

    public MWHerobrine() {
        this.name = "Herobrine";
        this.icon = Material.DIAMOND_SWORD;
        this.color = ChatColor.YELLOW;

        this.playstyles = new Playstyle[] {
            Playstyle.DAMAGE,
            Playstyle.CONTROL
        };

        this.diamonds = new Diamond[] {
            Diamond.SWORD
        };

        this.classInfo = new MWClassInfo(
            "Wrath",
            "Unleash the wrath of Herobrine, striking all nearby enemies in a 5 block radius for &a4.5 &rtrue damage.",
            "Power",
            "Killing an emeny grants you Strength I for &a6 &rseconds.",
            "Flurry",
            "Every &a3 &rattacks will grant you Speed II for 3 seconds and Regeneration I for 5 seconds.",
            "Treasure Hunter",
            "Increases the chance to find treasure chests by &a300% &rwhen mining."
        );

        this.classInfo.addEnergyGainType("Melee", 25);
        this.classInfo.addEnergyGainType("Bow", 25);
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
            energyManager.clear(player);

            for (Player victim : cache) {
                mwhealth.trueDamage(victim, 4.5, null);
            }

            world.playSound(player.getLocation(), Sound.ENDERMAN_DEATH, 1, (float) 0.5);
            return;
        }

        ActionBar.send(player, "No players within " + ChatColor.RED + 5 + ChatColor.RESET + " meters!");
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) != this) return;

        if (!increment.containsKey(player)) {
            increment.put(player, 0);
        } else {
            increment.put(player, (increment.get(player) + 1) % 3);
        }

        if (increment.get(player) == 0) {
            PotionUtils.effect(player, "speed", 3, 1);
            PotionUtils.effect(player, "regeneration", 5);
        }

        energyManager.add(player, 25);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player player = victim.getKiller();

        if (player == null || victim == player) return;
        if (!manager.isMW(player)) return;

        if (manager.get(player) == this) {
            PotionUtils.effect(player, "strength", 6);
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
