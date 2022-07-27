package net.nuggetmc.mw.mwclass;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.energy.EnergyManager;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.utils.MWHealth;
import net.nuggetmc.mw.utils.SpecialItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;

public abstract class MWClass implements Listener {

    protected final MegaWalls plugin;
    protected final MWClassManager manager;
    protected final MWHealth mwhealth;
    protected final EnergyManager energyManager;

    protected String[] name;
    protected Material icon;
    protected ChatColor color;
    protected Playstyle[] playstyles;
    protected Diamond[] diamonds;
    protected MWClassInfo classInfo;

    public MWClass() {
        this.plugin = MegaWalls.getInstance();
        this.manager = plugin.getClassManager();
        this.mwhealth = plugin.getMWHealth();
        this.energyManager = plugin.getEnergyManager();
    }

    public String getName() {
        return plugin.isChinese()?name[0]:name[1];
    }

    protected Set<Player> inRange(Player player, double radius) {
        World world = player.getWorld();
        Location locUp = player.getEyeLocation();
        Set<Player> result = new HashSet<>();

        for (Player victim : Bukkit.getOnlinePlayers()) {
            if (world != victim.getWorld()) continue;

            Location loc = victim.getEyeLocation();

            if (locUp.distance(loc) <= radius && player != victim && !victim.isDead()) {
                result.add(victim);
            }
        }

        return result;
    }
    /*@EventHandler
    public void onBow(EntityDamageByEntityEvent e){
        if (e.getDamager()instanceof Arrow){
            if (e.getEntity() instanceof Player){
                ((Player) e.getEntity()).getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 1*20, 0));
            }
        }
    }*/
    @EventHandler
    public void onInteract(PlayerInteractEvent e){
        Player p = e.getPlayer();
        if(!e.getAction().name().contains("RIGHT")) return;
        if(p.getItemInHand() == null || p.getItemInHand().getType() == Material.AIR) return;
        if (p.getItemInHand().getType()!=Material.ENDER_CHEST) return;
        p.openInventory(p.getEnderChest());
    }
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
    @EventHandler
    public void onPlace(BlockPlaceEvent e){
        if (plugin.getCombatManager().isInCombat(e.getPlayer())&&e.getBlock().getType()==Material.ENDER_CHEST){
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onDrop(PlayerDropItemEvent e){
        if (plugin.getCombatManager().isInCombat(e.getPlayer())&&e.getItemDrop().getItemStack().getType()==Material.ENDER_CHEST){
            e.setCancelled(true);
        }
    }
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
    @EventHandler
    public void onHit(EntityDamageByEntityEvent e){
        this.hit(e);
    }

    public Material getIcon() {
        return icon;
    }

    public ChatColor getColor() {
        return color;
    }

    public Playstyle[] getPlaystyles() {
        return playstyles;
    }

    public Diamond[] getDiamonds() {
        return diamonds;
    }

    public MWClassInfo getInfo() {
        return classInfo;
    }
    public String getShortName(){
        return plugin.isChinese()?name[0] :name[2];
    }

    public abstract void ability(Player player);

    public abstract void assign(Player player);
    public abstract void hit(EntityDamageByEntityEvent e);
}
