package com.xlxyvergil.hamstercore.faction;

public enum Faction {
    GRINEER("Grineer"),
    INFESTED("Infested"),
    CORPUS("Corpus"),
    OROKIN("Orokin"),
    SENTIENT("Sentient"),
    MURMUR("Murmur"),
    NONE("None");
    
    private final String displayName;
    
    Faction(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}