package net.nuggetmc.mw.energy;

import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.HashMap;
import java.util.Map;

public class Energy {

    private static final Map<Player, Integer> PLAYER_DATA = new HashMap<>();

    public static boolean isValid(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return false;
        if (!(event.getDamager() instanceof Player) && !(event.getDamager() instanceof Arrow)) return false;

        Player player;

        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Player) {
                player = (Player) arrow.getShooter();
            } else {
                return false;
            }

        } else {
            player = (Player) event.getDamager();
        }

        if (event.getDamage() == 0 || event.isCancelled()) return false;
        if (!MWClassManager.isMW(player)) return false;

        return true;
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
