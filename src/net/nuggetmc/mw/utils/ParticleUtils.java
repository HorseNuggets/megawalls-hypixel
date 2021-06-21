package net.nuggetmc.mw.utils;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ParticleUtils {

    public static void play(EnumParticle type, Location loc, double offsetX, double offsetY, double offsetZ, double speed, int count) {
        PacketPlayOutWorldParticles particles = new PacketPlayOutWorldParticles(
            type,
            true,
            (float) loc.getX(),
            (float) loc.getY(),
            (float) loc.getZ(),
            (float) offsetX,
            (float) offsetY,
            (float) offsetZ,
            (float) speed,
            count,
            (int[]) null
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(particles);
        }
    }
}
