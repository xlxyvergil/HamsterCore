package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.util.WeaponDataNBTUtil;
import com.xlxyvergil.hamstercore.util.WeaponJudgeUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraftforge.registries.ForgeRegistries;

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
        
        // 遍历所有已注册的物品
        for (Map.Entry<net.minecraft.resources.ResourceKey<Item>, Item> entry : ForgeRegistries.ITEMS.getEntries()) {
            ResourceLocation itemKey = entry.getKey().location();
            Item item = entry.getValue();
            
            // 检查物品是否为武器或工具
            if (WeaponJudgeUtil.isWeaponOrTool(item)) {
                // 创建物品堆
                ItemStack stack = new ItemStack(item);
                
                // 获取物品配置
                WeaponData weaponData = WeaponConfig.getWeaponConfig(stack);
                if (weaponData != null) {
                    // 应用配置到物品NBT
                    WeaponDataNBTUtil.writeWeaponDataToNBT(stack, weaponData);
                    appliedCount++;
                }
            }
        }
        
        return appliedCount;
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