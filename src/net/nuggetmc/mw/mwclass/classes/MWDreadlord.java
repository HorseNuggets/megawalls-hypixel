package net.nuggetmc.mw.mwclass.classes;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
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
import net.nuggetmc.mw.utils.*;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;

public class MWDreadlord implements MWClass {

    private MegaWalls plugin;

    private final String NAME;
    private final Material ICON;
    private final ChatColor COLOR;
    private final Playstyle[] PLAYSTYLES;
    private final Diamond[] DIAMONDS;
    private final MWClassInfo CLASS_INFO;

    public MWDreadlord() {
        this.plugin = MegaWalls.getInstance();

        NAME = "Dreadlord";
        ICON = Material.NETHER_BRICK_ITEM;
        COLOR = ChatColor.DARK_RED;

        PLAYSTYLES = new Playstyle[]
        {
            Playstyle.RUSHER,
            Playstyle.DAMAGE
        };

        DIAMONDS = new Diamond[]
        {
            Diamond.SWORD,
            Diamond.HELMET
        };

        CLASS_INFO = new MWClassInfo
        (
            "Shadow Burst",
            "Fire three wither skulls at once dealing a total of &a8 &rtrue damage.",
            "Soul Eater",
            "Every &a5 &rattacks will restore 3 hunger and &a2 HP&r.",
            "Soul Siphon",
            "Gain Strength I and Regeneration I for &a5 &rseconds on kill.",
            "Dark Matter",
            "Every &a1 &riron ore will be auto-smelted when mined, dropping an iron ingot."
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

    private final Set<ArmorStand> ARMOR_STANDS = new HashSet<>();

    @Override
    public void ability(Player player) {
        Energy.clear(player);

        World world = player.getWorld();
        Location loc = player.getEyeLocation();

        world.playSound(loc, Sound.WITHER_SHOOT, 1, 1);

        Set<Vector> directions = new HashSet<>();
        Vector facing = loc.getDirection();

        directions.add(facing);
        directions.add(MathUtils.rotateAroundY(facing, 15));
        directions.add(MathUtils.rotateAroundY(facing, -15));

        for (Vector dir : directions) {
            Location ahead = loc.clone().add(dir.getX(), -1, dir.getZ());
            ArmorStand pt = (ArmorStand) world.spawnEntity(ahead, EntityType.ARMOR_STAND);

            ARMOR_STANDS.add(pt);

            pt.setVisible(false);
            pt.setGravity(false);
            pt.setSmall(true);
            pt.setHelmet(new ItemStack(Material.SKULL_ITEM, 1, (byte) 1));

            BukkitRunnable task = new BukkitRunnable() {

                @Override
                public void run() {
                    if (pt.isDead() || !ARMOR_STANDS.contains(pt)) {
                        this.cancel();
                        return;
                    }

                    Location pointLoc = pt.getEyeLocation();

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        ParticleUtils.play(EnumParticle.REDSTONE, pointLoc, 0.01, 0, 0, 1, 0);
                    }, 2);

                    for (Player target : Bukkit.getOnlinePlayers()) {
                        if (player == target) continue;
                        if (target.getWorld() != pointLoc.getWorld()) continue;

                        if (pointLoc.distance(target.getLocation().add(0, 1, 0)) < 1) {
                            witherSkullHit(player, pointLoc, target);
                            pt.remove();
                            this.cancel();
                            return;
                        }
                    }

                    if (pointLoc.getBlock().getType().isSolid()) {
                        witherSkullHit(player, pointLoc, null);
                        pt.remove();
                        this.cancel();
                    }

                    pt.teleport(pt.getLocation().clone().add(dir));
                }
            };

            task.runTaskTimer(plugin, 0, 1);
        }
    }

    private void witherSkullHit(Player player, Location loc, Player hit) {
        Set<Player> playersToDamage = new HashSet<>();
        if (hit != null) playersToDamage.add(hit);

        WorldUtils.createNoDamageExplosion(loc, 2);

        for (Player target : Bukkit.getOnlinePlayers()) {
            if (player == target) continue;
            if (playersToDamage.contains(target)) continue;
            if (target.getWorld() != loc.getWorld()) continue;

            if (loc.distance(target.getLocation()) < 1) {
                playersToDamage.add(target);
            }
        }

        for (Player target : playersToDamage) {
            MWHealth.trueDamage(target, 2.666667, player);
        }
    }

    @EventHandler
    public void cancelArmorStand(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof ArmorStand)) return;

        ArmorStand stand = (ArmorStand) event.getEntity();

        if (ARMOR_STANDS.contains(stand)) {
            event.setCancelled(true);
        }
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
            INCREMENT.put(player, (INCREMENT.get(player) + 1) % 5);
        }

        if (INCREMENT.get(player) == 4) {
            MWHealth.heal(player, 2);
            MWHealth.feed(player, 3);

            ParticleUtils.play(EnumParticle.HEART, player.getEyeLocation(), 0.5, 0.5, 0.5, 0.15, 1);
        }

        Energy.add(player, 10);
    }

    @EventHandler
    public void onKill(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player player = victim.getKiller();

        if (player == null || victim == player) return;
        if (!MWClassManager.isMW(player)) return;

        if (MWClassManager.get(player) == this) {
            PotionUtils.effect(player, "strength", 5);
            PotionUtils.effect(player, "regeneration", 5);
        }
    }

    @EventHandler
    public void gathering(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (MWClassManager.get(player) == this) {
            Block block = event.getBlock();

            if (block.getType() == Material.IRON_ORE) {
                block.setType(Material.AIR);

                Location loc = block.getLocation().add(0.5, 0.5, 0.5);
                World world = block.getWorld();

                Bukkit.getScheduler().runTaskLater(plugin, () -> {
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
            armorEnch.put(Enchantment.DURABILITY, 10);
            armorEnch.put(Enchantment.PROTECTION_FIRE, 1);
            armorEnch.put(Enchantment.PROTECTION_EXPLOSIONS, 2);

            ItemStack sword = MWItem.createSword(this, Material.DIAMOND_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack helmet = MWItem.createArmor(this, Material.DIAMOND_HELMET, armorEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 2, 8, 2);

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, helmet, null, null, null, null);
        }

        MWKit.assignItems(player, items);
    }
}
