package net.nuggetmc.mw.mwclass.info;

import net.md_5.bungee.api.ChatColor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MWClassInfo {

    private final Map<EnumInfoType, ClassInfoEntry> data;
    private final Map<String, String> energyGain;

    public MWClassInfo(String ability, String abilityInfo, String passive1, String passive1Info, String passive2, String passive2Info, String gatheringTalent, String gatheringTalentInfo) {
        this.data = new HashMap<>();

        data.put(EnumInfoType.ABILITY, new ClassInfoEntry(ability, abilityInfo));
        data.put(EnumInfoType.PASSIVE_1, new ClassInfoEntry(passive1, passive1Info));
        data.put(EnumInfoType.PASSIVE_2, new ClassInfoEntry(passive2, passive2Info));
        data.put(EnumInfoType.GATHERING, new ClassInfoEntry(gatheringTalent, gatheringTalentInfo));

        this.energyGain = new HashMap<>();
    }

    public ClassInfoEntry getInfoEntry(EnumInfoType type) {
        return data.get(type);
    }

    public List<String> getLoreFormatted(EnumInfoType type, boolean whitespace) {
        List<String> list = new ArrayList<>();
        ClassInfoEntry entry = getInfoEntry(type);

        if (entry != null) {
            list.add(ChatColor.GRAY + type.getLabel() + ": " + ChatColor.RED + entry.getName());
            list.addAll(entry.getDescription());

            if (whitespace) {
                list.add("");
            }
        }

        return list;
    }

    public List<String> getLoreFormatted(EnumInfoType type) {
        return getLoreFormatted(type, true);
    }

    public Map<String, String> getEnergyGain() {
        return energyGain;
    }

    /*
     * Energy gain types:
     * - Melee
     * - Bow
     * - When Hit
     * - When Bowed
     * - Per Second
     */

    public void addEnergyGainType(String type, Object value) {
        energyGain.put(type, String.valueOf(value));
    }
}
