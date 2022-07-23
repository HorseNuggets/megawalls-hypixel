package net.nuggetmc.mw.economics;

import jdk.nashorn.internal.objects.annotations.Getter;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.craftbukkit.libs.jline.internal.Nullable;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class Good {
    private ItemStack displayItem;
    private int price;
    private String extralore;
    private String displayname;
    private ItemStack theItem;
    public Good(ItemStack displayItem, String displayname , int price,ItemStack theItem, @Nullable String extralore){
        this.displayItem=displayItem;
        this.price = price;
        this.displayname=displayname;
        this.theItem=theItem;
        this.extralore=extralore;
    }
    @Getter
    public ItemStack getDisplayItem(){
        return this.displayItem;
    }
    @Getter
    public ItemStack getTheItem(){
        return this.theItem;
    }
    @Getter
    public String getDisplayName(){
        return this.displayname;
    }
    @Getter
    public int getPrice(){
        return this.price;
    }
    public ItemStack getMenuItem() {





        ItemStack item = new ItemStack(this.displayItem);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.YELLOW + this.displayname);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.GREEN+"Price: "+this.price);

        if (extralore!=null) {
            lore.add(extralore);
        }





        meta.setLore(lore);
        item.setItemMeta(meta);



        return item;
    }
}
