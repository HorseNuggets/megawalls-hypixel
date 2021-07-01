package net.nuggetmc.mw.utils;

import org.bukkit.util.Vector;

public class MathUtils {

    public static Vector rotateAroundY(Vector vector, double deg) {
        double x = vector.getX();
        double z = vector.getZ();

        double angle = Math.atan2(z, x) + Math.toRadians(deg);

        return new Vector(Math.cos(angle), vector.getY(), Math.sin(angle));
    }
}
