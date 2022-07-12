package net.nuggetmc.mw.mwclass;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.energy.EnergyManager;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.EnumInfoType;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import net.nuggetmc.mw.utils.WorldUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class MWClassMenu implements Listener {

    private static final String CLOSE_NAME = ChatColor.RED + "Close";

    private final MWClassManager manager;
    private final EnergyManager energyManager;
    private final String menuTitle;
    private final Map<String, ItemStack> cache;

    public MWClassMenu(MegaWalls plugin, String title) {
        this.manager = plugin.getManager();
        this.energyManager = plugin.getEnergyManager();
        this.menuTitle = title;
        this.cache = new HashMap<>();
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, menuTitle);

        int n = 1;

        for (Map.Entry<String, MWClass> entry : manager.getClasses().entrySet()) {
            ItemStack item = generateClassInfo(entry.getValue());
            inv.setItem(n + 9 + 2 * ((n - 1) / 7), item);

            n++;
        }

        inv.setItem(49, createClose());
        player.openInventory(inv);
    }

    private void select(Player player, String name) {
        MWClass mwclass = manager.fetch(name);
        if (mwclass == null) return;

        select(player, mwclass);
    }

    public void select(Player player, MWClass mwclass) {
        player.sendMessage("You have selected " + mwclass.getColor() + mwclass.getName() + ChatColor.RESET + ".");
        player.closeInventory();

        energyManager.clear(player);
        player.getInventory().clear();
        manager.assign(player, mwclass, true);
        Location loc=new Location(player.getWorld(),MegaWalls.getInstance().spawnx, MegaWalls.getInstance().spawny, MegaWalls.getInstance().spawnz);
        player.teleport(WorldUtils.nearby(loc));
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

    private ItemStack generateClassInfo(MWClass mwclass) {
        String name = mwclass.getName();

        if (cache.containsKey(name)) {
            return cache.get(name);
        }

        ItemStack item = new ItemStack(mwclass.getIcon());
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(ChatColor.YELLOW + name);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        List<String> lore = new ArrayList<>();

        Playstyle[] styles = mwclass.getPlaystyles();
        List<String> styleValues = new ArrayList<>();

        for (Playstyle style : styles) {
            styleValues.add(Playstyle.display(style));
        }

        lore.add(ChatColor.GRAY + "Playstyle: " + String.join(ChatColor.GRAY + ", ", styleValues));

        Diamond[] diamonds = mwclass.getDiamonds();
        List<String> diamondValues = new ArrayList<>();

        Arrays.stream(diamonds).forEach(d -> diamondValues.add(ChatColor.AQUA + StringUtils.capitalize(d.name().toLowerCase())));

        lore.add(ChatColor.GRAY + "Diamond: " + String.join(ChatColor.GRAY + ", ", diamondValues));
        lore.add("");

        MWClassInfo info = mwclass.getInfo();
        Arrays.stream(EnumInfoType.values()).forEach(type -> lore.addAll(info.getLoreFormatted(type)));

        lore.add(ChatColor.GRAY + "Energy Gain:");

        info.getEnergyGain().forEach((key, value) -> lore.add(ChatColor.DARK_GRAY + " ▪ " + ChatColor.GRAY + key + ": " + ChatColor.GREEN + value));

        lore.add("");
        lore.add(ChatColor.GRAY + "Click to play!");

        meta.setLore(lore);
        item.setItemMeta(meta);

        cache.put(name, item);

        return item;
    }
}
