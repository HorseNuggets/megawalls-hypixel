package net.nuggetmc.mw.economics;

import net.md_5.bungee.api.ChatColor;
import net.nuggetmc.mw.MegaWalls;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.Comparator;
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

    public void saveCoins(Player player, int amount) {
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
        if (plugin.getEnergyManager().validate(e)==null){
            return;
        }
        Player killer=plugin.getEnergyManager().validate(e);
        //
        plugin.getCoinsManager().add(killer,20);
        killer.sendMessage(ChatColor.YELLOW+"+ 20 Coins (Kill Player)!");
    }
    public void clear(Player player){
        set(player,0);
    }
    public String getBalTop(){
        StringBuilder result = new StringBuilder();
        result.append(ChatColor.AQUA + "------------BalTop------------\n");
        Map<String,Integer> data=new HashMap<>();
        ArrayList<String> mapArrayList=new ArrayList<>();
        for (String playername:plugin.getConfig().getConfigurationSection("coins").getKeys(false)){
            data.put(playername,(plugin.getOrDefaultFromConfig("coins."+playername,0)));
        }
        for (int i=0;i<data.keySet().toArray().length;i++){
            mapArrayList.add((String) data.keySet().toArray()[i]);
        }
        mapArrayList.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                if (data.get(s)<data.get(t1)){
                    return 1;
                }else {
                    return -1;
                }
            }
        });
        for (int i=0;i<mapArrayList.size();i++){
            result.append(getColorOfGrade(i+1)+"["+(i+1)+"] "+mapArrayList.get(i)+" : "+data.get(mapArrayList.get(i))+"\n");
        }
        result.append(ChatColor.AQUA + "------------------------------\n");
        return result.toString();
    }
    public Map<String,Integer> getMapBalTop(){
        Map<String,Integer> result=new HashMap<>();
        Map<String,Integer> data=new HashMap<>();
        ArrayList<String> mapArrayList=new ArrayList<>();
        for (String playername:plugin.getConfig().getConfigurationSection("coins").getKeys(false)){
            data.put(playername,(plugin.getOrDefaultFromConfig("coins."+playername,0)));
        }
        for (int i=0;i<data.keySet().toArray().length;i++){
            mapArrayList.add((String) data.keySet().toArray()[i]);
        }
        mapArrayList.sort(new Comparator<String>() {
            @Override
            public int compare(String s, String t1) {
                if (data.get(s)<data.get(t1)){
                    return 1;
                }else {
                    return -1;
                }
            }
        });
        for (int i=0;i<mapArrayList.size();i++){
            result.put(mapArrayList.get(i),i+1);
        }
        return result;
    }
    public int getRankOnBalTop(Player player){
        if (!getMapBalTop().containsKey(player.getName())){
            return 0;
        }
        return getMapBalTop().get(player.getName());

    }
    private ChatColor getColorOfGrade(int input){
        switch (input){
            case 1:
                return ChatColor.GOLD;
            case 2:
                return ChatColor.AQUA;
            case 3:
                return ChatColor.GREEN;
            default:
                return ChatColor.RESET;
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        ConfigurationSection sectionCoins = plugin.getConfig().getConfigurationSection("coins");

        if (sectionCoins==null) return;
        int coinamount;
        try {
            coinamount = (int) plugin.getConfig().get("coins." + e.getPlayer().getName());
        }catch (Exception exception){
            return;
        }
        plugin.getCoinsManager().set(e.getPlayer(),coinamount);
        plugin.saveConfig();
    }


}
