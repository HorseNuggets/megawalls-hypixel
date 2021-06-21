package net.nuggetmc.mw.utils;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

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
}
