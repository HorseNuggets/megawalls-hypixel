package net.nuggetmc.mw.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemStackCreator{

	public static ItemStack updateItem(ItemStack currentItem, List<String> lore){
		ItemMeta im = currentItem.getItemMeta();
		im.setLore(lore);
		currentItem.setItemMeta(im);
		return currentItem;
	}
	public static ItemStack createItem(Material mat, String name, int amount) {
		ItemStack stack = new ItemStack(mat, amount);
		ItemMeta stackmeta = stack.getItemMeta();
		stackmeta.setDisplayName(name);
		stack.setItemMeta(stackmeta);
		return stack;
	}

	public static ItemStack createItem(Material type, String name,List<String> asList) {
		ItemStack stack = new ItemStack(type);
		ItemMeta stackmeta = stack.getItemMeta();
		stackmeta.setDisplayName(name);
		stackmeta.setLore(asList);
		stack.setItemMeta(stackmeta);
		return stack;
	}

	public static ItemStack createItem(Material type, String name,String[] description) {
		ItemStack stack = new ItemStack(type);
		ItemMeta stackmeta = stack.getItemMeta();
		stackmeta.setDisplayName(name);
		stackmeta.setLore(Arrays.asList(description));
		stack.setItemMeta(stackmeta);
		return stack;
	}

	public static ItemStack createItem(ItemStack mat, String name,List<String> asList) {
		ItemStack stack = mat;
		ItemMeta stackmeta = stack.getItemMeta();
		stackmeta.setDisplayName(name);
		stackmeta.setLore(asList);
		stack.setItemMeta(stackmeta);
		return stack;
	}

	public static ItemStack createItemWithData(Material mat, String name,
			int amount, int data) {
		ItemStack stack = new ItemStack(mat, amount, (byte) data);
		ItemMeta stackmeta = stack.getItemMeta();
		stackmeta.setDisplayName(name);
		stack.setItemMeta(stackmeta);
		return stack;
	}

	public static ItemStack createItemStack(Material mat,
			HashMap<Enchantment, Integer> enchants) {
		ItemStack item = new ItemStack(mat);
		if ((item != null) && (item.getItemMeta() != null)) {
			ItemMeta itemMeta = item.getItemMeta();
			if (!enchants.isEmpty()) {
				for (Enchantment enchantment : enchants.keySet()) {
					itemMeta.addEnchant(enchantment, enchants.get(enchantment),
							true);
				}
			}
			item.setItemMeta(itemMeta);
		}
		return item;
	}

	public static ItemStack createItem(Material mat, String name, int amount,
			List<String> lore) {
		ItemStack stack = new ItemStack(mat, amount);
		ItemMeta stackmeta = stack.getItemMeta();
		stackmeta.setDisplayName(name);
		stackmeta.setLore(lore);
		stack.setItemMeta(stackmeta);
		return stack;
	}

	public static ItemStack createItem(ItemStack mat, String name, int amount,
			List<String> lore) {
		ItemStack stack = mat;
		ItemMeta stackmeta = stack.getItemMeta();
		stackmeta.setDisplayName(name);
		stackmeta.setLore(lore);
		stack.setItemMeta(stackmeta);
		return stack;
	}

	public static ItemStack createItem(ItemStack mat, String name, int amount) {
		ItemStack stack = mat;
		stack.setAmount(amount);
		ItemMeta stackmeta = stack.getItemMeta();
		stackmeta.setDisplayName(name);
		stack.setItemMeta(stackmeta);
		return stack;
	}

	public static ItemStack Skull(ItemStack item, String name) {
		SkullMeta meta = (SkullMeta) item.getItemMeta();
		meta.setOwner(name);
		meta.setDisplayName("�rskull!");
		item.setItemMeta(meta);
		return item;
	}

	public static ItemStack createItemStack(Material mat, int amount, byte data, String name, List<String> lore, HashMap<Enchantment, Integer> enchants)
	{
		ItemStack item = new ItemStack(mat, amount, data);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName(name);
		itemMeta.setLore(lore);
		if (!enchants.isEmpty()) {
			for (Enchantment enchantment : enchants.keySet()) {
				itemMeta.addEnchant(enchantment, ((Integer)enchants.get(enchantment)).intValue(), true);
			}
		}
		item.setItemMeta(itemMeta);
		return item;
	}

	public static ItemStack createItemStack(Material mat, int amount, byte data, String name, List<String> lore)
	{
		ItemStack item = new ItemStack(mat, amount, data);
		ItemMeta itemMeta = item.getItemMeta();
		itemMeta.setDisplayName(name);
		itemMeta.setLore(lore);
		item.setItemMeta(itemMeta);
		return item;
	}

	public static ItemStack createItemStack(ItemStack item, String name, List<String> lore, HashMap<Enchantment, Integer> enchants)
	{
		if ((item != null) && (item.getItemMeta() != null)) {
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.setDisplayName(name);
			itemMeta.setLore(lore);
			if (!enchants.isEmpty()) {
				for (Enchantment enchantment : enchants.keySet()) {
					itemMeta.addEnchant(enchantment, ((Integer)enchants.get(enchantment)).intValue(), true);
				}
			}
			item.setItemMeta(itemMeta);
		}
		return item;
	}
	public static ItemStack createItemStack(ItemStack item, String name, String[] lore, HashMap<Enchantment, Integer> enchants)
	{
		if ((item != null) && (item.getItemMeta() != null)) {
			item.setAmount(1);
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.setDisplayName(name);
			List<String> list = new ArrayList<String>();
			for(String string : lore){
				list.add("�7"+string);
			}
			itemMeta.setLore(list);
			if (!enchants.isEmpty()) {
				for (Enchantment enchantment : enchants.keySet()) {
					itemMeta.addEnchant(enchantment, ((Integer)enchants.get(enchantment)).intValue(), true);
				}
			}
			item.setItemMeta(itemMeta);
		}
		return item;
	}

	public static ItemStack createItemStack(ItemStack item, String name, List<String> lore){
		if ((item != null) && (item.getItemMeta() != null)) {
			item.setAmount(1);
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.setDisplayName(name);
			itemMeta.setLore(lore);
			item.setItemMeta(itemMeta);
		}
		return item;
	}

	public static ItemStack createItemStack(ItemStack item, int newAmount, String name, List<String> lore, HashMap<Enchantment, Integer> enchants)
	{
		if ((item != null) && (item.getItemMeta() != null)) {
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.setDisplayName(name);
			itemMeta.setLore(lore);
			if (!enchants.isEmpty()) {
				for (Enchantment enchantment : enchants.keySet()) {
					itemMeta.addEnchant(enchantment, ((Integer)enchants.get(enchantment)).intValue(), true);
				}
			}

			item.setItemMeta(itemMeta);
		}
		item.setAmount(newAmount);
		return item;
	}

	public static ItemStack createItemStack(ItemStack item, int newAmount, String name, List<String> lore)
	{
		if ((item != null) && (item.getItemMeta() != null)) {
			ItemMeta itemMeta = item.getItemMeta();
			itemMeta.setDisplayName(name);
			itemMeta.setLore(lore);
			item.setItemMeta(itemMeta);
		}	
		item.setAmount(newAmount);
		return item;
	}

	public static ItemStack createItemStack(ItemStack item, String name, byte data)
	{
		if ((item != null) && (item.getItemMeta() != null)) {
			ItemStack newItem = new ItemStack(item.getType(), 1, data);
			ItemMeta itemMeta = newItem.getItemMeta();
			itemMeta.setDisplayName(name);
			itemMeta.setLore(item.getItemMeta().getLore());
			newItem.setItemMeta(itemMeta);
			return newItem;
		}
		return item;
	}

	public static ItemStack createItemStack(ItemStack curr, Material mat, byte data, List<String> arrayList)
	{
		ItemStack item = new ItemStack(mat, curr.getAmount(), data);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(curr.getItemMeta().getDisplayName());
		im.setLore(arrayList);
		item.setItemMeta(im);
		return item;
	}

	public static ItemStack createItemStack(ItemStack curr, String name, byte data, List<String> lore)
	{
		ItemStack item = new ItemStack(curr.getType(), curr.getAmount(), data);
		ItemMeta im = item.getItemMeta();
		im.setDisplayName(name);
		im.setLore(lore);
		item.setItemMeta(im);
		return item;
	}
}