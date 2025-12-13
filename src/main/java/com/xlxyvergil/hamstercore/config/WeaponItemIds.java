package com.xlxyvergil.hamstercore.config;

import net.minecraft.resources.ResourceLocation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 武器道具ID管理器
 * 专门存储WeaponConfig生成的配置文件中所有道具的ID
 */
public class WeaponItemIds {
    
    // 存储所有武器道具ID的集合
    private static final Set<ResourceLocation> WEAPON_ITEM_IDS = new HashSet<>();
    
    /**
     * 添加武器道具ID
     * @param itemKey 武器道具的ResourceLocation
     */
    public static void addWeaponItemId(ResourceLocation itemKey) {
        if (itemKey != null) {
            WEAPON_ITEM_IDS.add(itemKey);
        }
    }
    
    /**
     * 批量添加武器道具ID
     * @param itemKeys 武器道具的ResourceLocation集合
     */
    public static void addAllWeaponItemIds(Set<ResourceLocation> itemKeys) {
        if (itemKeys != null) {
            WEAPON_ITEM_IDS.addAll(itemKeys);
        }
    }
    
    /**
     * 获取所有武器道具ID的不可变视图
     * @return 所有武器道具ID的集合
     */
    public static Set<ResourceLocation> getAllWeaponItemIds() {
        return Collections.unmodifiableSet(WEAPON_ITEM_IDS);
    }
    
    /**
     * 检查指定的道具ID是否为已配置的武器
     * @param itemKey 道具的ResourceLocation
     * @return 是否为已配置的武器
     */
    public static boolean isConfiguredWeapon(ResourceLocation itemKey) {
        return itemKey != null && WEAPON_ITEM_IDS.contains(itemKey);
    }
    
    /**
     * 清空所有武器道具ID（通常在重新加载配置时使用）
     */
    public static void clearAllWeaponItemIds() {
        WEAPON_ITEM_IDS.clear();
    }
}