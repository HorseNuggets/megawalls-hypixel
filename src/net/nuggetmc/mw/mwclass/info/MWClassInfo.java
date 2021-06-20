package net.nuggetmc.mw.mwclass.info;

import net.nuggetmc.mw.utils.DisplayUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MWClassInfo {

    private final String ABILITY_NAME;
    private final List<String> ABILITY_INFO;

    private final String PASSIVE_1;
    private final List<String> PASSIVE_1_INFO;

    private final String PASSIVE_2;
    private final List<String> PASSIVE_2_INFO;

    private final String GATHERING_TALENT;
    private final List<String> GATHERING_TALENT_INFO;

    private final Map<String, String> ENERGY_GAIN;

    public MWClassInfo(String ability, String abilityInfo, String passive1, String passive1Info, String passive2, String passive2Info, String gatheringTalent, String gatheringTalentInfo) {
        ABILITY_NAME = ability;
        ABILITY_INFO = DisplayUtils.fit(abilityInfo);

        PASSIVE_1 = passive1;
        PASSIVE_1_INFO = DisplayUtils.fit(passive1Info);

        PASSIVE_2 = passive2;
        PASSIVE_2_INFO = DisplayUtils.fit(passive2Info);

        GATHERING_TALENT = gatheringTalent;
        GATHERING_TALENT_INFO = DisplayUtils.fit(gatheringTalentInfo);

        ENERGY_GAIN = new HashMap<>();
    }

    public String getAbilityName() {
        return ABILITY_NAME;
    }

    public List<String> getAbilityInfo() {
        return ABILITY_INFO;
    }

    public String getPassive1Name() {
        return PASSIVE_1;
    }

    public List<String> getPassive1Info() {
        return PASSIVE_1_INFO;
    }

    public String getPassive2Name() {
        return PASSIVE_2;
    }

    public List<String> getPassive2Info() {
        return PASSIVE_2_INFO;
    }

    public String getGatheringTalentName() {
        return GATHERING_TALENT;
    }

    public List<String> getGatheringTalentInfo() {
        return GATHERING_TALENT_INFO;
    }

    public Map<String, String> getEnergyGain() {
        return ENERGY_GAIN;
    }

    /*
     * Energy gain types:
     * - Melee
     * - Bow
     * - When Hit
     * - When Bowed
     * - Per Second
     */

    public void addEnergyGainType(String type, int value) {
        ENERGY_GAIN.put(type, String.valueOf(value));
    }

    public void addEnergyGainType(String type, String value) {
        ENERGY_GAIN.put(type, value);
    }
}
