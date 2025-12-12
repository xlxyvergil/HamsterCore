package com.xlxyvergil.hamstercore.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.HashSet;
import java.util.Set;

/**
 * 武器适用物品查找器
 * 查找所有可以应用元素属性的物品
 */
public class WeaponApplicableItemsFinder {
    
    /**
     * 查找所有可应用元素属性的物品
     * @return 可应用的物品ID集合
     */
    public static Set<ResourceLocation> findApplicableItems() {
        Set<ResourceLocation> applicableItems = new HashSet<>();
        
        // 遍历所有已注册的物品
        for (Item item : BuiltInRegistries.ITEM) {
            ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(item);
            if (itemKey == null || itemKey == BuiltInRegistries.ITEM.getDefaultKey()) {
                continue;
            }
            
            // 排除MOD特殊物品
            if ("slashblade:slashblade".equals(itemKey.toString()) || 
                "tacz:modern_kinetic_gun".equals(itemKey.toString())) {
                continue;
            }
            
            // 检查物品是否为武器或工具
            if (WeaponJudgeUtil.isWeaponOrTool(item)) {
                applicableItems.add(itemKey);
            }
        }
        
        return applicableItems;
    }
}