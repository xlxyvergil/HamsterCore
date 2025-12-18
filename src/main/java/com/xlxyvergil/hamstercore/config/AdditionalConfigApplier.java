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
     * 加载额外物品配置并应用到物品
     * 在onServerStarted事件中调用此方法
     */
    public static void load() {
        applyConfigToItem();
    }
    
    /**
     * 应用额外的元素属性配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigToItem() {
        int appliedCount = 0;
        
        // 确保配置已加载
        WeaponConfig.init();
        
        // 获取所有额外配置
        // 加载额外普通武器配置
        WeaponConfig.loadAdditionalNormalWeaponConfigs();
        
        // 获取所有额外配置
        Map<ResourceLocation, WeaponData> additionalConfigs = WeaponConfig.getAdditionalWeaponConfigs();
        if (additionalConfigs == null || additionalConfigs.isEmpty()) {
            return 0;
        }
        
        // 为每个额外配置应用元素属性
        for (Map.Entry<ResourceLocation, WeaponData> entry : additionalConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            if (applyConfigToSingleItem(itemKey, weaponData)) {
                appliedCount++;
            }
        }
        
        // 注意：WeaponItemIds现在通过统一的初始化系统收集所有ID，不需要手动添加
        
        return appliedCount;
    }
    
    /**
     * 应用配置到单个物品
     */
    private static boolean applyConfigToSingleItem(ResourceLocation itemKey, WeaponData weaponData) {
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
                // 将配置好的物品保存到全局映射中，供游戏运行时使用
                WeaponConfig.cacheConfiguredItemStack(itemKey, stack);
            }
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}