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
     * 加载普通物品配置并应用到物品
     * 在onServerStarted事件中调用此方法
     */
    public static void load() {
        applyConfigToItem();
    }
    
    /**
     * 应用普通物品的配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigToItem() {
        int appliedCount = 0;
        
        try {
            // 直接从文件加载默认武器配置
            WeaponConfig.loadDefaultWeaponConfigsFromFile();
            
            // 获取所有武器配置
            Map<ResourceLocation, WeaponData> weaponConfigs = WeaponConfig.getAllWeaponConfigs();
            if (weaponConfigs == null || weaponConfigs.isEmpty()) {
                return 0;
            }
            
            // 遍历所有配置，过滤掉MOD特殊物品
            for (Map.Entry<ResourceLocation, WeaponData> entry : weaponConfigs.entrySet()) {
                ResourceLocation itemKey = entry.getKey();
                WeaponData weaponData = entry.getValue();
                
                // 检查是否为MOD特殊物品
                if (isModSpecialItem(itemKey)) {
                    continue; // 跳过MOD特殊物品，由其他应用器处理
                }
                
                // 直接应用配置到物品
                if (applyConfigToSingleItem(itemKey, weaponData)) {
                    appliedCount++;
                }
            }
            
            // 注意：WeaponItemIds现在通过统一的初始化系统收集所有ID，不需要手动添加
        } catch (Exception e) {
            e.printStackTrace();
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
     * 应用配置到单个物品
     */
    private static boolean applyConfigToSingleItem(ResourceLocation itemKey, WeaponData weaponData) {
        if (weaponData == null) {
            return false;
        }
        
        try {
            // 将配置保存到全局配置映射中，以便在游戏中使用
            WeaponConfig.cacheWeaponConfig(itemKey, weaponData);
            
            // 获取物品并创建一个物品堆
            Item item = BuiltInRegistries.ITEM.get(itemKey);
            if (item != null) {
                // 创建一个临时的物品堆用于保存NBT数据
                ItemStack tempStack = new ItemStack(item);
                // 将配置数据保存到物品的NBT中
                WeaponDataManager.saveInitialModifierData(tempStack, weaponData);
                // 将配置好的物品保存到全局映射中，供游戏运行时使用
                WeaponConfig.cacheConfiguredItemStack(itemKey, tempStack);
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
        
        // 检查是否为MOD特殊物品
        if (isModSpecialItem(itemKey)) {
            return false; // MOD特殊物品不由这个类处理
        }
        
        // 从WeaponConfig类获取配置（从缓存中）
        WeaponData weaponData = WeaponConfig.getWeaponConfig(stack);
        
        if (weaponData == null) {
            return false;
        }
        
        // 应用配置到物品NBT，只保存InitialModifier层数据
        WeaponDataManager.saveInitialModifierData(stack, weaponData);
        
        return true;
    }
}