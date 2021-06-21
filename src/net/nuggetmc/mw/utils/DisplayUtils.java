package net.nuggetmc.mw.utils;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class DisplayUtils {

    // Automatically bullets and indents
    public static List<String> fit(String string) {
        if (string.contains("\n")) {
            String[] split = string.split("\n");
            List<String> lines = new ArrayList<>();

            for (int i = 0; i < split.length; i++) {
                lines.addAll(fit(split[i]));
            }

            return lines;
        }

        String msg = string.replace("&r", "&7");
        List<String> msgSplit = new ArrayList<>();

        int index = 0;
        int max = 45;

        while (index < msg.length()) {
            String line = msg.substring(index, Math.min(index + max, msg.length()));
            int increment = line.length();

            if (line.length() == max && line.contains(" ")) {
                line = line.substring(0, line.lastIndexOf(" "));
                increment = line.length() + 1;
            }

            if (line.length() > 1 && line.startsWith(" ")) {
                line = line.substring(1);
            }

            msgSplit.add((index != 0 ? "   " : ChatColor.DARK_GRAY + " ▪ ") + ChatColor.GRAY + ChatColor.translateAlternateColorCodes('&', line));
            index += increment;
        }

        return msgSplit;
    }
}
