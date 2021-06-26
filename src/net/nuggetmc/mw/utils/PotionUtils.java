package net.nuggetmc.mw.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PotionUtils {

    /*
     * The reason why I am applying effects via command and not via player.addPotionEffect() is because the command version already has built-in features
     * to compensate for players who already have the effect (for certain time invervals), and would adjust the time as necessary. In the future, I will make my own
     * that can do just that, and maybe even ADD to the current effect time ;)
     */

    public static void effect(Player player, String type, int time) {
        effect(player, type, time, 0);
    }

    public static void effect(Player player, String type, int time, int amplifier) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "effect " + player.getName() + " " + type + " " + time + " " + amplifier);
    }
}
