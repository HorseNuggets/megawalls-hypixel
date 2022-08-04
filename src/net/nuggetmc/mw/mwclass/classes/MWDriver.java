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
import net.nuggetmc.mw.utils.ActionBar;
import net.nuggetmc.mw.utils.ParticleUtils;
import net.nuggetmc.mw.utils.PotionUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.*;
import java.util.stream.Collectors;

public class MWDriver extends MWClass {

    private Set<Player> runnerList=new HashSet<>();
    private final Map<Player, Boolean> rideothers = new HashMap<>();


    public MWDriver() {
        this.name = new String[]{"司机","Driver","DRI"};
        this.icon = Material.IRON_FENCE;
        this.color = ChatColor.BLACK;

        this.playstyles = new Playstyle[] {
                Playstyle.RUSHER,
                Playstyle.FIGHTER
        };

        this.diamonds = new Diamond[] {
                Diamond.LEGGINGS
        };

        this.classInfo = new MWClassInfo(
                "Ride",
                "Ride the closest player or make the closest player ride you\n when they are in a &a15 &rblocks radius, gaining Strength I,resistance I and jump_boost II for &a5 &rseconds,\ngiving the player 1 second of slowness in the max(255) level,\nright click your bow to switch between the two modes.",
                "L Runner",
                "Once you are below 7 HP,you get 6 seconds of Absorption XX(20) , speed III and jump boost III for 12 seconds.\nIf that damage cause you to be dead,it will be cancelled.\nCooldown: &a50 &rseconds.",
                "Solo handjob god",
                "Once you were shoot by a player,you automatically throw 10 snowball to where you face,gaining Absorption I for 2 seconds",
                "Stupid dev",
                "There is no gathering talent because this kit is made for mwffa."
        );

        this.classInfo.addEnergyGainType("Melee", 10);
        this.classInfo.addEnergyGainType("Bow", 10);
    }
    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player )) return;
        Player victim= (Player) e.getEntity();
        if (manager.get(victim) != this) {
            return;
        }
        if (runnerList.contains(victim)){
            return;
        }
        if (!(victim.getHealth()-e.getDamage()<=7)) return;
        if ((victim.getHealth()-e.getDamage()<=0)) e.setCancelled(true);
        runnerList.add(victim);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,12*20,2));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.JUMP,12*20,2));
        victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,6*20,19));
        victim.sendMessage(this.getColor()+"You have became super runner! L runner");
        new Thread(() -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
            //Cool down finished
            runnerList.remove(victim);
        }, 50 * 20)).start();
    }
    @EventHandler
    public void onArrowDMG(EntityDamageByEntityEvent e){
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player )) return;
        Player victim= (Player) e.getEntity();
        if (manager.get(victim) != this) {
            return;
        }
      //  if (victim== energyManager.validate(e)) return;
        if (!(e.getDamager() instanceof Arrow)) return;
        if (!(((Arrow) e.getDamager()).getShooter() instanceof Player)) return;
        int i=0;
        while (i<10){
            victim.throwSnowball();
            i++;
        }
        victim.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION,2*20,0));

    }


    @Override
    public void ability(Player player) {
            Player target=null;
            for (Player player1 : player.getWorld().getPlayers()){
                if (!plugin.getCombatManager().isInCombat(player1)||player1.isDead()||player1.getGameMode()==GameMode.CREATIVE||(player1.getLocation().distance(player.getLocation())>15)||player1.equals(player)){
                    continue;
                }else {
                    target=player1;
                    break;
                }
            }
        if (target == null) {
            ActionBar.send(player, "No players within " + ChatColor.RED + 15 + ChatColor.RESET + " blocks!");
            return;
        }else {
            energyManager.clear(player);
            if (rideothers.get(player)){
                target.setPassenger(player);
            }else {
                player.setPassenger(target);
            }
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20,254));
        }
    }






    @Override
    public void hit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) != this) return;



        energyManager.add(player, 10);

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
            swordEnch.put(Enchantment.DAMAGE_UNDEAD,1);

            Map<Enchantment, Integer> armorEnch = new HashMap<>();
            armorEnch.put(Enchantment.PROTECTION_ENVIRONMENTAL, 2);
            armorEnch.put(Enchantment.DURABILITY, 10);

            ItemStack sword = MWItem.createSword(this, Material.IRON_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack leggings = MWItem.createArmor(this, Material.DIAMOND_LEGGINGS, armorEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 2, 7, 2);

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, null, null, leggings, null, null);
        }

        MWKit.assignItems(player, items);
        if (!rideothers.containsKey(player)){
            rideothers.put(player,true);
        }
        if (runnerList.contains(player)){
            runnerList.remove(player);
        }
    }
}
