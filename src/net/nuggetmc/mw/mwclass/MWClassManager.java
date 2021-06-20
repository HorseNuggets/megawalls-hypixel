package net.nuggetmc.mw.mwclass;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MWClassManager {

    private static Map<String, MWClass> classes = new HashMap<>();

    private static Map<Player, MWClass> active = new HashMap<>();

    public static void register(MWClass mwclass) {
        classes.put(mwclass.getName(), mwclass);
    }

    public static Map<String, MWClass> getClasses() {
        return classes;
    }

    public static MWClass fetch(String name) {
        if (classes.containsKey(name)) {
            return classes.get(name);
        }

        return null;
    }

    public static void assign(Player player, MWClass mwclass) {
        active.put(player, mwclass);
    }
}
