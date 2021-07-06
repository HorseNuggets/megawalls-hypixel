package net.nuggetmc.mw.mwclass.classes;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.ParticleUtils;
import net.nuggetmc.mw.utils.PotionUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MWZombie extends MWClass {
    
    private final Map<Player, Wrapper> tasks = new HashMap<>();
    private final Map<Player, Integer> increment = new HashMap<>();

    public MWZombie() {
        this.name = "Zombie";
        this.icon = Material.ROTTEN_FLESH;
        this.color = ChatColor.DARK_GREEN;

        this.playstyles = new Playstyle[] {
            Playstyle.TANK,
            Playstyle.SUPPORT
        };

        this.diamonds = new Diamond[] {
            Diamond.CHESTPLATE
        };

        this.classInfo = new MWClassInfo(
            "Circle of Healing",
            "Heal yourself for &a8 HP &rand nearby allies in a 5 block radius for &a5 HP&r.",
            "Toughness",
            "You will gain Resistance I for 1 second after getting attacked every &a3 &rtimes.",
            "Berserk",
            "After being hit by an arrow, you will receive Strength I and Speed II for &a6 &rseconds.\nCooldown: &a15s",
            "Well Trained",
            "You will gain Haste III for &a5 &rseconds when breaking blocks."
        );

        this.classInfo.addEnergyGainType("Melee", 12);
        this.classInfo.addEnergyGainType("Bow", 12);
        this.classInfo.addEnergyGainType("When Hit", 1);
        this.classInfo.addEnergyGainType("When Bowed", 2);
    }

    @Override
    public void ability(Player player) {
        energyManager.clear(player);

        double health = player.getHealth();

        if (health < 32) {
            player.setHealth(health + 8);
        } else {
            player.setHealth(40);
        }

        Location loc = player.getEyeLocation();
        ParticleUtils.play(EnumParticle.HEART, loc, 0.5, 0.5, 0.5, 0.15, 12);

        player.getWorld().playSound(loc, Sound.LEVEL_UP, 1, 2);
    }

    @EventHandler
    public void gathering(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (manager.get(player) == this) {
            PotionUtils.effect(player, "haste", 5, 2);
        }
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) == this) {
            energyManager.add(player, 12);
        }

        Player victim = (Player) event.getEntity();

        if (manager.get(victim) == this) {
            if (!increment.containsKey(victim)) {
                increment.put(victim, 0);
            } else {
                increment.put(victim, (increment.get(victim) + 1) % 3);
            }

            if (increment.get(victim) == 0) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 20, 0));
                victim.getWorld().playSound(victim.getLocation(), Sound.GHAST_FIREBALL, (float) 0.2, 2);
            }

            int n;

            if (event.getDamager() instanceof Arrow) {
                berserk(victim);
                n = 2;
            } else {
                n = 1;
            }

            energyManager.add(victim, n);
        }
    }

    static class Wrapper {
        public Wrapper(BukkitRunnable task, int time) {
            this.task = task;
            this.time = time;
        }

        private BukkitRunnable task;

        public int time;
    }

    private void berserk(Player player) {
        if (tasks.containsKey(player)) {
            return;
        }

        World world = player.getWorld();
        world.playSound(player.getEyeLocation(), Sound.ZOMBIE_HURT, (float) 0.2, 2);

        player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 6 * 20, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 6 * 20, 1));

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                Wrapper wpr = tasks.get(player);

                if (wpr == null || wpr.time <= 0) {
                    if (tasks.containsKey(player)) {
                        tasks.remove(player);
                    }

                    this.cancel();
                    return;
                }

                if (wpr.time >= 30 && !player.isDead() && player.isOnline()) {
                    ParticleUtils.play(EnumParticle.VILLAGER_ANGRY, player.getEyeLocation(), 0.5, 0.5, 0.5, 0.15, 1);
                }

                wpr.time--;
            }
        };

        tasks.put(player, new Wrapper(task, 42));
        task.runTaskTimer(MegaWalls.getInstance(), 0, 10);
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

            Map<Enchantment, Integer> helmetEnch = new HashMap<>();
            helmetEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            helmetEnch.put(Enchantment.DURABILITY, 10);

            Map<Enchantment, Integer> chestplateEnch = new HashMap<>();
            chestplateEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 3);
            chestplateEnch.put(Enchantment.DURABILITY, 10);

            ItemStack sword = MWItem.createSword(this, Material.IRON_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack helmet = MWItem.createArmor(this, Material.IRON_HELMET, helmetEnch);
            ItemStack chestplate = MWItem.createArmor(this, Material.DIAMOND_CHESTPLATE, chestplateEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 1, 10, 2);

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, helmet, chestplate, null, null, null);
        }

        MWKit.assignItems(player, items);
    }
}
