package net.nuggetmc.mw.mwclass.classes;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.MWHealth;
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

public class MWCreeper implements MWClass {

    private MegaWalls plugin;

    private final String NAME;
    private final Material ICON;
    private final ChatColor COLOR;
    private final Playstyle[] PLAYSTYLES;
    private final Diamond[] DIAMONDS;
    private final MWClassInfo CLASS_INFO;

    public MWCreeper() {
        this.plugin = MegaWalls.getInstance();

        NAME = "Creeper";
        ICON = Material.TNT;
        COLOR = ChatColor.GREEN;

        PLAYSTYLES = new Playstyle[]
        {
            Playstyle.CONTROL,
            Playstyle.DAMAGE
        };

        DIAMONDS = new Diamond[]
        {
            Diamond.LEGGINGS
        };

        CLASS_INFO = new MWClassInfo
        (
            "Detonate",
            "You will set off an explosion that deals up to &a10 &rtrue damage in a 6 block radius with a &a3 &rsecond delay.\nHowever, you will lose &a0.75 &rdamage for every block that separates you and an opponent, with a minimum of &a5 &rdamage.",
            "Fission Heart",
            "You will spawn a Creeper with Resistance V on death.\nPlacing a TNT block will instantly prime it, but it will only deal &a87.5% &rof its vanilla damage and will not destroy blocks.\nWhile sneaking, you can place unprimed vanilla TNT.",
            "Willpower",
            "When your health drops below 20 HP, you gain Speed II and Regeneration I for &a12 &rseconds.\nIf you are hit by one of your own explosions above 25 HP, you will instead receive the Speed II effect for &a4 &rseconds with half of the cooldown.\nCooldown: &a20s",
            "TNT Mining",
            "A TNT block will be dropped for every &a1 &rcoal ore mined."
        );

        CLASS_INFO.addEnergyGainType("Melee", 20);
        CLASS_INFO.addEnergyGainType("Bow", 20);
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

    private final Map<BukkitRunnable, Integer> TASKS = new HashMap<>();

    @Override
    public void ability(Player player) {
        Energy.clear(player);

        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                if (!TASKS.containsKey(this) || player == null || player.isDead() || !player.isOnline()) {
                    this.cancel();
                    return;
                }

                int n = TASKS.get(this);

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

                TASKS.put(this, n - 1);
            }
        };

        TASKS.put(task, 12);
        task.runTaskTimer(plugin, 0, 5);
    }

    private final Map<TNTPrimed, Player> MINI_TNTS = new HashMap<>();

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

                MWHealth.trueDamage(victim, dmg, player);
            }
        }
    }

    @EventHandler
    public void miniTNTPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        if (MWClassManager.get(player) == this) {
            Block block = event.getBlock();

            if (block.getType() != Material.TNT) return;
            if (player.isSneaking()) return;

            block.setType(Material.AIR);

            TNTPrimed tnt = (TNTPrimed) block.getWorld().spawnEntity(block.getLocation().add(0.5, 0, 0.5), EntityType.PRIMED_TNT);
            tnt.setFuseTicks(20);
            tnt.setYield(2);

            MINI_TNTS.put(tnt, player);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void explosionCancel(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof TNTPrimed)) return;

        TNTPrimed tnt = (TNTPrimed) event.getDamager();

        if (MINI_TNTS.containsKey(tnt)) {
            Player player = MINI_TNTS.get(tnt);

            if (MWClassManager.get(player) == this) {
                Player victim = (Player) event.getEntity();

                if (player == victim) {
                    willpower(victim, victim.getHealth() - event.getDamage(), true);
                } else {
                    Energy.add(player, 10);
                }
            }
        }
    }

    @EventHandler
    public void blockList(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) return;

        TNTPrimed tnt = (TNTPrimed) event.getEntity();

        if (MINI_TNTS.containsKey(tnt)) {
            MINI_TNTS.remove(tnt);
            event.blockList().clear();
        }
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = Energy.validate(event);
        if (player == null) return;

        if (MWClassManager.get(player) == this) {
            Energy.add(player, 20);
        }

        Player victim = (Player) event.getEntity();

        if (MWClassManager.get(victim) == this) {
            willpower(victim, player.getHealth() - event.getDamage(), false);
        }
    }

    private final Set<Player> WILLPOWER_LIST = new HashSet<>();

    private void willpower(Player player, double health, boolean own) {
        if ((own && health >= 25) || (!own && health < 20)) {
            if (WILLPOWER_LIST.contains(player)) return;

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

            WILLPOWER_LIST.add(player);


            Bukkit.getScheduler().runTaskLater(plugin, () -> WILLPOWER_LIST.remove(player), n * 20);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (MWClassManager.get(player) == this) {
            Creeper creeper = (Creeper) player.getWorld().spawnEntity(player.getLocation(), EntityType.CREEPER);
            creeper.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 1000000, 4));
        }
    }

    @EventHandler
    public void gathering(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (MWClassManager.get(player) == this) {
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
