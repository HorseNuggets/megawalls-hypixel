package net.nuggetmc.mw.mwclass;

import java.util.HashMap;
import java.util.Map;

public class MWClassManager {

    private static Map<String, MWClass> classes = new HashMap<>();

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
}
