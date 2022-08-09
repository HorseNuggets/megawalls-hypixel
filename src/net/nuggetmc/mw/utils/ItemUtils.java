package net.nuggetmc.mw.utils;

import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.nuggetmc.mw.MegaWalls;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.material.EnderChest;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.stream.Collectors;

public class ItemUtils {

    public static void tickMWItems() {
        BukkitRunnable task = new BukkitRunnable() {

            @Override
            public void run() {
                checkMWItems();
            }
        };

        task.runTaskTimerAsynchronously(MegaWalls.getInstance(), 20, 20 * 3);
    }

    private static void checkMWItems() {
        Set<Entity> items = new HashSet<>();

        Bukkit.getWorlds().forEach(w -> w.getEntities().forEach(e -> {
            if (e instanceof Item) {
                if (isKitItem(((Item) e).getItemStack())||isDiamondItem(((Item) e).getItemStack())) {
                    items.add(e);
                }
            }
        }));

      //  Bukkit.broadcastMessage(items.toString());

        if (!items.isEmpty()) {
            Bukkit.getScheduler().runTask(MegaWalls.getInstance(), () -> items.forEach(Entity::remove));
        }
    }

    public static boolean isKitItem(ItemStack item) {
        if (item == null) return false;
        if (item.getType() == Material.ENDER_CHEST) return true;
        if (item.getType()==Material.COMPASS) return true;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem == null) return false;

        NBTTagCompound compound = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        if (compound.getBoolean("cowown")) return true;
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
        return contents.stream().filter(i -> i != null && i.getType() != Material.AIR).collect(Collectors.toList());
    }
    public static boolean isDiamondItem(ItemStack itemStack){

        if (itemStack == null) return false;

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        if (nmsItem == null) return false;
        Material material=itemStack.getType();

        ArrayList<Material> diamonditem=new ArrayList<>();
        diamonditem.add(Material.DIAMOND_HELMET);
        diamonditem.add(Material.DIAMOND_CHESTPLATE);
        diamonditem.add(Material.DIAMOND_LEGGINGS);
        diamonditem.add(Material.DIAMOND_BOOTS);
        for (Material material1:diamonditem){
            if (material1==material){
                return true;
            }
        }
        return false;
    }
    public static int findItemSlot(Player player,ItemStack thing) {
        try {
            for(int i=0;i<36;i++) {
                ItemStack stack = player.getInventory().getContents()[i];
                if (stack != null && stack.isSimilar(thing)) {
                    return i;
                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            return -1;
        }
        return -1;





    }
    public static boolean isSword(Material material){
        return material==Material.WOOD_SWORD||material==Material.STONE_SWORD||material==Material.IRON_SWORD||material==Material.GOLD_SWORD||material==Material.DIAMOND_SWORD;
    }
}
