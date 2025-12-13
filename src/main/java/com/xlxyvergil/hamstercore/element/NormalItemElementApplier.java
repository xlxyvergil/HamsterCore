package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * 普通物品元素应用器
 * 专门处理普通物品（非拔刀剑非TACZ）的元素属性应用
 */
public class NormalItemElementApplier {
    
    /**
     * 应用普通物品的元素属性
     */
    public static int applyNormalItemsElements() {
        DebugLogger.log("开始应用普通物品元素属性...");
        
        // 确保配置已加载
        WeaponConfig.load();
        
        // 获取普通武器配置
        Map<ResourceLocation, WeaponData> normalWeaponConfigs = WeaponConfig.getAllWeaponConfigs();
        DebugLogger.log("配置文件中共有 %d 个普通武器配置", normalWeaponConfigs.size());
        
        int appliedCount = 0;
        
        // 遍历所有普通物品配置，应用元素属性
        for (Map.Entry<ResourceLocation, WeaponData> entry : normalWeaponConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            DebugLogger.log("处理普通物品: %s", itemKey.toString());
            if (applyElementAttributesToNormalItem(itemKey, weaponData)) {
                appliedCount++;
            }
        }
        
        DebugLogger.log("普通物品元素属性应用完成，处理了 %d 个普通物品", appliedCount);
        return appliedCount;
    }
    
    /**
     * 为普通物品应用元素属性
     * 使用新的两层数据结构
     */
    private static boolean applyElementAttributesToNormalItem(ResourceLocation itemKey, WeaponData weaponData) {
        // 检查武器数据是否为空
        if (weaponData == null) {
            return false;
        }
        
        try {
            // 创建实际的ItemStack用于存储元素属性
            Item item = BuiltInRegistries.ITEM.get(itemKey);
            if (item == null) {
                return false;
            }
            
            ItemStack stack = new ItemStack(item);
            
            if (stack.isEmpty()) {
                return false;
            }
            
            // 应用元素修饰符到物品
            ElementApplier.applyElementModifiers(stack, weaponData.getBasicElements());
            
            // 保存元素数据到NBT（只保存Basic层和Usage层）
            WeaponDataManager.saveElementData(stack, weaponData);
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}