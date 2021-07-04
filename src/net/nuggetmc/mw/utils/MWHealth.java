package net.nuggetmc.mw.utils;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.energy.EnergyManager;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class MWHealth implements Listener {

    private final MegaWalls plugin;
    private final MWClassManager manager;
    private final EnergyManager energyManager;

    public MWHealth() {
        this.plugin = MegaWalls.getInstance();
        this.manager = plugin.getManager();
        this.energyManager = plugin.getEnergyManager();
    }

    private void healthSetup(Player player) {
        energyManager.clear(player);

        final Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();

        if (board.getObjective("health") != null) {
            board.getObjective("health").unregister();
        }

        final Objective obj = board.registerNewObjective("hp", "health");
        obj.setDisplayName(ChatColor.RED + "HP");
        obj.setDisplaySlot(DisplaySlot.BELOW_NAME);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            for (Player others : Bukkit.getOnlinePlayers()) {
                Score health = obj.getScore(others.getName());

                if (health.getScore() == 0) {
                    health.setScore((int) others.getHealth());
                }
            }
        }, 1);

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> player.setScoreboard(board), 2);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        healthSetup(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(final PlayerRespawnEvent event) {
        healthSetup(event.getPlayer());

        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            healthSetup(event.getPlayer());
        }, 4);
    }

    @EventHandler
    public void potion(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem == null) return;

        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        int value = compound.getInt("mwHeal");

        if (value == 0) return;

        Player player = event.getPlayer();

        int amount = value * 2 - 4;

        heal(player, amount);
    }

    public void heal(Player player, double amount) {
        double health = player.getHealth();

        if (health < 40 - amount) {
            player.setHealth(health + amount);
        } else {
            player.setHealth(40);
        }
    }

    public void feed(Player player, int amount) {
        int food = player.getFoodLevel();

        if (food < 20 - amount) {
            player.setFoodLevel(food + amount);
        } else {
            player.setFoodLevel(20);
        }
    }

    public void trueDamage(Player player, double amount, Player damager) {
        double health = player.getHealth();

        if (manager.isMW(player) && manager.get(player).getName().equals("Golem")) {
            amount *= 0.8;
            player.getWorld().playSound(player.getLocation(), Sound.ZOMBIE_METAL, 1, 1);
        }

        if (health >= amount + 0.01) {
            player.damage(0.01, damager);
            player.setHealth(health - amount);
        } else {
            player.damage(250, damager);
        }
    }
}
