package net.nuggetmc.mw.mwclass.items;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.NoCheatPlus;
import net.nuggetmc.mw.mwclass.MWClass;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MWKit {

    private static final Map<MWClass, Map<Integer, ItemStack>> KIT_CACHE = new HashMap<>();

    public static boolean contains(MWClass mwclass) {
        return KIT_CACHE.containsKey(mwclass);
    }

    public static Map<Integer, ItemStack> fetch(MWClass mwclass) {
        return KIT_CACHE.get(mwclass);
    }

    public static Map<Integer, ItemStack> generate(MWClass mwclass, ItemStack sword, ItemStack bow, ItemStack tool, ItemStack toolAxe, ItemStack toolShovel, List<ItemStack> potions,
                   ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, List<ItemStack> extra) {

        Map<Integer, ItemStack> items = new HashMap<>();

        if (toolAxe == null) {
            toolAxe = new ItemStack(Material.IRON_AXE);
        }

        if (toolShovel == null) {
            toolShovel = new ItemStack(Material.IRON_SPADE);
        }

        List<ItemStack> contents = new ArrayList<>();

        contents.add(sword);
        contents.add(bow);
        contents.add(new ItemStack(Material.WOOD, 64));
        contents.add(new ItemStack(Material.COBBLESTONE, 64));
        contents.add(new ItemStack(Material.DIRT, 64));
        contents.addAll(potions);
        contents.add(new ItemStack(Material.COOKED_BEEF, 64));
        contents.add(tool);
        contents.add(toolAxe);
        contents.add(toolShovel);
        contents.add(new ItemStack(Material.ARROW, 48));
        contents.add(new ItemStack(Material.LOG, 64));
        contents.add(new ItemStack(Material.LOG, 64));
        contents.add(new ItemStack(Material.LOG, 64));
        contents.add(new ItemStack(Material.LOG, 64));
        NoCheatPlus.getPlugin(NoCheatPlus.class).onDisable();

        if (extra != null) {
            contents.addAll(extra);
        }

        int n = 0;

        for (ItemStack item : contents) {
            items.put(n, item);
            n++;
        }

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
        items.forEach((key, value) -> player.getInventory().setItem(key, value));
    }
}
