package com.xlxyvergil.hamstercore.weapon;

import com.mojang.serialization.Codec;

/**
 * 武器类型枚举
 * 定义了游戏中具体的武器类型
 */
public enum WeaponType {
    // 主要武器类型
    RIFLE("rifle", "步枪", WeaponCategory.PRIMARY, true),
    SMG("smg", "冲锋枪", WeaponCategory.PRIMARY, true),
    LMG("lmg", "机枪", WeaponCategory.PRIMARY, true),
    RPG("rpg", "火箭筒", WeaponCategory.PRIMARY, true),
    SHOTGUN("shotgun", "霰弹枪", WeaponCategory.PRIMARY, true),
    SNIPER("sniper", "狙击枪", WeaponCategory.PRIMARY, true),
    RANGED("ranged", "远程武器", WeaponCategory.PRIMARY, true),
    
    // 次要武器类型
    PISTOL("pistol", "手枪", WeaponCategory.SECONDARY, true),
    
    // 近战武器类型
    MELEE("melee", "近战武器", WeaponCategory.MELEE, false);
    
    private final String name;
    private final String displayName;
    private final WeaponCategory category;
    private final boolean isRanged;
    
    WeaponType(String name, String displayName, WeaponCategory category, boolean isRanged) {
        this.name = name;
        this.displayName = displayName;
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
     * 获取武器类型显示名称
     */
    public String getDisplayName() {
        return displayName;
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
