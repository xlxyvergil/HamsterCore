package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.util.WeaponDataNBTUtil;
import com.xlxyvergil.hamstercore.compat.ModCompat;
import com.xlxyvergil.hamstercore.util.TacZItemsFetcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * TACZ配置应用类
 * 处理TACZ物品的NBT应用
 */
public class TacZConfigApplier {
    
    /**
     * 应用TACZ物品的配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigToItem() {
        // 检查TACZ是否已加载
        if (!ModCompat.isTaczLoaded()) {
            return 0;
        }
        
        int appliedCount = 0;
        
        // 获取所有TACZ物品
        Map<ResourceLocation, String> taczItems = TacZItemsFetcher.getAllGunIds();
        for (Map.Entry<ResourceLocation, String> entry : taczItems.entrySet()) {
            // 创建物品堆
            ItemStack stack = new ItemStack(entry.getKey().getBlock());
            
            // 设置gunId到NBT
            if (stack.hasTag()) {
                stack.getTag().putString("GunId", entry.getValue());
            } else {
                stack.getOrCreateTag().putString("GunId", entry.getValue());
            }
            
            // 获取TACZ物品配置
            WeaponData weaponData = WeaponConfig.getTacZWeaponConfig(stack);
            if (weaponData != null) {
                // 应用配置到物品NBT
                WeaponDataNBTUtil.writeWeaponDataToNBT(stack, weaponData);
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 应用TACZ物品的配置到物品堆
     * @param stack 物品堆
     * @return 是否成功应用配置
     */
    public static boolean applyConfigToItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 检查是否为TACZ物品
        if (!ModCompat.isTacZItem(stack)) {
            return false;
        }
        
        // 获取TACZ物品配置
        WeaponData weaponData = WeaponConfig.getTacZWeaponConfig(stack);
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