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
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.*;

public class MWGolem implements MWClass {

    private MegaWalls plugin;

    private final String NAME;
    private final Material ICON;
    private final ChatColor COLOR;
    private final Playstyle[] PLAYSTYLES;
    private final Diamond[] DIAMONDS;
    private final MWClassInfo CLASS_INFO;

    public MWGolem() {
        this.plugin = MegaWalls.getInstance();

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

    private Set<Player> inRange(Player player) {
        World world = player.getWorld();
        Location locUp = player.getEyeLocation();
        Set<Player> result = new HashSet<>();

        for (Player victim : Bukkit.getOnlinePlayers()) {
            if (world != victim.getWorld()) continue;

            Location loc = victim.getEyeLocation();

            if (locUp.distance(loc) <= 4.5 && player != victim && !victim.isDead()) {
                result.add(victim);
            }
        }

        return result;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void ability(Player player) {
        Energy.clear(player);
        World world = player.getWorld();
        Location locUp = player.getEyeLocation();

        for (Player victim : inRange(player)) {
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20, 1));

            Vector vel = locUp.toVector().subtract(victim.getEyeLocation().toVector());
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
        }

        Vector vel = new Vector(0, -1, 0);
        Location loc = player.getLocation();

        loc.add(0, 6, 0);
        int theta = 30;

        for (int i = 0; i < 12; i++) {
            int deg = theta * i;
            double rad = Math.toRadians(deg);

            double x = 3 * Math.cos(rad);
            double z = 3 * Math.sin(rad);

            Location pt = loc.clone().add(x, 0, z);
            FallingBlock block = world.spawnFallingBlock(pt, Material.IRON_BLOCK, (byte) 0);

            block.setVelocity(vel);

            Bukkit.getScheduler().runTaskLater(plugin, block::remove, 60);
        }

        ParticleUtils.play(EnumParticle.LAVA, locUp, 3, 0.1, 3, 10, 40);

        world.playSound(locUp, Sound.ANVIL_LAND, 1, 2);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player victim : inRange(player)) {
                MWHealth.trueDamage(victim, 6, player);
            }

            ParticleUtils.play(EnumParticle.EXPLOSION_HUGE, loc, 0.1, 0.1, 0.1, 0, 3);

            world.playSound(loc, Sound.EXPLODE, 1, 1);
            world.playSound(loc, Sound.EXPLODE, 1, 1);
            world.playSound(loc, Sound.EXPLODE, 1, (float) 0.7);
            world.playSound(loc, Sound.EXPLODE, 1, (float) 0.7);
        }, 7);
    }

    @EventHandler
    public void onBlockChange(EntityChangeBlockEvent event) {
        if (!(event.getEntity() instanceof FallingBlock)) return;

        FallingBlock block = (FallingBlock) event.getEntity();

        if (block.getMaterial() == Material.IRON_BLOCK) {
            event.setCancelled(true);
        }
    }

    private final Map<Player, Integer> INCREMENT = new HashMap<>();

    @EventHandler
    public void gathering(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (MWClassManager.get(player) == this) {
            if (!INCREMENT.containsKey(player)) {
                INCREMENT.put(player, 0);
            } else {
                INCREMENT.put(player, (INCREMENT.get(player) + 1) % 4);
            }

            if (INCREMENT.get(player) == 0) {
                Block block = event.getBlock();
                Location location = block.getLocation();

                location.add(0.5, 0.5, 0.5);

                if (block.getType() == Material.LOG || block.getType() == Material.LOG_2) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        block.getWorld().dropItem(location, new ItemStack(Material.IRON_BLOCK));
                    }, 2);
                }
            }
        }
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player player = victim.getKiller();

        if (player == null || victim == player) return;
        if (!MWClassManager.isMW(player)) return;

        if (MWClassManager.get(player) == this) {
            PotionUtils.effect(player, "absorption", 10, 1);
        }
    }

    @EventHandler
    public void hit(EntityDamageByEntityEvent event) {
        Player player = Energy.validate(event);
        if (player == null) return;

        if (MWClassManager.get(player) == this) {
            Energy.add(player, 10);
        }

        Player victim = (Player) event.getEntity();

        if (MWClassManager.get(victim) == this) {
            if (event.getDamager() instanceof Arrow) {
                PotionUtils.effect(victim, "resistance", 9);
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

            items = MWKit.generate(this, sword, bow, tool, toolAxe, null, potions, null, chestplate, null, boots, null);
        }

        MWKit.assignItems(player, items);
    }
}
