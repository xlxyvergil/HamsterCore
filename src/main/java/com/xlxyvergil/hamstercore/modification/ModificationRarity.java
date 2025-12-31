package com.xlxyvergil.hamstercore.modification;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.util.ExtraCodecs;

public enum ModificationRarity {
    COMMON(0, ChatFormatting.WHITE, 10, "common"),
    UNCOMMON(1, ChatFormatting.YELLOW, 8, "uncommon"),
    RARE(2, ChatFormatting.AQUA, 6, "rare"),
    EPIC(3, ChatFormatting.LIGHT_PURPLE, 4, "epic"),
    MYTHIC(4, ChatFormatting.GOLD, 2, "mythic");

    private static final Map<String, ModificationRarity> BY_ID = new HashMap<>();

    static {
        for (ModificationRarity rarity : values()) {
            BY_ID.put(rarity.name, rarity);
        }
    }

    public static final Codec<ModificationRarity> CODEC = ExtraCodecs.stringResolverCodec(ModificationRarity::getName, ModificationRarity::byId);

    private final ChatFormatting color;
    private final int weight;
    private final String name;

    ModificationRarity(int ordinal, ChatFormatting color, int weight, String name) {
        this.color = color;
        this.weight = weight;
        this.name = name;
    }

    public ChatFormatting getColor() {
        return color;
    }

    public int getWeight() {
        return weight;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public static ModificationRarity byId(String name) {
        return BY_ID.get(name);
    }
}
