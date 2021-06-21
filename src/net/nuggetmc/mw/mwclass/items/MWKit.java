package net.nuggetmc.mw.mwclass.items;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.mwclass.MWClass;
import net.nuggetmc.mw.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MWKit {

    private static final Map<MWClass, Map<Integer, ItemStack>> KIT_CACHE = new HashMap<>();

    public static boolean contains(MWClass mwclass) {
        return KIT_CACHE.containsKey(mwclass);
    }

    public static Map<Integer, ItemStack> fetch(MWClass mwclass) {
        return KIT_CACHE.get(mwclass);
    }

    public static Map<Integer, ItemStack> generate(MWClass mwclass, ItemStack sword, ItemStack bow, ItemStack tool, int hPotCount, int hPotAmount, int sPotCount,
                   ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots) {

        Map<Integer, ItemStack> items = new HashMap<>();

        // Eventually have the numbers increment up for more flexibility

        items.put(0, sword);
        items.put(1, bow);

        items.put(2, new ItemStack(Material.WOOD, 64));
        items.put(3, new ItemStack(Material.COBBLESTONE, 64));
        items.put(4, new ItemStack(Material.DIRT, 64));

        items.put(5, createHealPotions(hPotCount, hPotAmount));
        items.put(6, createSpeedPotions(sPotCount));

        items.put(7, new ItemStack(Material.COOKED_BEEF, 64));

        items.put(8, tool);

        items.put(9, new ItemStack(Material.IRON_AXE));
        items.put(10, new ItemStack(Material.IRON_SPADE));

        items.put(11, new ItemStack(Material.ARROW, 64));

        if (helmet == null) helmet = new ItemStack(Material.IRON_HELMET);
        if (chestplate == null) chestplate = new ItemStack(Material.IRON_CHESTPLATE);
        if (leggings == null) leggings = new ItemStack(Material.IRON_LEGGINGS);
        if (boots == null) boots = new ItemStack(Material.IRON_BOOTS);

        items.put(36, boots);
        items.put(37, leggings);
        items.put(38, chestplate);
        items.put(39, helmet);

        KIT_CACHE.put(mwclass, items);

        return items;
    }

    public static void assignItems(Player player, Map<Integer, ItemStack> items) {
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            player.getInventory().setItem(entry.getKey(), entry.getValue());
        }
    }

    public static ItemStack createHealPotions(int count, int amount) {
        Potion potion = new Potion(PotionType.INSTANT_HEAL);
        ItemStack item = potion.toItemStack(count);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.setDisplayName(ChatColor.YELLOW + "Herobrine Potion of Health");
        meta.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);

        ArrayList<String> lore = new ArrayList<String>();
        lore.add(ChatColor.GRAY + "Instant Health (" + ChatColor.RED + amount + "❤" + ChatColor.GRAY + ")");

        meta.setLore(lore);
        item.setItemMeta(meta);

        return ItemUtils.toMWItem(item);
    }

    public static ItemStack createSpeedPotions(int count) {
        Potion potion = new Potion(PotionType.SPEED);
        ItemStack item = potion.toItemStack(count);
        PotionMeta meta = (PotionMeta) item.getItemMeta();

        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 15 * 20, 1), true);
        meta.setDisplayName(ChatColor.YELLOW + "Herobrine Potion of Speed II");

        item.setItemMeta(meta);

        return ItemUtils.toMWItem(item);
    }
}
