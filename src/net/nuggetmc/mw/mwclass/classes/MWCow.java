package net.nuggetmc.mw.mwclass.classes;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.EntityItem;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.mwclass.items.MWItem;
import net.nuggetmc.mw.mwclass.items.MWKit;
import net.nuggetmc.mw.mwclass.items.MWPotions;
import net.nuggetmc.mw.utils.ActionBar;
import org.bukkit.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class MWCow extends MWClass {

    Map<Player,Integer> mine=new HashMap<>();
    final int cowBucketValue=60;
    private final Set<Player> willpowerList = new HashSet<>();
    int dmgcount=0;

    public MWCow() {
        this.name = new String[]{"牛","Cow","COW"};
        this.icon = Material.MILK_BUCKET;
        this.color = ChatColor.LIGHT_PURPLE;

        this.playstyles = new Playstyle[] {
            Playstyle.SUPPORT,
            Playstyle.TANK
        };

        this.diamonds = new Diamond[] {
            Diamond.CHESTPLATE
        };

        this.classInfo = new MWClassInfo(
            "Granting Moo",
            "Moo, granting Resistance"+ChatColor.GREEN+" II"+ChatColor.RESET+" and Regeneration "+ChatColor.GREEN+"II"+ChatColor.RESET+" to yourself",
            "Bucket Barrier",
            "Once below "+ChatColor.GREEN+"20 HP"+ChatColor.RESET+", a shield of milk buckets forms around you for 20 seconds,blocking the next 4 sources of damage by "+ChatColor.GREEN+"25%"+ChatColor.RESET+".Whenever damage gets blocked, you will get healed for "+ChatColor.GREEN+"2HP"+ChatColor.RESET,
            "Refreshing Sip",
            "Drinking any milk bucket grants nearby allies in a 7 block radius "+ChatColor.GREEN+"3 HP"+ChatColor.RESET+", replenishing both hunger and saturation",
            "Ultra Pasteurized",
            "You will receive 2 milk buckets for every "+ChatColor.GREEN+"80"+ChatColor.RESET+" Stone you mine.Milk buckets grant Resistance I and Regeneration II for 5 seconds and can be given to teammates"
        );

        this.classInfo.addEnergyGainType("Melee", 20);
        this.classInfo.addEnergyGainType("Bow", 20);
    }

    @Override
    public void ability(Player player) {
        energyManager.clear(player);
        player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) (2.5*20),1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (int) (2.5*20),1));
    }


    @Override
    public void hit(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        Player player = energyManager.validate(event);
        if (player == null) return;

        if (manager.get(player) != this) return;


        energyManager.add(player, 20);
    }
    @EventHandler
    public void onDamage(EntityDamageEvent e){
        if (e.isCancelled()) return;
        if (!(e.getEntity() instanceof Player )) return;
        Player victim= (Player) e.getEntity();
        if (manager.get(victim) != this) {
            return;
        }
        BucketBarrier(victim, victim.getHealth() - e.getDamage(),e);
    }
    @EventHandler
    public void onBucketGet(BlockBreakEvent e){

        Player player=e.getPlayer();
        if (manager.get(player) != this) {
            return;
        }
        if (e.getBlock().getType()!=Material.STONE) return;
        if (mine.get(player)<cowBucketValue){
            mine.replace(player,mine.get(player)+1);
            ActionBar.send(player,this.getColor()+"Ultra Pasteurized "+ChatColor.WHITE+mine.get(player)+"/"+cowBucketValue);
        } else if (mine.get(player)==cowBucketValue) {
            mine.replace(player,0);
            player.getInventory().addItem(plugin.getSpecialItemUtils().getCowBucket(2));
            ActionBar.send(player,this.getColor()+"Ultra Pasteurized "+ChatColor.GREEN+"✔");
        }
        //❤❥✔✖✗✘❂⋆✢✭✬✫✪✩✦✥✤✣✮✷➡➧⬅⬇➟➢➙➴➽▄▜▛➝▄⚔

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
            armorEnch.put(Enchantment.DURABILITY, 10);

            ItemStack sword = MWItem.createSword(this, Material.IRON_SWORD, swordEnch);
            ItemStack bow = MWItem.createBow(this, null);
            ItemStack tool = MWItem.createTool(this, Material.DIAMOND_PICKAXE);
            ItemStack chestplate = MWItem.createArmor(this, Material.DIAMOND_CHESTPLATE, armorEnch);

            List<ItemStack> potions = MWPotions.createBasic(this, 1, 10, 2);
            List<ItemStack> extra= new ArrayList<>();
            extra.add(plugin.getSpecialItemUtils().getCowOwnBucket(3));

            items = MWKit.generate(this, sword, bow, tool, null, null, potions, null, chestplate, null, null,extra );
        }

        MWKit.assignItems(player, items);
        if (mine.containsKey(player)){
            mine.replace(player,0);
        }else {
            mine.put(player,0);
        }
        dmgcount=0;
    }
    private void BucketBarrier(Player player, double health, EntityDamageEvent e) {
        if (health <= 20) {
            if (willpowerList.contains(player)) return;
            if (dmgcount==4) return;
            if (dmgcount==0) {
                player.sendMessage(this.getColor()+"You have activated the Bucket Barrier !");
            }

            int n=20;


            dmgcount++;

            e.setDamage(e.getDamage()*0.75);
            if (player.getHealth()>=player.getMaxHealth()-2) {
                player.setHealth(player.getMaxHealth());
            }else {
                player.setHealth(player.getHealth()+2);
            }
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                //Cool down finished
                willpowerList.remove(player);
                dmgcount=0;
            }, n * 20);
        }
    }
}
