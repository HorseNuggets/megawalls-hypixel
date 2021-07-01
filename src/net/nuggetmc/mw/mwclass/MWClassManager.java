package net.nuggetmc.mw.mwclass;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.classes.MWEnderman;
import net.nuggetmc.mw.utils.ItemUtils;
import net.nuggetmc.mw.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MWClassManager implements Listener {

    private MegaWalls plugin;

    public MWClassManager(MegaWalls instance) {
        this.plugin = instance;
    }

    private static Map<String, MWClass> classes = new HashMap<>();
    private static Map<Player, MWClass> active = new HashMap<>();

    private static boolean kitLock = true;

    public static boolean getKitLock() {
        return kitLock;
    }

    public static void setKitLock(boolean lock) {
        kitLock = lock;
    }

    public static void register(MWClass... mwclasses) {
        Arrays.stream(mwclasses).forEach(m -> classes.put(m.getName(), m));
    }

    public static Map<String, MWClass> getClasses() {
        return classes;
    }

    public static MWClass fetch(String name) {
        return classes.getOrDefault(name, null);
    }

    public static boolean isMW(Player player) {
        return active.containsKey(player);
    }

    public static MWClass get(Player player) {
        return active.get(player);
    }

    public static Map<Player, MWClass> getActive() {
        return active;
    }

    public void assign(Player player, MWClass mwclass) {
        player.getInventory().clear();

        if (player.getMaxHealth() == 20 || player.getHealth() >= 35) {
            player.setMaxHealth(40);
            player.setHealth(40);
            player.setFoodLevel(20);
            player.setSaturation(20);
        }

        mwclass.assign(player);

        active.put(player, mwclass);

        plugin.getConfig().set(player.getName(), mwclass.getName());
        plugin.saveConfig();
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        check(event, event.getPlayer(), event.getItemDrop().getItemStack());
    }

    @EventHandler
    public void onPickUp(PlayerPickupItemEvent event) {
        check(event, event.getPlayer(), event.getItem().getItemStack());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        MWClass mwclass = active.get(player);

        boolean listed = active.containsKey(player);

        if (!(listed && mwclass instanceof MWEnderman && ((MWEnderman) mwclass).isKeepInventory(player))) {
            if (listed) {
                active.remove(player);
            }

            Energy.clear(player);

            event.setDroppedExp(0);
            event.getDrops().removeIf(ItemUtils::isKitItem);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player != null && player.isDead()) {
                player.spigot().respawn();
            }
        }, 12);
    }

    @EventHandler
    public void onItemTransfer(InventoryClickEvent event) {
        InventoryType type = event.getInventory().getType();

        if (type == InventoryType.PLAYER || type == InventoryType.CRAFTING) {
            return;
        }

        check(event, (Player) event.getWhoClicked(), event.getCurrentItem());
    }

    private void check(Cancellable event, Player player, ItemStack item) {
        if (ItemUtils.isKitItem(item) && player.getGameMode() == GameMode.SURVIVAL && kitLock) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                player.sendMessage(ChatColor.GREEN + "Do " + ChatColor.YELLOW + "/mw" + ChatColor.GREEN + " to select a class!");
            }
        }, 10);
    }

    @EventHandler
    public void onPreJoin(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(WorldUtils.nearby(event.getPlayer()));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(WorldUtils.nearby(event.getPlayer()));
    }
}
