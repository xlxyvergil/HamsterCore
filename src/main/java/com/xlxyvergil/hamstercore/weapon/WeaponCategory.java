package com.xlxyvergil.hamstercore.weapon;

import com.mojang.serialization.Codec;
import java.util.Arrays;
import java.util.List;
import net.minecraft.network.chat.Component;

/**
 * 武器分类枚举
 * 定义了游戏中的武器类型分类
 */
public enum WeaponCategory {
    // 主要武器 - 远程武器（步枪类）
    PRIMARY("primary", 
            Arrays.asList(WeaponType.RIFLE, WeaponType.SMG, WeaponType.LMG, 
                    WeaponType.RPG, WeaponType.SNIPER, WeaponType.RANGED)),
    
    // 突击步枪
    ASSAULT_RIFLE("assault_rifle", 
            Arrays.asList(WeaponType.RIFLE, WeaponType.SMG, WeaponType.LMG)),
    
    // 狙击枪
    SNIPER_CATEGORY("sniper", 
            Arrays.asList(WeaponType.SNIPER)),
    
    // 弓箭（除了tacz外的所有远程）
    BOW("bow", 
            Arrays.asList(WeaponType.RANGED)),
    
    // 次要武器 - 手枪
    SECONDARY("secondary", 
            Arrays.asList(WeaponType.PISTOL)),
    
    // 近战武器
    MELEE("melee", 
            Arrays.asList(WeaponType.MELEE));
    
    private final String name;
    private final List<WeaponType> allowedTypes;
    
    WeaponCategory(String name, List<WeaponType> allowedTypes) {
        this.name = name;
        this.allowedTypes = allowedTypes;
    }
    
    /**
     * 获取分类名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取分类显示名称的翻译键
     */
    public String getTranslationKey() {
        return "weapon.category." + this.name;
    }
    
    /**
     * 获取分类显示名称
     */
    public Component getDisplayName() {
        return Component.translatable(this.getTranslationKey());
    }
    
    /**
     * 获取该分类允许的武器类型列表
     */
    public List<WeaponType> getAllowedTypes() {
        return allowedTypes;
    }
    
    /**
     * 检查指定的武器类型是否属于该分类
     */
    public boolean allowsWeaponType(WeaponType type) {
        return allowedTypes.contains(type);
    }
    
    /**
     * 根据名称获取武器分类
     */
    public static WeaponCategory byName(String name) {
        for (WeaponCategory category : values()) {
            if (category.name.equals(name)) {
                return category;
            }
        }
        return PRIMARY; // 默认返回PRIMARY，避免返回null
    }
    
    /**
     * 用于编解码的Codec
     */
    public static final Codec<WeaponCategory> CODEC = Codec.STRING.xmap(
        name -> {
            for (WeaponCategory category : values()) {
                if (category.name.equals(name)) {
                    return category;
                }
            }
            return PRIMARY; // 默认返回PRIMARY，避免返回null导致改装件加载失败
        },
        WeaponCategory::getName
    );
}