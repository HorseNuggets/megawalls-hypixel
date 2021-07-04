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
import net.nuggetmc.mw.utils.ParticleUtils;
import net.nuggetmc.mw.utils.PotionUtils;
import net.nuggetmc.mw.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class MWSkeleton extends MWClass {

    private final Map<Arrow, Player> explosiveArrows = new HashMap<>();
    private final Map<Arrow, Float> arrowForce = new HashMap<>();
    private final Set<Player> cooldownCache = new HashSet<>();

    public MWSkeleton() {
        this.name = "Skeleton";
        this.icon = Material.BONE;
        this.color = ChatColor.AQUA;

        this.playstyles = new Playstyle[] {
            Playstyle.RANGED,
            Playstyle.CONTROL
        };

        this.diamonds = new Diamond[] {
            Diamond.HELMET
        };

        this.classInfo = new MWClassInfo(
            "Explosive Arrow",
            "You will fire an explosive arrow that deals &a6 &rdamage in a 6 block radius and breaks blocks.",
            "Salvaging",
            "When landing a bow shot onto an opponent, you will receive &a2 &rarrows and &a2 &rhunger.",
            "Agile",
            "You gain Speed II and Regeneration I for &a7 &rseconds after hitting an enemy with a bow shot.\nThe cooldown only affects Speed II.\nCooldown: &a14s",
            "Efficiency",
            "Drops while breaking iron ore, coal ore, and wooden logs are tripled, and drops while mining diamond ore are doubled."
        );

        this.classInfo.addEnergyGainType("Bow", "25 × CHARGE%");
    }

    @Override
    public void ability(Player player) {
        energyManager.clear(player);

        World world = player.getWorld();
        Location loc = player.getEyeLocation();

        Vector dir = loc.getDirection();
        Vector vel = dir.clone().multiply(1.5);

        Arrow arrow = world.spawnArrow(loc.add(dir.getX() * 1.5, -0.1, dir.getZ() * 1.5), new Vector(0, 0, 0), 1, 0);
        arrow.setShooter(player);
        arrow.setVelocity(vel);

        world.playSound(arrow.getLocation(), Sound.FUSE, (float) 0.6, (float) 1.4);

        particles(arrow);

        explosiveArrows.put(arrow, player);
    }

    private void particles(Arrow arrow) {
        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                if (arrow == null || arrow.isDead() || !explosiveArrows.containsKey(arrow)) {
                    this.cancel();
                    return;
                }

                ParticleUtils.play(EnumParticle.EXPLOSION_NORMAL, arrow.getLocation(), 0.15, 0.15, 0.15, 0.05, 4);
            }
        };

        task.runTaskTimer(plugin, 0, 2);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile proj = event.getEntity();

        if (explosiveArrows.containsKey(proj)) {
            WorldUtils.createNoDamageExplosion(proj.getLocation(), 2);

            explosionDamage(proj, explosiveArrows.get(proj));

            explosiveArrows.remove(proj);

            proj.remove();
        }
    }

    public void explosionDamage(Projectile proj, Player player) {
        for (Player victim : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != victim.getWorld()) continue;

            if (player != victim && !victim.isDead() && proj.getLocation().distance(victim.getLocation()) < 6) {
                victim.damage(6, player);
            }
        }
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = energyManager.validate(event);

        if (player == null) return;
        if (manager.get(player) != this) return;
        if (!(event.getDamager() instanceof Arrow)) return;

        Arrow arrow = (Arrow) event.getDamager();

        if (arrowForce.containsKey(arrow)) {
            float force = arrowForce.get(arrow);
            energyManager.add(player, (int) (25 * force));
        }

        PotionUtils.effect(player, "regeneration", 7);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "give " + player.getName() + " arrow 2");

        mwhealth.feed(player, 4);

        if (!cooldownCache.contains(player)) {
            PotionUtils.effect(player, "speed", 7, 1);

            player.getWorld().playSound(player.getLocation(), Sound.SKELETON_WALK, 1, 1);

            cooldownCache.add(player);

            Bukkit.getScheduler().runTaskLater(plugin, () -> cooldownCache.remove(player), 21 * 20);
        }
    }

    @EventHandler
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getProjectile() instanceof Arrow)) return;

        Player player = (Player) event.getEntity();

        if (manager.get(player) == this) {
            Arrow arrow = (Arrow) event.getProjectile();
            arrowForce.put(arrow, event.getForce());
        }
    }

    @EventHandler
    public void gathering(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (manager.get(player) == this) {
            Block block = event.getBlock();
            Material type = block.getType();
            Collection<ItemStack> drops = block.getDrops();

            int n = 2;

            if (type == Material.DIAMOND_ORE) {
                n = 1;
            }

            final int m = n;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                for (int i = 0; i < m; i++) {
                    for (ItemStack item : drops) {
                        block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), item);
                    }
                }
            }, 2);
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

            Map<Enchantment, Integer> bowEnch = new HashMap<>();
            bowEnch.put(Enchantment.ARROW_DAMAGE, 3);
            bowEnch.put(Enchantment.DURABILITY, 10);

            Map<Enchantment, Integer> armorEnch = new HashMap<>();
            armorEnch.put(Enchantment.PROTECTION_PROJECTILE, 3);
            armorEnch.put(Enchantment.DURABILITY, 10);

            ItemStack sword = MWItem.createSword(this, Material.IRON_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, bowEnch);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack helmet = MWItem.createArmor(this, Material.DIAMOND_HELMET, armorEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 2, 8, 2);

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, helmet, null, null, null, null);
        }

        MWKit.assignItems(player, items);
    }
}
