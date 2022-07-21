package net.nuggetmc.mw.combat;

import net.nuggetmc.mw.MegaWalls;
import net.nuggetmc.mw.mwclass.MWClassManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.HashMap;
import java.util.Map;

public class CoinsManager implements Listener {
    private Map<Player,Integer> coinsData = new HashMap<>();
    private final MegaWalls plugin=MegaWalls.getInstance();


    public CoinsManager(){

    }

    public void set(Player player, int amount) {
        coinsData.put(player, amount);

        saveCoins(player, amount);
    }

    private void saveCoins(Player player, int amount) {
        plugin.getConfig().set("coins." + player.getName(), amount);
        plugin.saveConfig();
    }

    public int get(Player player){
        if (coinsData.containsKey(player)) {
            return coinsData.get(player);
        }

        return 0;
    }
    public void add(Player player, int amount) {
        if (!coinsData.containsKey(player)) {
            set(player, amount);
            return;
        }

        int current = coinsData.get(player);
        int updated = current + amount;


        set(player, updated);
    }
    @EventHandler
    public void onKill(PlayerDeathEvent e){
       //
    }


}
