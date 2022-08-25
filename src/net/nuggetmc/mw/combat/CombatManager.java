package net.nuggetmc.mw.combat;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CombatManager {
    public static List<Player> inCombatPlayers=new ArrayList<>();

    public void addInCombat(Player player){
        inCombatPlayers.add(player);
    }
    public boolean isInCombat(Player player){
        return inCombatPlayers.contains(player);
    }
    public void removeInCombat(Player player){
        if (isInCombat(player)){
        inCombatPlayers.remove(player);
    }
    }

}
