package net.nuggetmc.mw.mwclass.info;

public enum EnumInfoType {
    ABILITY("Ability"),
    PASSIVE_1("Passive I"),
    PASSIVE_2("Passive II"),
    GATHERING("Gathering Talent");

    private final String label;

    EnumInfoType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
