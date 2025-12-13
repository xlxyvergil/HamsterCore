package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.util.WeaponDataNBTUtil;
import com.xlxyvergil.hamstercore.compat.ModCompat;
import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * 拔刀剑配置应用类
 * 处理拔刀剑物品的NBT应用
 */
public class SlashBladeConfigApplier {
    
    /**
     * 应用拔刀剑物品的配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigToItem() {
        // 检查拔刀剑是否已加载
        if (!ModCompat.isSlashBladeLoaded()) {
            return 0;
        }
        
        int appliedCount = 0;
        
        // 获取所有拔刀剑物品translationKeys
        Set<String> slashBladeKeys = SlashBladeItemsFetcher.getAllTranslationKeys();
        for (String translationKey : slashBladeKeys) {
            // 创建物品堆（使用默认的slahblade物品）
            ItemStack stack = new ItemStack(ModCompat.getSlashBladeItem());
            
            // 设置translationKey到NBT
            if (stack.hasTag()) {
                stack.getTag().putString("TranslationKey", translationKey);
            } else {
                stack.getOrCreateTag().putString("TranslationKey", translationKey);
            }
            
            // 获取拔刀剑物品配置
            WeaponData weaponData = WeaponConfig.getSlashBladeWeaponConfig(stack);
            if (weaponData != null) {
                // 应用配置到物品NBT
                WeaponDataNBTUtil.writeWeaponDataToNBT(stack, weaponData);
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 应用拔刀剑物品的配置到物品堆
     * @param stack 物品堆
     * @return 是否成功应用配置
     */
    public static boolean applyConfigToItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        // 检查是否为拔刀剑物品
        if (!ModCompat.isSlashBladeItem(stack)) {
            return false;
        }
        
        // 获取拔刀剑物品配置
        WeaponData weaponData = WeaponConfig.getSlashBladeWeaponConfig(stack);
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