package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Map;

/**
 * 普通物品配置应用类
 * 处理普通物品的NBT应用
 */
public class NormalConfigApplier {
    
    /**
     * 应用普通物品的配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigToItem() {
        int appliedCount = 0;
        
        // 确保配置已加载
        WeaponConfig.init();
        
        // 获取所有武器配置
        Map<ResourceLocation, WeaponData> allWeaponConfigs = WeaponConfig.getAllWeaponConfigs();
        
        // 遍历所有配置，过滤掉MOD特殊物品
        for (Map.Entry<ResourceLocation, WeaponData> entry : allWeaponConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            // 检查是否为MOD特殊物品
            if (isModSpecialItem(itemKey)) {
                continue; // 跳过MOD特殊物品，由其他应用器处理
            }
            
            // 为普通物品应用元素属性
            if (applyElementAttributesToNormalItem(itemKey, weaponData)) {
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 判断是否为MOD特殊物品
     */
    private static boolean isModSpecialItem(ResourceLocation itemKey) {
        String itemKeyStr = itemKey.toString();
        
        // 检查是否为TACZ枪械
        if ("tacz:modern_kinetic_gun".equals(itemKeyStr)) {
            return true;
        }
        
        // 检查是否为拔刀剑
        if ("slashblade:slashblade".equals(itemKeyStr)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 为普通物品应用元素属性
     */
    private static boolean applyElementAttributesToNormalItem(ResourceLocation itemKey, WeaponData weaponData) {
        if (weaponData == null) {
            return false;
        }
        
        try {
            // 将配置保存到全局配置映射中，以便在游戏中使用
            WeaponConfig.cacheWeaponConfig(itemKey, weaponData);
            
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
    
    /**
     * 应用普通物品的配置到物品堆
     * @param stack 物品堆
     * @return 是否成功应用配置
     */
    public static boolean applyConfigToItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 获取物品的ResourceLocation
        ResourceLocation itemKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemKey == null || itemKey == BuiltInRegistries.ITEM.getDefaultKey()) {
            return false;
        }
        
        // 获取物品配置
        WeaponData weaponData = WeaponConfig.getWeaponConfig(stack);
        if (weaponData == null) {
            return false;
        }
        
        // 应用配置到物品NBT，只保存InitialModifier层数据
        WeaponDataManager.saveInitialModifierData(stack, weaponData);
        
        return true;
    }
    
    /**
     * 从物品堆中读取武器数据
     * @param stack 物品堆
     * @return 武器数据，如果不存在则返回null
     */
    public static WeaponData readWeaponDataFromItem(ItemStack stack) {
        return WeaponDataManager.loadElementData(stack);
    }
    
    /**
     * 检查物品堆是否包含武器数据
     * @param stack 物品堆
     * @return 是否包含武器数据
     */
    public static boolean hasWeaponData(ItemStack stack) {
        return WeaponDataManager.loadElementData(stack) != null;
    }
} 