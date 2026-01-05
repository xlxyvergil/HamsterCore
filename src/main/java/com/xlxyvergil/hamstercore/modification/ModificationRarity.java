package com.xlxyvergil.hamstercore.modification;

import com.mojang.serialization.Codec;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import net.minecraft.ChatFormatting;

/**
 * 改装件稀有度定义
 */
public enum ModificationRarity implements CodecProvider<ModificationRarity> {
    COMMON("common", 0, ChatFormatting.WHITE),
    UNCOMMON("uncommon", 1, ChatFormatting.YELLOW),
    RARE("rare", 2, ChatFormatting.BLUE),
    EPIC("epic", 3, ChatFormatting.DARK_PURPLE),
    MYTHIC("mythic", 4, ChatFormatting.GOLD);

    public static final Codec<ModificationRarity> CODEC = Codec.STRING.xmap(
        ModificationRarity::fromString,
        ModificationRarity::getName
    );

    private final String name;
    private final int rarityLevel;
    private final ChatFormatting color;

    ModificationRarity(String name, int rarityLevel, ChatFormatting color) {
        this.name = name;
        this.rarityLevel = rarityLevel;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getRarityLevel() {
        return rarityLevel;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /**
     * 从字符串获取稀有度
     */
    public static ModificationRarity fromString(String name) {
        for (ModificationRarity rarity : values()) {
            if (rarity.name.equals(name)) {
                return rarity;
            }
        }
        return COMMON; // 默认返回普通
    }

    @Override
    public Codec<? extends ModificationRarity> getCodec() {
        return CODEC;
    }
}
