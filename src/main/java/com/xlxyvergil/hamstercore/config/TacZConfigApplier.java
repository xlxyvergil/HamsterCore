package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.util.WeaponDataNBTUtil;
import com.xlxyvergil.hamstercore.compat.ModCompat;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class TacZConfigApplier {
    
    /**
     * 应用TACZ配置到所有TACZ物品
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigs() {
        // 检查TACZ模组是否已加载
        if (!ModCompat.isTaczLoaded()) {
            return 0;
        }
        
        int appliedCount = 0;
        
        // 获取所有TACZ物品
        Set<ResourceLocation> taczItems = ModSpecialItemsFetcher.getTacZGunIDs();
        for (ResourceLocation gunId : taczItems) {
            // 创建物品堆
            ItemStack stack = new ItemStack(ModCompat.getTaczItem());
            
            // 应用元素属性
            WeaponData data = WeaponConfig.getWeaponConfigByGunId(gunId.toString());
            if (data != null) {
                // 写入NBT数据
                WeaponDataNBTUtil.writeWeaponDataToNBT(stack, data);
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