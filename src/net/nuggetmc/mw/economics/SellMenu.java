package net.nuggetmc.mw.economics;

import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_8_R3.Items;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.energy.EnergyManager;
import net.nuggetmc.mw.mwclass.MWClassManager;
import net.nuggetmc.mw.utils.ItemStackCreator;
import net.nuggetmc.mw.utils.ItemUtils;
import net.nuggetmc.mw.utils.SpecialItemUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class SellMenu implements Listener {
    MegaWalls plugin=MegaWalls.getInstance();

    SpecialItemUtils specialItemUtils= plugin.getSpecialItemUtils();
    private static final String CLOSE_NAME = ChatColor.RED + "Close";


    private final String menuTitle="Sell";
    private static Map<ItemStack,Integer> things=new HashMap<>();


    public SellMenu() {
        things.put(new ItemStack(Material.DIAMOND),50);
        things.put(new ItemStack(Material.IRON_ORE),4);
        //things.put(new ItemStack(Material.COBBLESTONE),1);
    }



    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, menuTitle);



        for (int i=0;i<things.keySet().size();i++){
            inv.setItem(i+10, (ItemStack) things.keySet().toArray()[i]);
        }




        inv.setItem(49, createClose());
        player.openInventory(inv);
    }




    public void select(Player player, ItemStack thing) {
        int itemSlot= ItemUtils.findItemSlot(player,thing);
        if (itemSlot==-1){
            player.sendMessage("There is no such item in your inventory!");
        }else {
            int amount = player.getInventory().getContents()[itemSlot].getAmount();
            int totalcoin = amount * things.get(thing);
            plugin.getCoinsManager().add(player, totalcoin);
            player.getInventory().clear(itemSlot);
            player.sendMessage("You have sold "+amount+" "+ChatColor.YELLOW+(thing.hasItemMeta()?thing.getItemMeta().getDisplayName():thing.getType().name())+ChatColor.RESET+" , getting "+ChatColor.GREEN+ totalcoin +ChatColor.RESET+" coins.");
        }


    }

    @EventHandler
    public void click(InventoryClickEvent event) {
        Inventory inv = event.getClickedInventory();
        if (inv == null) return;

        String invName = inv.getName();
        if (!invName.equals(menuTitle)) return;

        event.setCancelled(true);
        //event.getWhoClicked().sendMessage("1");
        ItemStack item = event.getCurrentItem();
        if (item == null) return;
       // event.getWhoClicked().sendMessage("2");
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
       // event.getWhoClicked().sendMessage("3");
        //String name = meta.getDisplayName();
        //if (name == null) return;
       // event.getWhoClicked().sendMessage("4");


        Player player = (Player) event.getWhoClicked();

        if (item.getItemMeta().getDisplayName()!=null&&item.getItemMeta().getDisplayName().equals(CLOSE_NAME)) {
            player.closeInventory();
            return;
        }

        select(player, item);
    }

    private ItemStack createClose() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(CLOSE_NAME);
        item.setItemMeta(meta);

        return item;
    }


}
