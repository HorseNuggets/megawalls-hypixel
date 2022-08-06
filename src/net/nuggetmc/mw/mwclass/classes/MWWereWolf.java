package net.nuggetmc.mw.mwclass.classes;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.ItemSword;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.ItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.logging.Level;

public class MWWereWolf extends MWClass {
    private Set<Player> steakregenInCDList =new HashSet<>();
    private Set<Player> abilityCDCathe =new HashSet<>();
    private Set<Player> BLhandlingList =new HashSet<>();
    private Set<Player> abilityhandlingList =new HashSet<>();
    private Map<Player,Integer> hitcountMap=new HashMap<>();
    private Map<Player,Integer> abilityhitcount=new HashMap<>();
    private Map<Player,Set<Player>> checkUniqueEntityMap =new HashMap<>();

    public MWWereWolf() {
        this.name = new String[]{"狼人","WereWolf","WER"};
        this.icon = Material.COOKED_BEEF;
        this.color = ChatColor.DARK_GREEN;

        this.playstyles = new Playstyle[] {
                Playstyle.MOBILITY,
                Playstyle.TANK
        };

        this.diamonds = new Diamond[] {
                Diamond.CHESTPLATE
        };

        this.classInfo = new MWClassInfo(
                "Lycanthropy",
                "You will gain Speed II for §a5§7 seconds. §7After your Speed effect is over, enemies in §7a 5 block radius around you will receive a burst §7of damage based on how many unique enemies you §7attacked with your sword\n§7The burst damage you will deal increases §7by §a0.5§7 per unique enemy\n§7This has a minimum of 1 damage, and a §7maximum of 5 damage\n§7You heal for 25% of the melee damage and §7burst damage dealt against enemies\n§7The burst damage has a healing cap of §7§a10 HP§7\nCooldown:8s",
                "Blood Lust",
                "§7Gain Speed I and Resistance I for §a6 §7seconds after landing any combination of 3 consecutive attacks without taking any damage." ,
                "Devour",
                "§7Every §a1§7 steaks eaten will give §7you Regeneration I§7 for §a5§7 seconds.\nCooldown:8s",
                "Carnivore"+""+ChatColor.RED+"unavailable",
                "§7Every §a1§7 player killed will drop §71§7 extra steak, and enemies that are final killed §7will drop §a4§7 extra steak"
        );

        this.classInfo.addEnergyGainType("Melee", 20);
        this.classInfo.addEnergyGainType("Bow", 20);
    }

    @Override
    public void ability(Player player) {
        if (abilityCDCathe.contains(player)) return;
        energyManager.clear(player);
        abilityCDCathe.add(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,5*20,1));
        abilityhandlingList.add(player);
        //start handling the hit count
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                Set<Player> targets=new HashSet<>();
                for (Player player1 : player.getWorld().getPlayers()){
                    if (!plugin.getCombatManager().isInCombat(player1)||player1.isDead()||player1.getGameMode()== GameMode.CREATIVE||(player1.getLocation().distance(player.getLocation())>5)||player1.equals(player)){
                        continue;
                    }else {
                        targets.add(player1);
                    }
                }
                //the speed effect is expired now
                abilityhandlingList.remove(player);
                //so we stop handling it now

                if (abilityhitcount.containsKey(player)){
                    double dmgamount=abilityhitcount.get(player)*0.5;
                    double healamount=0;
                    if (dmgamount<1) dmgamount=1;
                    if (dmgamount>5) dmgamount=5;
                    plugin.getServer().getLogger().log(Level.WARNING,"1");
                    if (targets.isEmpty()){
                        plugin.getServer().getLogger().log(Level.WARNING,"2");
                        //do not heal
                    }else {
                        plugin.getServer().getLogger().log(Level.WARNING,"3");
                        for (Player player1:targets){
                            mwhealth.trueDamage(player1,dmgamount,player);
                            healamount+=dmgamount;
                        }
                        plugin.getServer().getLogger().log(Level.WARNING,"4");
                        if (healamount>10) healamount=10;
                        double hpamount=player.getHealth()+healamount;
                        player.setHealth(Math.min(hpamount, player.getMaxHealth()));
                    }
                    plugin.getServer().getLogger().log(Level.WARNING,"5");
                    checkUniqueEntityMap.get(player).clear();
                    abilityhitcount.replace(player,0);
                }
            }
        },5*20);
        Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
            @Override
            public void run() {
                abilityCDCathe.remove(player);
            }
        },8*20);
    }

    @Override
    public void hit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Player player = energyManager.validate(event);
        if (!(event.getEntity() instanceof Player)) return;
        Player victim= (Player) event.getEntity();
        if (player == null) return;

        if (manager.get(player) != this) return;
        energyManager.add(player, 20);
        if (!ItemUtils.isSword(player.getInventory().getItemInHand().getType())) return;
        if (abilityhandlingList.contains(player)){
            //if handling:count the hit
            if (checkUniqueEntityMap.containsKey(player)){
                if (checkUniqueEntityMap.get(player).contains(victim)){
                    //if is not unique,return
                    return;
                }
            }
            //heal from this hit first
            double hpamount=player.getHealth()+event.getDamage()*0.25;
            player.setHealth(Math.min(hpamount, player.getMaxHealth()));
            //do unique record
            if (abilityhitcount.containsKey(player)){
                if (checkUniqueEntityMap.containsKey(player)){
                    checkUniqueEntityMap.get(player).add(victim);
                }else {
                    Set<Player> temp=new HashSet<>();
                    temp.add(victim);
                    checkUniqueEntityMap.put(player,temp);
                }
                abilityhitcount.replace(player, abilityhitcount.get(player)+1);
            }else {
                abilityhitcount.put(player,1);
                //do unique record.These codes are like shit.
                if (checkUniqueEntityMap.containsKey(player)){
                    checkUniqueEntityMap.get(player).add(victim);
                }else {
                    Set<Player> temp=new HashSet<>();
                    temp.add(victim);
                    checkUniqueEntityMap.put(player,temp);
                }
            }
        }

    }
    @EventHandler
    public void onRightClick(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(!e.getAction().name().contains("RIGHT")) return;
        if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) return;
        if (manager.get(p)!=this) return;
        if (p.getFoodLevel()==20){
            p.setFoodLevel(19);
        }
    }
    @EventHandler
    public void onOurDamage(EntityDamageEvent e) {
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player)) return;
        Player victim = (Player) e.getEntity();
        if (manager.get(victim) == this) {
            if (BLhandlingList.contains(victim)){
                BLhandlingList.remove(victim);
            }
            //You took damage!no chance for u to use blood last.
        }
    }
    @EventHandler
    public void handle(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) != this) return;
        if (BLhandlingList.contains(player)){
            if (hitcountMap.containsKey(player)){
                hitcountMap.put(player, hitcountMap.get(player)==null?1:hitcountMap.get(player)+1);
            }else {
                hitcountMap.put(player,0);
            }
        }
        if (hitcountMap.get(player)==3&&BLhandlingList.contains(player)){
            //if we are just handling the player,do blood last.
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED,6*20,0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE,6*20,0));
            BLhandlingList.remove(player);
            hitcountMap.replace(player,0);
            //stop handling and reset the hit count
            return;
        }


    }

    @EventHandler
    public void onSteakRegen(PlayerItemConsumeEvent e){

        Player player=e.getPlayer();
        if (manager.get(player) != this) {
            return;
        }
        if (steakregenInCDList.contains(player)){
            return;
        }
        if (e.getItem().getType()==Material.COOKED_BEEF){
            steakregenInCDList.add(player);
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION,5*20,0));
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    steakregenInCDList.remove(player);
                }
            },8*20);
        }
    }
    @EventHandler
    public void onFall(EntityDamageEvent e){
        if(!(e.getCause()== EntityDamageEvent.DamageCause.FALL)) return;
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player )) return;
        Player victim= (Player) e.getEntity();
        if (manager.get(victim) != this) {
            return;
        }
        //So we started handling blood last.
        BLhandlingList.add(victim);
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

            ItemStack sword = MWItem.createSword(this, Material.DIAMOND_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack chestplate = MWItem.createArmor(this, Material.DIAMOND_CHESTPLATE, armorEnch);

            List<ItemStack> potions = MWPotions.createWolfBasic(this, 1, 8, 3);

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, null, chestplate, null, null,null );
        }

        MWKit.assignItems(player, items);
        if (steakregenInCDList.contains(player)){
            steakregenInCDList.remove(player);
        }
        if (abilityCDCathe.contains(player)){
            abilityCDCathe.remove(player);
        }
        if (checkUniqueEntityMap.containsKey(player)){
            checkUniqueEntityMap.remove(player);
        }
    }



    //UNAVAILABLE
    //@EventHandler
    //public void onKill(PlayerDeathEvent e){
    //    Player player=energyManager.validate(e);
    //    if (player==null)return;
    //    if (manager.get(player)!=this) return;
    //    e.getDrops().add(new ItemStack(Material.COOKED_BEEF));
    //}
    /*
      through it is unavailable,I still did that.
     */


}
