package net.nuggetmc.mw.utils;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ItemUtils {

    public static boolean isKitItem(ItemStack item) {
        if (item == null) return false;

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem == null) return false;

        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        return compound.getBoolean("megaWalls");
    }

    public static ItemStack toMWItem(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        compound.setBoolean("megaWalls", true);
        nmsItem.setTag(compound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static void givePlayerItemStack(Player player, ItemStack item) {
        player.getWorld().dropItem(player.getEyeLocation(), item).setPickupDelay(0);
    }

    public static List<ItemStack> getAllContents(PlayerInventory inv) {
        List<ItemStack> contents = new ArrayList<>(Arrays.asList(inv.getContents()));
        contents.addAll(Arrays.asList(inv.getArmorContents()));
        return contents.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }
}
