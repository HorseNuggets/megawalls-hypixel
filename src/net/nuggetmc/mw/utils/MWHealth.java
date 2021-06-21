package net.nuggetmc.mw.utils;

import org.bukkit.entity.Player;

public class MWHealth {

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
