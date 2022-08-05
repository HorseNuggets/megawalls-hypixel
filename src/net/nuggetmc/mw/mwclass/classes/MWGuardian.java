package net.nuggetmc.mw.mwclass.classes;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.Blocks;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.ActionBar;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class MWGuardian extends MWClass {
    Set<Player> extrimityList=new HashSet<>();
    Set<Player> suckList=new HashSet<>();
    Set<Player> waterList=new HashSet<>();



    public MWGuardian() {
        this.name = new String[]{"守卫者","Guardian","GUA"};
        this.icon = Material.MOB_SPAWNER;
        this.color = ChatColor.BLUE;

        this.playstyles = new Playstyle[] {
                Playstyle.CONTROL,
                Playstyle.DAMAGE
        };

        this.diamonds = new Diamond[] {
                Diamond.BOOTS
        };

        this.classInfo = new MWClassInfo(
                "Curse laser",
                "Shoot a laser to the closest player in a &a10 &rblocks radius,\n dealing &a5&r true damage , gaining Speed &aII&r for &a5 &rseconds.",
                "Extremity",
                "Once you are below 20 HP,every hit will heal you &a2.5&r HP by 50% chance in &a8&r seconds,you gain &a3&r seconds of resistance &aII&r.\nIf that damage cause you to be dead,it will be cancelled.\nCooldown: &a15 &rseconds.",
                "Ruins guardian",
                "If you are in water, you will deal &a+75%&r damage,gaining regeneration &aI&r for the next &a3&r seconds.\nCooldown:&a30&r seconds.",
                "Stupid dev",
                "There is no gathering talent because this kit is made for mwffa."
        );

        this.classInfo.addEnergyGainType("Melee", 15);
        this.classInfo.addEnergyGainType("Bow", 15);
    }
    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player )) return;
        Player victim= (Player) e.getEntity();
        if (manager.get(victim) != this) {
            return;
        }
        if (extrimityList.contains(victim)){
            return;
        }
        if (!(victim.getHealth()-e.getDamage()<=20)) return;
        if ((victim.getHealth()-e.getDamage()<=0)) e.setCancelled(true);
        extrimityList.add(victim);
        suckList.add(victim);
        victim.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,3*20,1));
        victim.sendMessage(this.getColor()+"You have activated extremity!");
        new Thread(() -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
            //Cool down finished
            extrimityList.remove(victim);
        }, 15 * 20)).start();
        new Thread(() -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
            //Cool down finished
            suckList.remove(victim);
        }, 8 * 20)).start();

    }
    @EventHandler
    public void onSuck(EntityDamageByEntityEvent e){
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player )) return;
        Player player= energyManager.validate(e);
        if (player==null) return;
        if (manager.get(player) != this) {
            return;
        }
        if (!suckList.contains(player)){
            return;
        }
        if(new Random().nextBoolean()) return;
        //50% chance
        double finalhealth=player.getHealth()+2.6;
        if (finalhealth>=player.getMaxHealth()){
            player.setHealth(player.getMaxHealth());
        }else {
            player.setHealth(finalhealth);
        }


    }
    @EventHandler
    public void onMove(PlayerMoveEvent e){
        if (manager.get(e.getPlayer()) != this) return;
        if (waterList.contains(e.getPlayer())) return;
        if (e.getPlayer().getLocation().getBlock().getType()== Material.WATER||e.getPlayer().getLocation().getBlock().getType()== Material.STATIONARY_WATER){
            waterList.add(e.getPlayer());
            e.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,3*20,0));
            e.getPlayer().sendMessage("You have activated Ruins guardian!");
            new Thread(() -> Bukkit.getScheduler().runTaskLater(plugin, () -> {
                //Cool down finished
                waterList.remove(e.getPlayer());
            }, 30 * 20)).start();
        }

    }
    @EventHandler
    public void onMultiply(EntityDamageByEntityEvent e){
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player )) return;
        Player player= energyManager.validate(e);
        if (player==null) return;
        if (manager.get(player) != this) {
            return;
        }
        if (!waterList.contains(player)) return;
        e.setDamage(e.getDamage()*1.75);
    }


    @Override
    public void ability(Player player) {
        Set<Player> targets=new HashSet<>();
        for (Player player1:Bukkit.getOnlinePlayers()){
            if (!plugin.getCombatManager().isInCombat(player1)||player1.isDead()||player1.getGameMode()==GameMode.CREATIVE||(player1.getLocation().distance(player.getLocation())>15)||player1.equals(player)){
                continue;
            }else {
                targets.add(player1);
            }
        }
        if (targets.isEmpty()){
            ActionBar.send(player, "No players within " + ChatColor.RED + 15 + ChatColor.RESET + " blocks!");
            return;
        }else {
            energyManager.clear(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,5*20,1));
            ArrayList<Player> arrayList=new ArrayList<>(targets.size());
            for (int i=0;i<targets.size();i++){
                arrayList.add((Player) targets.toArray()[i]);
            }
            arrayList.sort(new Comparator<Player>() {
                @Override
                public int compare(Player player1, Player t1) {
                    if (player.getEyeLocation().distance(player1.getLocation())>(player.getEyeLocation().distance(t1.getLocation()))){
                        return 1;
                    }else {
                        return -1;
                    }
                }
            });
            mwhealth.trueDamage(arrayList.get(0),5d,player);
        }
    }






    @Override
    public void hit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) != this) return;



        energyManager.add(player, 15);

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
            armorEnch.put(Enchantment.DEPTH_STRIDER, 5);
            armorEnch.put(Enchantment.DURABILITY, 10);

            ItemStack sword = MWItem.createSword(this, Material.IRON_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack boots = MWItem.createArmor(this, Material.DIAMOND_BOOTS, armorEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 1, 8, 2);

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, null, null, null, boots, null);
        }

        MWKit.assignItems(player, items);
        if (extrimityList.contains(player)){
            extrimityList.remove(player);
        }
        if (suckList.contains(player)){
            suckList.remove(player);
        }
        if (waterList.contains(player)){
            waterList.remove(player);
        }
    }
}
