package net.nuggetmc.mw.energy;

import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class EnergyManager implements Listener {

    private final MegaWalls plugin;

    private final MWClassManager manager;
    private final Map<Player, Integer> playerData = new HashMap<>();

    public EnergyManager() {
        this.plugin = MegaWalls.getInstance();
        this.manager = plugin.getManager();

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 20, 20);
    }

    public void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            MWClass mwclass = manager.get(player);

            if (mwclass == null) continue;

            switch (mwclass.getName()) {
                default:
                    break;

                case "Spider":
                case "蜘蛛":
                    add(player, 4);
                    break;
            }
        }
    }

    public Player validate(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player)) return null;
        if (!(event.getDamager() instanceof Player) && !(event.getDamager() instanceof Arrow)) return null;

        Player player;

        if (event.getDamager() instanceof Arrow) {
            Arrow arrow = (Arrow) event.getDamager();

            if (arrow.getShooter() instanceof Player) {
                player = (Player) arrow.getShooter();

                if (player == event.getEntity()) return null;
            }

            else {
                return null;
            }

        } else {
            player = (Player) event.getDamager();
        }

        if (((Player) event.getEntity()).getNoDamageTicks() >= 12) return null;
        if (event.getDamage() == 0 || event.isCancelled()) return null;

        if (manager.isMW(player)) {
            return player;
        }

        return null;
    }

    @EventHandler
    public void onExpSpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof ExperienceOrb) {
            event.setCancelled(true);
        }
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
        if (!manager.isMW(player)) return;
        if (fetch(player) < 100) return;

        manager.get(player).ability(player);
    }

    public int fetch(Player player) {
        return playerData.getOrDefault(player, 0);
    }

    public void add(Player player, int amount) {
        if (!playerData.containsKey(player)) {
            set(player, amount);
            return;
        }

        int current = playerData.get(player);
        int updated = current + amount;

        if (updated > 100) {
            updated = 100;
        }

        set(player, updated);
    }

    public void set(Player player, int amount) {
        playerData.put(player, amount);

        plugin.getConfig().set("energy." + player.getName(), amount);
        plugin.saveConfig();

        float bar = (float) (amount / 100.0);

        player.setLevel(amount);
        player.setExp(bar);
    }

    public int get(Player player) {
        if (playerData.containsKey(player)) {
            return playerData.get(player);
        }

        return 0;
    }

    public void clear(Player player) {
        set(player, 0);
    }

    public void flash() {
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            playerData.keySet().forEach(p -> {
                int level = p.getLevel();
                float bar = p.getExp();

                if (level == 100 && bar == 1) {
                    p.setExp(0);
                } else if (level == 100 && bar == 0) {
                    p.setExp(1);
                }
            });
        }, 6, 6);
    }
}
