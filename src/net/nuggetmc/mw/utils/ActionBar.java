package net.nuggetmc.mw.utils;

import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class ActionBar {

    public static void send(Player player, String message) {
        PacketPlayOutChat packet = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + message + "\"}"), (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }
}
