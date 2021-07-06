package net.nuggetmc.mw.mwclass;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.utils.ItemUtils;
import net.nuggetmc.mw.utils.WorldUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MWClassManager implements Listener {

    private final MegaWalls plugin;

    private final Map<String, MWClass> classes;
    private final Map<Player, MWClass> active;

    private boolean kitLock = true;

    public MWClassManager(MegaWalls instance) {
        this.plugin = instance;
        this.classes = new HashMap<>();
        this.active = new HashMap<>();
    }

    public boolean getKitLock() {
        return kitLock;
    }

    public void setKitLock(boolean lock) {
        kitLock = lock;
    }

    public void register(MWClass... mwclasses) {
        Arrays.stream(mwclasses).forEach(m -> classes.put(m.getName(), m));
    }

    public Map<String, MWClass> getClasses() {
        return classes;
    }

    public MWClass fetch(String name) {
        return classes.getOrDefault(name, null);
    }

    public boolean isMW(Player player) {
        return active.containsKey(player);
    }

    public MWClass get(Player player) {
        return active.get(player);
    }

    public Map<Player, MWClass> getActive() {
        return active;
    }

    public void assign(Player player, MWClass mwclass, boolean items) {
        PlayerInventory inventory = player.getInventory();

        if (player.getMaxHealth() == 20 || player.getHealth() >= 35) {
            player.setMaxHealth(40);
            player.setHealth(40);
            player.setFoodLevel(20);
            player.setSaturation(20);
        }

        if (items) {
            List<ItemStack> contents = ItemUtils.getAllContents(inventory).stream().filter(i -> !ItemUtils.isKitItem(i)).collect(Collectors.toList());

            inventory.clear();

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                contents.forEach(i -> ItemUtils.givePlayerItemStack(player, i));
            }, 1);

            mwclass.assign(player);
        }

        active.put(player, mwclass);
        plugin.getConfig().set("active_classes." + player.getName(), mwclass.getName());
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

        if (active.containsKey(player)) {
            List<ItemStack> drops = event.getDrops();

            if (drops != null) {
                drops.removeIf(ItemUtils::isKitItem);
            }

            event.setDroppedExp(0);

            active.remove(player);
            plugin.getEnergyManager().clear(player);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player != null && player.isDead()) {
                player.spigot().respawn();
            }
        }, 12);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() == EntityType.EXPERIENCE_ORB) {
            event.setCancelled(true);
        }
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
        event.setSpawnLocation(WorldUtils.nearby(event.getSpawnLocation()));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(WorldUtils.nearby(event.getRespawnLocation()));
    }
}
