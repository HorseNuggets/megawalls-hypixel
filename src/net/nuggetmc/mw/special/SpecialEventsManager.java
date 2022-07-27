package net.nuggetmc.mw.special;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.utils.SpecialItemUtils;
import net.nuggetmc.mw.utils.WorldUtils;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.math.BigDecimal;

import static net.nuggetmc.mw.MegaWalls.OPBYPASSGM;

public class SpecialEventsManager implements Listener {
    MegaWalls plugin;
    public SpecialEventsManager(){
        this.plugin=MegaWalls.getInstance();
    }

    ///////////////////////////COW BUCKET
    SpecialItemUtils specialItemUtils=new SpecialItemUtils();
    @EventHandler
    public void onCowBucket(PlayerItemConsumeEvent e){
        if (specialItemUtils.isCowBucket(e.getItem())){
            Player player = e.getPlayer();
            player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 5*20, 0));
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5*20, 1));
            player.setFoodLevel(20);
            player.setSaturation(20);
        }
    }
    ///////////////////////////ENDER CHEST
    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        if (plugin.getCombatManager().isInCombat(e.getPlayer())&&e.getBlock().getType()== Material.ENDER_CHEST){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        if (plugin.getCombatManager().isInCombat(e.getPlayer())&&e.getItemDrop().getItemStack().getType()==Material.ENDER_CHEST){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(!e.getAction().name().contains("RIGHT")) return;
        if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) return;
        if (p.getItemInHand().getType()!=Material.ENDER_CHEST) return;
        p.openInventory(p.getEnderChest());
    }
    ///////////////////////////PLAYER TRACK
    @EventHandler
    public void onMove(PlayerMoveEvent e){
        Player plr = e.getPlayer();
        Location plrLocation = plr.getLocation();
        if (plr.getWorld().getEnvironment() == World.Environment.NORMAL){
            //System.out.println(plr.getPlayerListName());
            String plrName = plr.getPlayerListName();
            double LowestDistance = Double.MAX_VALUE;
            Location LowestLocation = plr.getLocation();
            for (Player p : Bukkit.getOnlinePlayers()){
                if (!plugin.getCombatManager().isInCombat(p)){
                    continue;
                }
                if(!p.getPlayerListName().equals(plrName)) {
                    // this is not yourself
                    if (p.getWorld().getEnvironment() == World.Environment.NORMAL) {
                        Location pLocation = p.getLocation();


                        if (LowestDistance > plrLocation.distance(pLocation)) {
                            LowestDistance = plrLocation.distance(pLocation);
                            LowestLocation = pLocation;
                        }

                    }
                }
            }
            if (LowestLocation==null){
                return;
                //Todo : When right click on Compass tell the player that target is not found
            }
            // we now have the lowest location and distance
            plr.setCompassTarget(LowestLocation);


        }
    }
    ///////////////////////////EXPORB
    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.EXPERIENCE_ORB) {
            event.setCancelled(true);
        }
    }
    ///////////////////////////TELL ARROW DAMAGE
    @EventHandler
    public void onArrowDamageTell(EntityDamageByEntityEvent e){
        if (!(e.getEntity() instanceof Player)) return;
        if (!(e.getDamager() instanceof Arrow)) return;
        Arrow arrow=(Arrow) e.getDamager();
        Player victim=((Player) e.getEntity()).getPlayer();
        if (arrow.getShooter() instanceof Player) {
            Player player = (Player) arrow.getShooter();


            player.sendMessage(ChatColor.YELLOW+victim.getDisplayName()+ChatColor.RESET+" is on "+(new BigDecimal(victim.getHealth()).setScale(1,BigDecimal.ROUND_HALF_UP)).doubleValue()+" health!");
        }
    }
    ///////////////////////////NO DAMAGE BEFORE JOINING
    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if (e.getEntity() instanceof Player) {
            Player player= (Player) e.getEntity();
            if (!MegaWalls.getInstance().getCombatManager().isInCombat(player)){
                e.setCancelled(true);
            }
        }
    }
    ///////////////////////////RESPAWN
    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player=event.getPlayer();
        event.setRespawnLocation(WorldUtils.nearby(event.getRespawnLocation()));
        if (event.getPlayer().isOp() && OPBYPASSGM) {
            //
        } else {
            event.getPlayer().setGameMode(GameMode.ADVENTURE);
        }
        MegaWalls.getInstance().getCombatManager().removeInCombat(event.getPlayer());
        player.setPlayerListName(MegaWalls.getInstance().getCombatManager().isInCombat(player)?player.getDisplayName()+" ["+plugin.getClassManager().get(player).getShortName()+"]":player.getDisplayName());
    }
}
