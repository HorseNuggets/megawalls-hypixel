package net.nuggetmc.mw.mwclass.items;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.utils.ItemUtils;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class MWPotions {

    public static List<ItemStack> createBasic(MWClass mwclass, int hPotCount, int hPotAmount, int sPotCount) {
        List<ItemStack> items = new ArrayList<>();

        String name = mwclass.getName();
        ChatColor color = mwclass.getColor();

        items.add(createHealPotions(name, color, hPotCount, hPotAmount));
        items.add(createSpeedPotions(name, color, sPotCount));

        return items;
    }

    public static ItemStack createHealPotions(String name, ChatColor color, int count, int amount) {
        Potion potion = new Potion(PotionType.INSTANT_HEAL);
        ItemStack item = potion.toItemStack(count);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.setDisplayName(color + name + " Potion of Health");
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Instant Health (" + ChatColor.RED + amount + "❤" + ChatColor.GRAY + ")");

        meta.setLore(lore);
        item.setItemMeta(meta);

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();

        compound.setBoolean("megaWalls", true);
        compound.setInt("mwHeal", amount);

        nmsItem.setTag(compound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static ItemStack createSpeedPotions(String name, ChatColor color, int count) {
        Potion potion = new Potion(PotionType.SPEED);
        ItemStack item = potion.toItemStack(count);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1), true);
        meta.setDisplayName(color + name + " Potion of Speed II");

        item.setItemMeta(meta);

        return ItemUtils.toMWItem(item);
    }

    public static ItemStack createRegenerationPotions(String name, ChatColor color, int count, int duration, int healthDisplay) {
        Potion potion = new Potion(PotionType.REGEN);
        ItemStack item = potion.toItemStack(count);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.REGENERATION, duration * 20, 2), true);
        meta.setDisplayName(color + name + " Potion of Regeneration");
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        String time = "0:" + String.format("%02d", duration);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GRAY + "Regeneration (" + time + ") (" + ChatColor.RED + healthDisplay + "❤" + ChatColor.GRAY + ")");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return ItemUtils.toMWItem(item);
    }

    public static ItemStack createSlowSplash(String name, ChatColor color) {
        Potion potion = new Potion(PotionType.SLOWNESS);
        potion.setSplash(true);

        ItemStack item = potion.toItemStack(1);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.SLOW, 7 * 20, 0), true);
        meta.addCustomEffect(new PotionEffect(PotionEffectType.WEAKNESS, 7 * 20, 3), true);
        meta.setDisplayName(color + name + " Splash Potion of Slowness + Weakness");

        item.setItemMeta(meta);

        return ItemUtils.toMWItem(item);
    }
}
