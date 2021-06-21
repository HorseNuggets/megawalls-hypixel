package net.nuggetmc.mw.energy;

import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class Energy implements Listener {

    private static final Map<Player, Integer> PLAYER_DATA = new HashMap<>();

    public static Player validate(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return null;
        if (!(event.getDamager() instanceof Player) && !(event.getDamager() instanceof Arrow)) return null;

        Player player;

        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Player) {
                player = (Player) arrow.getShooter();
            } else {
                return null;
            }

        } else {
            player = (Player) event.getDamager();
        }

        if (((Player) event.getEntity()).getNoDamageTicks() >= 12) return null;
        if (event.getDamage() == 0 || event.isCancelled()) return null;

        if (MWClassManager.isMW(player)) {
            return player;
        }

        return null;
    }

    public static int get(Player player) {
        return PLAYER_DATA.get(player);
    }

    @EventHandler
    public void onAbility(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        String action = event.getAction().name();

        checkActions(player, action);
    }

    @EventHandler
    public void onAbility2(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        if (!(event.getDamager() instanceof Player)) return;
        if (event.getDamage() == 0 || event.isCancelled()) return;

        checkActions((Player) event.getDamager(), "LEFT_CLICK");
    }

    private void checkActions(Player player, String action) {
        ItemStack item = player.getInventory().getItemInHand();
        if (item == null) return;

        Material type = player.getInventory().getItemInHand().getType();

        if (type == Material.BOW && action.contains("LEFT_CLICK")) {
            callAbility(player);
        }

        if (type.name().contains("SWORD") && action.contains("RIGHT_CLICK")) {
            callAbility(player);
        }
    }

    private void callAbility(Player player) {
        if (!MWClassManager.isMW(player)) return;
        if (fetch(player) < 100) return;

        MWClassManager.get(player).ability(player);
    }

    public static int fetch(Player player) {
        return PLAYER_DATA.getOrDefault(player, 0);
    }

    public static void add(Player player, int amount) {
        if (!PLAYER_DATA.containsKey(player)) {
            set(player, amount);
            return;
        }

        int current = PLAYER_DATA.get(player);
        int updated = current + amount;

        if (updated > 100) {
            updated = 100;
        }

        set(player, updated);
    }

    public static void set(Player player, int amount) {
        if (amount != 0) {
            PLAYER_DATA.put(player, amount);
        }

        float bar = (float) (amount / 100.0);

        player.setLevel(amount);
        player.setExp(bar);
    }

    public static void clear(Player player) {
        if (PLAYER_DATA.containsKey(player)) {
            PLAYER_DATA.remove(player);
        }

        set(player, 0);
    }

    public static void flash() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(MegaWalls.getInstance(), () -> {
            for (Player player : PLAYER_DATA.keySet()) {
                int level = player.getLevel();
                float bar = player.getExp();

                if (level == 100 && bar == 1) {
                    player.setExp(0);
                } else if (level == 100 && bar == 0) {
                    player.setExp(1);
                }
            }
        }, 6, 6);
    }
}
