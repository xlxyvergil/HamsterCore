package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.util.WeaponDataNBTUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

/**
 * 额外配置应用类
 * 处理额外物品的NBT应用
 */
public class AdditionalConfigApplier {
    
    /**
     * 应用额外物品的配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigToItem() {
        int appliedCount = 0;
        
        // 遍历所有额外配置的物品
        for (Map.Entry<ResourceLocation, WeaponData> entry : WeaponConfig.getAdditionalWeaponConfigs().entrySet()) {
            ResourceLocation itemKey = entry.getKey();
            WeaponData weaponData = entry.getValue();
            
            // 获取物品
            Item item = ForgeRegistries.ITEMS.getValue(itemKey);
            if (item != null) {
                // 创建物品堆
                ItemStack stack = new ItemStack(item);
                
                // 应用配置到物品NBT
                WeaponDataNBTUtil.writeWeaponDataToNBT(stack, weaponData);
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 应用额外物品的配置到物品堆
     * @param stack 物品堆
     * @return 是否成功应用配置
     */
    public static boolean applyConfigToItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 获取物品的ResourceLocation
        ResourceLocation itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem());
        if (itemKey == null || itemKey == ForgeRegistries.ITEMS.getDefaultKey()) {
            return false;
        }
        
        // 获取物品配置
        WeaponData weaponData = WeaponConfig.getWeaponConfig(stack);
        if (weaponData == null) {
            return false;
        }
        
        // 应用配置到物品NBT
        WeaponDataNBTUtil.writeWeaponDataToNBT(stack, weaponData);
        
        return true;
    }
    
    /**
     * 从物品堆中读取武器数据
     * @param stack 物品堆
     * @return 武器数据，如果不存在则返回null
     */
    public static WeaponData readWeaponDataFromItem(ItemStack stack) {
        return WeaponDataNBTUtil.readWeaponDataFromNBT(stack);
    }
    
    /**
     * 检查物品堆是否包含武器数据
     * @param stack 物品堆
     * @return 是否包含武器数据
     */
    public static boolean hasWeaponData(ItemStack stack) {
        return WeaponDataNBTUtil.hasWeaponData(stack);
    }
}