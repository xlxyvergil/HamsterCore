package com.xlxyvergil.hamstercore.weapon;

import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;

/**
 * 武器类型枚举
 * 定义了游戏中具体的武器类型
 */
public enum WeaponType {
    // 主要武器类型
    RIFLE("rifle", WeaponCategory.PRIMARY, true),
    SMG("smg", WeaponCategory.PRIMARY, true),
    LMG("lmg", WeaponCategory.PRIMARY, true),
    RPG("rpg", WeaponCategory.PRIMARY, true),
    SHOTGUN("shotgun", WeaponCategory.PRIMARY, true),
    SNIPER("sniper", WeaponCategory.PRIMARY, true),
    RANGED("ranged", WeaponCategory.PRIMARY, true),
    
    // 次要武器类型
    PISTOL("pistol", WeaponCategory.SECONDARY, true),
    
    // 近战武器类型
    MELEE("melee", WeaponCategory.MELEE, false);
    
    private final String name;
    private final WeaponCategory category;
    private final boolean isRanged;
    
    WeaponType(String name, WeaponCategory category, boolean isRanged) {
        this.name = name;
        this.category = category;
        this.isRanged = isRanged;
    }
    
    /**
     * 获取武器类型名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取武器类型显示名称的翻译键
     */
    public String getTranslationKey() {
        return "weapon.type." + this.name;
    }
    
    /**
     * 获取武器类型显示名称
     */
    public Component getDisplayName() {
        return Component.translatable(this.getTranslationKey());
    }
    
    /**
     * 获取武器所属分类
     */
    public WeaponCategory getCategory() {
        return category;
    }
    
    /**
     * 判断是否为远程武器
     */
    public boolean isRanged() {
        return isRanged;
    }
    
    /**
     * 判断是否为近战武器
     */
    public boolean isMelee() {
        return !isRanged;
    }
    
    /**
     * 根据名称获取武器类型
     */
    public static WeaponType byName(String name) {
        for (WeaponType type : values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 用于编解码的Codec
     */
    public static final Codec<WeaponType> CODEC = Codec.STRING.xmap(
        name -> {
            for (WeaponType type : values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return null;
        },
        WeaponType::getName
    );
}
