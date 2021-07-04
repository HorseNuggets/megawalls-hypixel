package net.nuggetmc.mw.mwclass.classes;

import com.google.common.collect.Sets;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.ActionBar;
import net.nuggetmc.mw.utils.ParticleUtils;
import net.nuggetmc.mw.utils.PotionUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class MWSpider extends MWClass {

    private final Set<Material> leapExceptions;
    private final Set<Player> leapingCache = new HashSet<>();
    private final Map<Player, SkitterData> skitterData = new HashMap<>();
    private final Map<Player, Integer> increment = new HashMap<>();

    public MWSpider() {
        this.name = "Spider";
        this.icon = Material.WEB;
        this.color = ChatColor.DARK_GRAY;

        this.playstyles = new Playstyle[] {
            Playstyle.MOBILITY,
            Playstyle.RUSHER
        };

        this.diamonds = new Diamond[] {
            Diamond.SWORD,
            Diamond.BOOTS
        };

        this.classInfo = new MWClassInfo(
            "Leap",
            "Leap forward into the air, applying Slowness I to all enemies in a 4 block radius for &a4 &rseconds.\nYou deal &a1.5x &rthe amount of fall damage you take, up to a maximum of &a12 HP &rdealt.\nYou gain Absorption I for &a5 &rseconds upon casting.",
            "Venom Strike",
            "For every &a4 &rmelee attacks, you will poison your opponent, dealing 3 damage over &a5 &rseconds.",
            "Skitter",
            "If you melee enemies &a4 &rtimes after landing with Leap within 3 seconds, you gain Speed I for &a5 &rseconds and earn &a20 &renergyManager.",
            "Iron Rush",
            "When digging with a shovel, you will receive an iron ingot for every &a1 &rblock mined."
        );

        this.classInfo.addEnergyGainType("Melee", 8);
        this.classInfo.addEnergyGainType("Bow", 8);
        this.classInfo.addEnergyGainType("Per Second", 4);

        leapExceptions = Sets.newHashSet(Material.AIR, Material.LONG_GRASS, Material.DOUBLE_PLANT);
    }

    @Override
    public void ability(Player player) {
        energyManager.clear(player);
        PotionUtils.effect(player, "absorption", 5);

        World world = player.getWorld();
        Location loc = player.getLocation();

        world.playSound(loc, Sound.SPIDER_IDLE, 1, 1);

        Vector dir = loc.getDirection().clone();
        double y = (0.2 * Math.pow(dir.getY(), 2)) + 0.7;

        dir.setY(y);
        dir.multiply(1.8);

        player.setVelocity(dir);

        leapingCache.add(player);

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                Location loc = player.getLocation().add(0, -1, 0);

                if (((Entity) player).isOnGround() || !leapExceptions.contains(loc.getBlock().getType())) {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {

                        if (leapingCache.contains(player)) {
                            leapingCache.remove(player);
                            skitter(player);
                        }

                    }, 5);

                    this.cancel();
                }
            }
        };

        task.runTaskTimer(plugin, 5, 2);
    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;

        Player player = (Player) event.getEntity();

        if (manager.get(player) == this) {
            if (leapingCache.contains(player)) {
                leapingCache.remove(player);

                skitter(player);
                spiderDamage(player, event);
            }
        }
    }

    private void spiderDamage(Player player, EntityDamageEvent event) {
        World world = player.getWorld();
        Location loc = player.getLocation();

        double dmg = event.getDamage() * 1.5;
        if (dmg > 12) dmg = 12;

        for (Player victim : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != victim.getWorld()) continue;

            if (player != victim && !victim.isDead() && loc.distance(victim.getLocation()) <= 4) {
                victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 5 * 20, 0));

                mwhealth.trueDamage(victim, dmg, player);
            }
        }

        world.playSound(loc, Sound.EXPLODE, (float) 0.7, (float) 0.5);

        ParticleUtils.play(EnumParticle.EXPLOSION_LARGE, loc, 0.1, 0.1, 0.1, 0, 3);
        ParticleUtils.play(EnumParticle.LAVA, loc, 0.3, 0.3, 0.3, 0, 10);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (leapingCache.contains(player)) {
                leapingCache.remove(player);
                skitter(player);
            }
        }, 5);
    }

    static class SkitterData {
        public SkitterData(BukkitRunnable task, int time, int count) {
            this.task = task;
            this.time = time;
            this.count = count;
        }

        private BukkitRunnable task;

        public int time;

        private int count;

        public void add() {
            if (time != 30) {
                count++;
            }
        }

        public int count() {
            return count;
        }
    }

    private void skitter(Player player) {
        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                SkitterData data = skitterData.get(player);

                if (data == null || data.time <= 0) {
                    if (skitterData.containsKey(player)) {
                        skitterData.remove(player);
                    }

                    ActionBar.clear(player);

                    this.cancel();
                    return;
                }

                int count = data.count();

                String msg = "Skitter (" + ChatColor.RED + count + ChatColor.RESET + "/4 Hits) (" + ChatColor.RED + (data.time / 10.0) + "s" + ChatColor.RESET + ")";

                if (count >= 4) {
                    if (skitterData.containsKey(player)) {
                        skitterData.remove(player);
                    }

                    ActionBar.send(player, ChatColor.GREEN + ChatColor.stripColor(msg));
                    energyManager.add(player, 20);

                    PotionUtils.effect(player, "speed", 5);

                    this.cancel();
                    return;
                }

                ActionBar.send(player, msg);

                data.time--;
            }
        };

        skitterData.put(player, new SkitterData(task, 30, 0));
        task.runTaskTimer(plugin, 0, 2);
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) != this) return;

        if (skitterData.containsKey(player)) {
            skitterData.get(player).add();
        }

        if (event.getDamager() instanceof Player) {
            if (!increment.containsKey(player)) {
                increment.put(player, 0);
            } else {
                increment.put(player, (increment.get(player) + 1) % 4);
            }

            if (increment.get(player) == 3) {
                Player victim = (Player) event.getEntity();
                PotionUtils.effect(victim, "poison", 5);
            }
        }

        energyManager.add(player, 8);
    }

    @EventHandler
    public void gathering(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (manager.get(player) == this) {
            ItemStack item = player.getItemInHand();
            if (item == null) return;

            if (item.getType().name().contains("SPADE")) {
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    Block block = event.getBlock();
                    Location loc = block.getLocation().add(0.5, 0.5, 0.5);
                    World world = block.getWorld();

                    world.dropItem(loc, new ItemStack(Material.IRON_INGOT));
                }, 2);
            }
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
            armorEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            armorEnch.put(Enchantment.DURABILITY, 10);

            ItemStack sword = MWItem.createSword(this, Material.DIAMOND_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack toolShovel = MWItem.createTool(this, Material.IRON_SPADE);
            ItemStack boots = MWItem.createArmor(this, Material.DIAMOND_BOOTS, armorEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 2, 8, 2);

            items = MWKit.generate(this, sword, bow, tool, null, toolShovel, potions, null, null, null, boots, null);
        }

        MWKit.assignItems(player, items);
    }
}
