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
import net.nuggetmc.mw.utils.ActionBar;
import net.nuggetmc.mw.utils.ParticleUtils;
import net.nuggetmc.mw.utils.PotionUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MWEnderman extends MWClass {

    private final Map<Player, Wrapper> cooldownCacheAbility = new HashMap<>();
    private final Set<Player> cooldownCacheRegen = new HashSet<>();
    private final Map<Player, Integer> incrementGathering = new HashMap<>();
    private final Map<Player, Integer> incrementEChest = new HashMap<>();

    public MWEnderman() {
        this.name = new String[]{"末影人","Enderman"};
        this.icon = Material.ENDER_PEARL;
        this.color = ChatColor.DARK_PURPLE;

        this.playstyles = new Playstyle[] {
            Playstyle.MOBILITY,
            Playstyle.FIGHTER
        };

        this.diamonds = new Diamond[] {
            Diamond.BOOTS
        };

        this.classInfo = new MWClassInfo(
            "Teleport",
            "Teleport up to &a25 &rblocks onto an opponent, gaining Speed III for &a5 &rseconds.\nCooldown: &a6s",
            "Ender Heart",
            "For every &a3 &rdeaths, you will keep your inventory outside of Diamond-related items.\nYou heal &a3 HP &ron kill, and &a1.5 HP &ron assist.",
            "Soul Charge",
            "You gain &a10 &rseconds of Regeneration I when you reach 100 energy.\nCooldown: &a5s",
            "Enderblocks",
            "You will instantly break all adjacent blocks of a similar type for every &a3 &rore, stone, or wooden logs broken."
        );

        this.classInfo.addEnergyGainType("Melee", 20);
        this.classInfo.addEnergyGainType("Bow", 20);
    }

    static class Wrapper {
        public Wrapper(int time) {
            this.time = time;
        }

        public int time;
    }

    @Override
    public void ability(Player player) {

        if (cooldownCacheAbility.containsKey(player)) return;

        World world = player.getWorld();
        Location loc = player.getLocation();
        Vector dir = loc.getDirection();
        Set<Location> points = new HashSet<>();

        for (int i = 0; i <= 25; i++) {
            points.add(loc.clone().add(dir.clone().multiply(i)));
        }

        Set<Player> valid = new HashSet<>();

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (world != target.getWorld()) continue;

            if (target != player && !target.isDead() && loc.distance(target.getLocation()) <= 25) {
                valid.add(target);
            }
        }

        Map<Player, Double> stream = new HashMap<>();

        for (Player target : valid) {
            double low = 100;

            for (Location pt : points) {
                double dist = pt.distance(target.getLocation());

                if (dist < low) {
                    low = dist;
                }
            }

            stream.put(target, low);
        }

        Player target = null;
        double hdist = 100;

        for (Map.Entry<Player, Double> entry : stream.entrySet()) {
            Player check = entry.getKey();
            double dist = entry.getValue();

            if (target == null || dist < hdist) {
                target = check;
                hdist = dist;
            }
        }

        if (target != null) {
            energyManager.clear(player);
            player.teleport(target);

            PotionUtils.effect(player, "speed", 5, 2);
            Location[] locs = new Location[]{loc, target.getLocation()};

            for (Location point : locs) {
                ParticleUtils.play(EnumParticle.SPELL_WITCH, point, 0.5, 0.5, 0.5, 0.15, 40);
                world.playSound(point, Sound.ENDERMAN_TELEPORT, 1, 1);
            }

            BukkitRunnable task = new BukkitRunnable() {

                @Override
                public void run() {
                    Wrapper wpr = cooldownCacheAbility.get(player);

                    if (wpr == null || wpr.time <= 0) {
                        cooldownCacheAbility.remove(player);

                        ActionBar.clear(player);

                        this.cancel();
                        return;
                    }

                    double num = wpr.time / 10.0;

                    String msg = "Teleport (" + ChatColor.RED + num + "s" + ChatColor.RESET + ")";
                    ActionBar.send(player, msg);

                    wpr.time--;
                }
            };

            cooldownCacheAbility.put(player, new Wrapper(60));
            task.runTaskTimer(plugin, 0, 2);

            return;
        }

        ActionBar.send(player, "No players within " + ChatColor.RED + 25 + ChatColor.RESET + " meters!");
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player player = victim.getKiller();

        if (player == null || victim == player) return;

        if (manager.get(player) == this) {
            mwhealth.heal(player, 3);
        }
    }

    public boolean isKeepInventory(Player player) {
        return incrementEChest.get(player) == 0;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (manager.get(player) == this) {
            if (!incrementEChest.containsKey(player)) {
                incrementEChest.put(player, 0);
            } else {
                incrementEChest.put(player, (incrementEChest.get(player) + 1) % 3);
            }

            if (isKeepInventory(player)) {
                ParticleUtils.play(EnumParticle.SPELL_WITCH, player.getLocation(), 0.5, 0.5, 0.5, 0.15, 40);
                event.setKeepInventory(true);
            }
        }
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) != this) return;


        int energy = energyManager.fetch(player);

        if (energy >= 80 && energy < 100) {
            if (!cooldownCacheRegen.contains(player)) {
                PotionUtils.effect(player, "regeneration", 10);

                cooldownCacheRegen.add(player);

                Bukkit.getScheduler().runTaskLater(plugin, () -> cooldownCacheRegen.remove(player), 5 * 20);
            }
        }

        energyManager.add(player, 20);

    }

    @EventHandler
    public void gathering(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (manager.get(player) == this) {
            if (!incrementGathering.containsKey(player)) {
                incrementGathering.put(player, 0);
            } else {
                incrementGathering.put(player, (incrementGathering.get(player) + 1) % 3);
            }

            if (incrementGathering.get(player) == 0) {
                Block block = event.getBlock();
                Location loc = block.getLocation();
                Material type = block.getType();
                Set<Location> adj = new HashSet<>();

                for (int i = -1; i <= 1; i += 2) {
                    adj.add(loc.clone().add(i, 0, 0));
                    adj.add(loc.clone().add(0, i, 0));
                    adj.add(loc.clone().add(0, 0, i));
                }

                loc.getWorld().playSound(loc, Sound.ENDERMAN_HIT, 1, 1);

                for (Block next : adj.stream().map(Location::getBlock).collect(Collectors.toList())) {
                    if (next.getType() == type) {
                        next.breakNaturally();
                    }
                }
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
            armorEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            armorEnch.put(Enchantment.PROTECTION_FALL, 4);
            armorEnch.put(Enchantment.DURABILITY, 10);

            ItemStack sword = MWItem.createSword(this, Material.IRON_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack boots = MWItem.createArmor(this, Material.DIAMOND_BOOTS, armorEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 2, 8, 2);

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, null, null, null, boots, null);
        }

        MWKit.assignItems(player, items);
    }
}
