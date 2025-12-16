package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Map;

/**
 * 额外配置应用类
 * 处理额外的元素属性配置应用
 */
public class AdditionalConfigApplier {
    
    /**
     * 应用额外的元素属性配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigToItem() {
        int appliedCount = 0;
        
        // 确保配置已加载
        WeaponConfig.init();
        
        // 获取所有额外配置
        Map<ResourceLocation, WeaponData> additionalConfigs = WeaponConfig.getAdditionalWeaponConfigs();
        if (additionalConfigs == null || additionalConfigs.isEmpty()) {
            return 0;
        }
        
        // 为每个额外配置应用元素属性
        for (Map.Entry<ResourceLocation, WeaponData> entry : additionalConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            if (applyAdditionalElementAttributes(itemKey, weaponData)) {
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 为物品应用额外的元素属性
     */
    private static boolean applyAdditionalElementAttributes(ResourceLocation itemKey, WeaponData weaponData) {
        if (weaponData == null) {
            return false;
        }
        
        try {
            // 将配置保存到全局配置映射中，以便在游戏中使用
            WeaponConfig.cacheAdditionalWeaponConfig(itemKey, weaponData);
            
            // 创建物品堆并仅保存InitialModifier数据到NBT
            Item item = BuiltInRegistries.ITEM.get(itemKey);
            if (item != null) {
                ItemStack stack = new ItemStack(item);
                // 只保存InitialModifier层数据
                WeaponDataManager.saveInitialModifierData(stack, weaponData);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}