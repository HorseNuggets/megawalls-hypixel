package net.nuggetmc.mw.mwclass;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.energy.Energy;
import net.nuggetmc.mw.mwclass.info.Diamond;
import net.nuggetmc.mw.mwclass.info.MWClassInfo;
import net.nuggetmc.mw.mwclass.info.Playstyle;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MWClassMenu implements Listener {

    private final MWClassManager manager;

    private final String menuTitle;
    private final String closeName;

    private final Map<String, ItemStack> CACHE;

    public MWClassMenu(String title, MWClassManager manager) {
        this.manager = manager;

        menuTitle = title;
        closeName = ChatColor.RED + "Close";
        CACHE = new HashMap<>();
    }

    public void openGUI(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, menuTitle);

        int n = 1;

        for (Map.Entry<String, MWClass> entry : MWClassManager.getClasses().entrySet()) {
            ItemStack item = generateClassInfo(entry.getValue());
            inv.setItem(n + 9 + 2 * ((n - 1) / 7), item);

            n++;
        }

        inv.setItem(49, createClose());
        player.openInventory(inv);
    }

    private void select(Player player, String name) {
        MWClass mwclass = MWClassManager.fetch(name);
        if (mwclass == null) return;

        select(player, mwclass);
    }

    public void select(Player player, MWClass mwclass) {
        player.sendMessage("You have selected " + mwclass.getColor() + mwclass.getName() + ChatColor.RESET + ".");
        player.closeInventory();

        Energy.clear(player);

        manager.assign(player, mwclass);
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

        if (name.equals(closeName)) {
            player.closeInventory();
            return;
        }

        select(player, ChatColor.stripColor(name));
    }

    private ItemStack createClose() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.setDisplayName(closeName);
        item.setItemMeta(meta);

        return item;
    }

    private ItemStack generateClassInfo(MWClass mwclass) {
        String name = mwclass.getName();

        if (CACHE.containsKey(name)) {
            return CACHE.get(name);
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

        for (Diamond diamond : diamonds) {
            diamondValues.add(ChatColor.AQUA + StringUtils.capitalize(diamond.name().toLowerCase()));
        }

        lore.add(ChatColor.GRAY + "Diamond: " + String.join(ChatColor.GRAY + ", ", diamondValues));
        lore.add("");

        MWClassInfo info = mwclass.getInfo();

        lore.add(ChatColor.GRAY + "Ability: " + ChatColor.RED + info.getAbilityName());
        lore.addAll(info.getAbilityInfo());
        lore.add("");

        lore.add(ChatColor.GRAY + "Passive I: " + ChatColor.RED + info.getPassive1Name());
        lore.addAll(info.getPassive1Info());
        lore.add("");

        lore.add(ChatColor.GRAY + "Passive II: " + ChatColor.RED + info.getPassive2Name());
        lore.addAll(info.getPassive2Info());
        lore.add("");

        lore.add(ChatColor.GRAY + "Gathering: " + ChatColor.RED + info.getGatheringName());
        lore.addAll(info.getGatheringInfo());
        lore.add("");

        lore.add(ChatColor.GRAY + "Energy Gain:");

        for (Map.Entry<String, String> entry : info.getEnergyGain().entrySet()) {
            lore.add(ChatColor.DARK_GRAY + " ▪ " + ChatColor.GRAY + entry.getKey() + ": " + ChatColor.GREEN + entry.getValue());
        }

        lore.add("");
        lore.add(ChatColor.GRAY + "Click to play!");

        meta.setLore(lore);
        item.setItemMeta(meta);

        CACHE.put(name, item);

        return item;
    }
}
