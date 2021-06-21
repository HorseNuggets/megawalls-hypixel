package net.nuggetmc.mw.utils;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.energy.Energy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

public class MWHealth implements Listener {

    private MegaWalls plugin;

    public MWHealth(MegaWalls instance) {
        this.plugin = instance;
    }

    private void healthSetup(Player player) {
        Energy.clear(player);

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

    public static void trueDamage(Player player, double amount) {
        double health = player.getHealth();

        if (health >= amount + 0.01) {
            player.damage(0.01);
            player.setHealth(health - amount);
        } else {
            player.setHealth(0);
        }
    }
}
