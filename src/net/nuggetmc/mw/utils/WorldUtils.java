package net.nuggetmc.mw.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WorldUtils implements Listener {

    private static Random RANDOM = new Random();
    private static Set<TNTPrimed> DETONATE_LIST = new HashSet<>();

    public static Location nearby(Player player) {
        int top = 150;

        Location loc = player.getLocation();
        loc.setY(top);
        loc.add(RANDOM.nextInt(60) - 30, 0, RANDOM.nextInt(60) - 30);

        for (int y = top; y >= 1; y--) {
            loc.setY(y);

            if (loc.getBlock().getType().isSolid()) {
                break;
            }
        }

        return loc.add(0, 2, 0);
    }

    // Eventually make this not spawn in a TNTPrimed, but just break
    public static void createNoDamageExplosion(Location loc, int strength) {
        TNTPrimed tnt = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);
        tnt.setFuseTicks(0);
        tnt.setYield(2.0F);

        DETONATE_LIST.add(tnt);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void explosionCancel(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof TNTPrimed)) return;

        TNTPrimed tnt = (TNTPrimed) event.getDamager();

        if (DETONATE_LIST.contains(tnt)) {
            DETONATE_LIST.remove(tnt);
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void blockList(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof TNTPrimed)) return;

        TNTPrimed tnt = (TNTPrimed) event.getEntity();

        if (DETONATE_LIST.contains(tnt)) {
            List<Block> blockList = event.blockList();

            if (blockList.size() > 0) {
                event.blockList().clear();

                for (Block block : blockList) {
                    block.breakNaturally();
                }
            }
        }
    }
}
