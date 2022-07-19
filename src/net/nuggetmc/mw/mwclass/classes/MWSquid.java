package net.nuggetmc.mw.mwclass.classes;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.ItemUtils;
import net.nuggetmc.mw.utils.ParticleUtils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class MWSquid extends MWClass {

    private final Set<Player> rejuvenateList = new HashSet<>();

    public MWSquid() {
        this.name = new String[]{"鱿鱼","Squid","SQU"};
        this.icon = Material.INK_SACK;
        this.color = ChatColor.BLUE;

        this.playstyles = new Playstyle[] {
            Playstyle.CONTROL,
            Playstyle.TANK
        };

        this.diamonds = new Diamond[] {
            Diamond.BOOTS
        };

        this.classInfo = new MWClassInfo(
            "Squid Splash",
            "You pull opponents inwards, dealing 3.5 damage to all enemies within a 5 block radius.\n"
                + "You are healed by &a70% &rof the total damage dealt.\n"
                + "You can heal for a maximum of &a7 HP&r.",
            "Inner Ink",
            "Every time you finish drinking a potion, you give blindness to enemies in a &a5 &rblock radius for &a3 &rseconds.",
            "Rejuvenate",
            "When you fall below &a21 HP&r, you will receive Regeneration V and Resistance I for &a1.5 &rseconds.\n"
                + "Every 0.3 seconds of Regeneration V heals for &a2 HP&r.\n"
                + "Cooldown: &a40s",
            "Luck Of The Sea",
            "On every kill, you will reveive &a2 &rAbsorption II &rpotions that each last &a1 &rminute."
        );

        this.classInfo.addEnergyGainType("Melee", 10);
        this.classInfo.addEnergyGainType("Bow", 10);
    }

    @Override
    public void ability(Player player) {
        energyManager.clear(player);

        World world = player.getWorld();
        Location loc = player.getLocation();

        world.playSound(loc, Sound.SPLASH, 1, 1);
        ParticleUtils.play(EnumParticle.WATER_SPLASH, loc, 0.5, 0.5, 0.5, 1, 500);

        double healedAmount = 0;

        for (Player victim : inRange(player, 5)) {
            Vector vel = loc.toVector().subtract(victim.getLocation().toVector());
            double y = vel.getY();

            if (Math.abs(y) > 0.4) {
                vel.setY(0.4 / y);
            }

            double len = vel.length();

            if (len > 0.9) {
                vel.multiply(0.9 / len);
            }

            vel.add(victim.getVelocity());
            victim.setVelocity(vel);

            double damage = 3.5;
            healedAmount += damage * 0.7;
            
            mwhealth.trueDamage(victim, damage, player);
        }

        if (healedAmount > 7) healedAmount = 7;

        mwhealth.heal(player, healedAmount);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player player = victim.getKiller();

        if (player == null || victim == player) return;

        if (manager.get(player) == this) {
            givePotions(player);
        }
    }

    private void givePotions(Player player) {
        ItemStack potions = MWPotions.createAbsorptionPotions(plugin.isChinese()?this.name[0]:this.name[1], this.color, 2, 60);
        ItemUtils.givePlayerItemStack(player, potions);
    }

    @EventHandler
    public void onPotionDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (manager.get(player) == this && event.getItem().getType() == Material.POTION) {
            Location loc = player.getLocation();

            player.getWorld().playSound(loc, Sound.GHAST_FIREBALL, (float) 0.75, 2);
            ParticleUtils.play(EnumParticle.SMOKE_LARGE, loc, 0.5, 0.5, 0.5, 0.1, 50);

            for (Player victim : inRange(player, 5)) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 3 * 20, 0));
                ParticleUtils.play(EnumParticle.SMOKE_LARGE, victim.getEyeLocation(), 0.5, 0.5, 0.5, 0.1, 50);
            }
        }
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) == this) {
            energyManager.add(player, 10);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Player player = (Player) event.getEntity();

        if (manager.get(player) == this) {
            rejuvenate(player, player.getHealth() - event.getDamage());
        }
    }

    private void rejuvenate(Player player, double health) {
        if (health >= 21) return;
        if (rejuvenateList.contains(player)) return;

        Location loc = player.getEyeLocation();

        player.getWorld().playSound(loc, Sound.SPLASH2, 1, 1);

        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 30, 4));
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 30, 0));

        ParticleUtils.play(EnumParticle.VILLAGER_ANGRY, loc, 0.5, 0.5, 0.5, 0.15, 1);

        rejuvenateList.add(player);

        Bukkit.getScheduler().runTaskLater(plugin, () -> rejuvenateList.remove(player), 40 * 20);
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
            armorEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
            armorEnch.put(Enchantment.DEPTH_STRIDER, 2);
            armorEnch.put(Enchantment.DURABILITY, 10);

            ItemStack sword = MWItem.createSword(this, Material.IRON_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack boots = MWItem.createArmor(this, Material.DIAMOND_BOOTS, armorEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 3, 6, 1);

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, null, null, null, boots, null);
        }

        MWKit.assignItems(player, items);
    }
}
