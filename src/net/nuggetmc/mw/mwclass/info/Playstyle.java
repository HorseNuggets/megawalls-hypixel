package net.nuggetmc.mw.mwclass.info;

import net.md_5.bungee.api.ChatColor;
import org.apache.commons.lang3.StringUtils;

public enum Playstyle {
    CONTROL,
    FIGHTER,
    MOBILITY,
    RANGED,
    TANK,
    SUPPORT,
    RUSHER,
    DAMAGE;

    public static String display(Playstyle style) {
        String name = StringUtils.capitalize(style.name().toLowerCase());
        ChatColor color;

        switch (style) {
            case CONTROL:
                color = ChatColor.GOLD;
                break;

            case FIGHTER:
                color = ChatColor.DARK_GREEN;
                break;

            case MOBILITY:
                color = ChatColor.AQUA;
                break;

            case RANGED:
                color = ChatColor.DARK_AQUA;
                break;

            case TANK:
                color = ChatColor.BLUE;
                break;

            case SUPPORT:
                color = ChatColor.LIGHT_PURPLE;
                break;

            case RUSHER:
                color = ChatColor.DARK_RED;
                break;

            case DAMAGE:
                color = ChatColor.RED;
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + style);
        }

        return color + name;
    }
}
