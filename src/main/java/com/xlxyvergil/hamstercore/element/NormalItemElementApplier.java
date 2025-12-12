package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.HamsterCore;
import com.xlxyvergil.hamstercore.config.WeaponConfig;
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
        
        // 确保配置已加载
        WeaponConfig.load();
        
        // 获取普通武器配置
        Map<ResourceLocation, WeaponData> normalWeaponConfigs = WeaponConfig.getAllWeaponConfigs();
        
        int appliedCount = 0;
        
        // 遍历所有普通物品配置，应用元素属性
        for (Map.Entry<ResourceLocation, WeaponData> entry : normalWeaponConfigs.entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            if (applyElementAttributesToNormalItem(itemKey, weaponData)) {
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 为普通物品应用元素属性
     * 使用新的四层数据结构
     */
    private static boolean applyElementAttributesToNormalItem(ResourceLocation itemKey, WeaponData weaponData) {
        // 检查武器数据是否为空
        if (weaponData == null) {
            return false;
        }
        
        try {
            // 创建实际的ItemStack用于存储元素属性
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemKey);
            if (item == null) {
                return false;
            }
            
            ItemStack stack = new ItemStack(item);
            
            // 确保物品栈有效
            if (stack.isEmpty()) {
                return false;
            }
            
            // 直接使用从配置加载的WeaponElementData
            WeaponElementData elementData = weaponData.getElementData();
            
            // 确保elementData不为空
            if (elementData == null) {
                elementData = new WeaponElementData();
            } else {
            }
            
            // 计算Usage数据
            WeaponDataManager.computeUsageData(stack, elementData);
            
            // 将数据写入NBT
            WeaponDataManager.saveElementData(stack, elementData);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}