package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.util.WeaponDataNBTUtil;

import com.xlxyvergil.hamstercore.util.SlashBladeItemsFetcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
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
        return applyConfigs();
    }
    
    /**
     * 应用拔刀剑物品的配置到物品堆
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigs() {
        int appliedCount = 0;
        
        // 检查拔刀剑是否已加载
        if (!SlashBladeItemsFetcher.isSlashBladeLoaded()) {
            return 0;
        }
        
        // 获取所有拔刀剑物品translationKeys
        Set<String> slashBladeKeys = SlashBladeItemsFetcher.getSlashBladeTranslationKeys();
        for (String translationKey : slashBladeKeys) {
            if (applySlashBladeAttributes(translationKey)) {
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 为拔刀剑应用属性 - 基于旧版本逻辑
     */
    private static boolean applySlashBladeAttributes(String translationKey) {
        try {
            // 从配置中获取对应的武器数据
            WeaponData weaponData = WeaponConfig.getWeaponConfigByTranslationKey(translationKey);
            if (weaponData == null) {
                return false;
            }
            
            // 创建基础物品栈 - 使用配置中的物品ID而不是硬编码
            ResourceLocation itemKey;
            if (weaponData.modid != null && weaponData.itemId != null) {
                itemKey = new ResourceLocation(weaponData.modid, weaponData.itemId);
            } else {
                // 默认使用slashblade:slashblade
                itemKey = new ResourceLocation("slashblade", "slashblade");
            }
            
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemKey);
            if (item == null) {
                return false;
            }
            
            ItemStack stack = new ItemStack(item);
            
            if (stack.isEmpty()) {
                return false;
            }
            
            // 设置translationKey到NBT - 这是拔刀剑物品的关键标识
            stack.getOrCreateTag().putString("translationKey", translationKey);
            
            // 写入武器数据到NBT
            WeaponDataNBTUtil.writeWeaponDataToNBT(stack, weaponData);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        
        // 检查是否为拔刀剑物品 - 使用SlashBladeItemsFetcher
        if (!SlashBladeItemsFetcher.isSlashBlade(stack)) {
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