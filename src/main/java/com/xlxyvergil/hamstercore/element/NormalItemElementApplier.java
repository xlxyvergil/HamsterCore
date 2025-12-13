package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.util.DebugLogger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
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
     */
    private static boolean applyElementAttributesToNormalItem(ResourceLocation itemKey, WeaponData weaponData) {
        try {
            // 创建物品栈
            Item item = BuiltInRegistries.ITEM.get(itemKey);
            if (item == null) {
                return false;
            }
            
            ItemStack stack = new ItemStack(item);
            if (stack.isEmpty()) {
                return false;
            }
            
            // 应用元素修饰符到物品（使用独立实现并保存Basic层数据）
            applyElementModifiers(stack, weaponData.getBasicElements());
            
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 应用元素修饰符到物品
     * 根据配置文件中的元素属性修饰符，在Basic层里存储修饰符的元素类型、排序以及是否是CONFIG
     */
    private static void applyElementModifiers(ItemStack stack, Map<String, List<WeaponData.BasicEntry>> basicElements) {
        // 同时将Basic层数据保存到NBT中
        WeaponData weaponData = new WeaponData();
        for (Map.Entry<String, List<WeaponData.BasicEntry>> entry : basicElements.entrySet()) {
            for (WeaponData.BasicEntry basicEntry : entry.getValue()) {
                weaponData.addBasicElement(basicEntry.getType(), basicEntry.getSource(), basicEntry.getOrder());
            }
        }
        
        // 保存元素数据到NBT（只保存Basic层）
        WeaponDataManager.saveElementDataWithoutUsage(stack, weaponData);
    }

}