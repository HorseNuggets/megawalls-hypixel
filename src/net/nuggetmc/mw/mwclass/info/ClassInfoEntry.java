package net.nuggetmc.mw.mwclass.info;

import net.nuggetmc.mw.utils.DisplayUtils;

import java.util.List;

public class ClassInfoEntry {

    private final String name;
    private final List<String> description;

    public ClassInfoEntry(String name, String description) {
        this.name = name;
        this.description = DisplayUtils.fit(description);
    }

    public String getName() {
        return name;
    }

    public List<String> getDescription() {
        return description;
    }
}
