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
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class MWCreeper extends MWClass {

    private final Map<BukkitRunnable, Integer> tasks = new HashMap<>();
    private final Map<TNTPrimed, Player> miniTNTList = new HashMap<>();
    private final Set<Player> willpowerList = new HashSet<>();

    public MWCreeper() {
        this.name = new String[]{"苦力怕","Creeper","CRE"};
        this.icon = Material.TNT;
        this.color = ChatColor.GREEN;

        this.playstyles = new Playstyle[] {
            Playstyle.CONTROL,
            Playstyle.DAMAGE
        };

        this.diamonds = new Diamond[] {
            Diamond.LEGGINGS
        };

        this.classInfo = new MWClassInfo(
            "Detonate",
            "You will set off an explosion that deals up to &a10 &rtrue damage in a 6 block radius with a &a3 &rsecond delay.\nHowever, you will lose &a0.75 &rdamage for every block that separates you and an opponent, with a minimum of &a5 &rdamage.",
            "Fission Heart",
            "You will spawn a Creeper with Resistance V on death.\nPlacing a TNT block will instantly prime it, but it will only deal &a87.5% &rof its vanilla damage and will not destroy blocks.\nWhile sneaking, you can place unprimed vanilla TNT.",
            "Willpower",
            "When your health drops below 20 HP, you gain Speed II and Regeneration I for &a12 &rseconds.\nIf you are hit by one of your own explosions above 25 HP, you will instead receive the Speed II effect for &a4 &rseconds with half of the cooldown.\nCooldown: &a20s",
            "TNT Mining",
            "A TNT block will be dropped for every &a1 &rcoal ore mined."
        );

        this.classInfo.addEnergyGainType("Melee", 20);
        this.classInfo.addEnergyGainType("Bow", 20);
    }

    @Override
    public void ability(Player player) {
        energyManager.clear(player);

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                if (!tasks.containsKey(this) || player == null || player.isDead() || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                int n = tasks.get(this);

                Location loc = player.getEyeLocation();
                World world = player.getWorld();

                ParticleUtils.play(EnumParticle.VILLAGER_ANGRY, loc, 0.5, 0.5, 0.5, 0.15, 1);

                if (n == 0) {
                    explode(player);
                    this.cancel();
                    return;
                }

                else if (n % 4 == 0 && n != 12) {
                    ParticleUtils.play(EnumParticle.EXPLOSION_NORMAL, loc, 0.5, 0.5, 0.5, 0.15, 10);
                    world.playSound(loc, Sound.WOOD_CLICK, 1, 1);
                }

                else if (n == 12) {
                    ParticleUtils.play(EnumParticle.EXPLOSION_NORMAL, loc, 0.5, 0.5, 0.5, 0.15, 20);
                    world.playSound(loc, Sound.CREEPER_HISS, 1, 1);
                }

                tasks.put(this, n - 1);
            }
        };

        tasks.put(task, 12);
        task.runTaskTimer(plugin, 0, 5);
    }

    private void explode(Player player) {
        WorldUtils.createNoDamageExplosion(player.getLocation(), 2);

        for (Player victim : Bukkit.getOnlinePlayers()) {
            if (player.getWorld() != victim.getWorld()) continue;

            Location pLoc = player.getLocation();
            Location vLoc = victim.getLocation();

            double dist = pLoc.distance(vLoc);

            if (dist <= 6 && player != victim && !victim.isDead()) {
                float dmg = (float) (10 - ((int) dist) * 0.75);
                if (dmg < 5) dmg = 5;

                mwhealth.trueDamage(victim, dmg, player);
            }
        }
    }

    @EventHandler
    public void miniTNTPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (manager.get(player) == this) {
            Block block = event.getBlock();

            if (block.getType() != Material.TNT) return;
            if (player.isSneaking()) return;

            block.setType(Material.AIR);

            TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
            tnt.setFuseTicks(20);
            tnt.setYield(2);

            miniTNTList.put(tnt, player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void explosionCancel(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof TNTPrimed)) return;

        TNTPrimed tnt = (TNTPrimed) event.getDamager();

        if (miniTNTList.containsKey(tnt)) {
            Player player = miniTNTList.get(tnt);

            if (manager.get(player) == this) {
                Player victim = (Player) event.getEntity();

                if (player == victim) {
                    willpower(victim, victim.getHealth() - event.getDamage(), true);
                } else {
                    energyManager.add(player, 10);
                }
            }
        }
    }

    @EventHandler
    public void blockList(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) return;

        TNTPrimed tnt = (TNTPrimed) event.getEntity();

        if (miniTNTList.containsKey(tnt)) {
            miniTNTList.remove(tnt);
            event.blockList().clear();
        }
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) == this) {
            energyManager.add(player, 20);
        }

        Player victim = (Player) event.getEntity();

        if (manager.get(victim) == this) {
            willpower(victim, player.getHealth() - event.getDamage(), false);
        }
    }

    private void willpower(Player player, double health, boolean own) {
        if ((own && health >= 25) || (!own && health < 20)) {
            if (willpowerList.contains(player)) return;

            Location loc = player.getEyeLocation();

            player.getWorld().playSound(loc, Sound.GHAST_FIREBALL, (float) 0.2, 2);

            int n;

            if (own) {
                n = 10;
                PotionUtils.effect(player, "speed", 4, 1);
            }

            else {
                n = 20;
                PotionUtils.effect(player, "regeneration", 12);
                PotionUtils.effect(player, "speed", 12, 1);
            }

            ParticleUtils.play(EnumParticle.VILLAGER_ANGRY, loc, 0.5, 0.5, 0.5, 0.15, 1);

            willpowerList.add(player);

            Bukkit.getScheduler().runTaskLater(plugin, () -> willpowerList.remove(player), n * 20);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (manager.get(player) == this) {
            Creeper creeper = (Creeper) player.getWorld().spawnEntity(player.getLocation(), EntityType.CREEPER);
            creeper.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 4));
        }
    }

    @EventHandler
    public void gathering(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (manager.get(player) == this) {
            Block block = event.getBlock();
            if (block.getType() != Material.COAL_ORE) return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                block.getWorld().dropItem(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(Material.TNT));
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

            Map<Enchantment, Integer> armorEnch = new HashMap<>();
            armorEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
            armorEnch.put(Enchantment.DURABILITY, 10);
            armorEnch.put(Enchantment.PROTECTION_EXPLOSIONS, 5);

            ItemStack sword = MWItem.createSword(this, Material.IRON_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack leggings = MWItem.createArmor(this, Material.DIAMOND_LEGGINGS, armorEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 2, 8, 2);

            List<ItemStack> extra = new ArrayList<>();
            extra.add(new ItemStack(Material.TNT, 16));

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, null, null, leggings, null, extra);
        }

        MWKit.assignItems(player, items);
    }
}
