package net.nuggetmc.mw.economics;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.energy.EnergyManager;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ShopMenu implements Listener {
    MegaWalls plugin=MegaWalls.getInstance();
    private static final String CLOSE_NAME = ChatColor.RED + "Close";

    private final MWClassManager classmanager =plugin.getClassManager();
    private final EnergyManager energyManager= plugin.getEnergyManager();
    private final String menuTitle="Shop";
    private static Map<String,Good> goods=new HashMap<>();
    Good gapple=new Good(new ItemStack(Material.GOLDEN_APPLE,1),"Golden Apple",300,new ItemStack(Material.GOLDEN_APPLE,1),null);
    //an example
    private void registergood(Good good){
        goods.put(good.getDisplayName(),good);
    }
    private Good getGoodByName(String goodname){
        return goods.get(goodname);
    }

    public ShopMenu() {
        registergood(gapple);
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, menuTitle);




            inv.setItem(10, gapple.getMenuItem());
          //  inv.setItem(11, example);




        inv.setItem(49, createClose());
        player.openInventory(inv);
    }

    private void select(Player player, String name) {
        Good good=getGoodByName(name);
        if (good == null) return;

        select(player, good);
    }


    public void select(Player player, Good good) {
        if (plugin.getCoinsManager().get(player)<good.getPrice()){
            player.sendMessage("not enough price!");
        }else {
            plugin.getCoinsManager().add(player,-good.getPrice());
            player.getInventory().addItem(good.getTheItem());
        }

    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null) return;

        String invName = inv.getName();
        if (!invName.equals(menuTitle)) return;

        event.setCancelled(true);

        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        String name = meta.getDisplayName();
        if (name == null) return;

        Player player = (Player) event.getWhoClicked();

        if (name.equals(CLOSE_NAME)) {
            player.closeInventory();
            return;
        }

        select(player, ChatColor.stripColor(name));
    }

    private ItemStack createClose() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(CLOSE_NAME);
        item.setItemMeta(meta);

        return item;
    }




}
