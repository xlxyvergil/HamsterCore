package com.xlxyvergil.hamstercore.config;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.util.WeaponDataNBTUtil;
import com.xlxyvergil.hamstercore.util.ModSpecialItemsFetcher;
import com.tacz.guns.init.ModItems;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class TacZConfigApplier {
    
    /**
     * 应用TACZ配置到所有TACZ物品
     * @return 成功应用配置的物品数量
     */
    public static int applyConfigs() {
        int appliedCount = 0;
        
        // 检查TACZ模组是否已加载
        if (!net.minecraftforge.fml.ModList.get().isLoaded("tacz")) {
            return 0;
        }
        
        // 获取所有TACZ物品 - ModSpecialItemsFetcher会自动检查模组是否加载
        Set<ResourceLocation> taczItems = ModSpecialItemsFetcher.getTacZGunIDs();
        if (taczItems.isEmpty()) {
            return 0;
        }
        
        // 应用每个TACZ枪械的属性
        for (ResourceLocation gunId : taczItems) {
            if (applyGunAttributes(gunId)) {
                appliedCount++;
            }
        }
        
        return appliedCount;
    }
    
    /**
     * 为枪械应用属性 - 基于旧版本逻辑
     */
    private static boolean applyGunAttributes(ResourceLocation gunId) {
        try {
            // 从配置中获取对应的武器数据
            WeaponData weaponData = WeaponConfig.getWeaponConfigByGunId(gunId.toString());
            if (weaponData == null) {
                return false;
            }
            
            // 创建基础物品栈 - 使用配置中的物品ID而不是硬编码
            ResourceLocation itemKey;
            if (weaponData.modid != null && weaponData.itemId != null) {
                itemKey = new ResourceLocation(weaponData.modid, weaponData.itemId);
            } else {
                // 默认使用tacz:modern_kinetic_gun
                itemKey = new ResourceLocation("tacz", "modern_kinetic_gun");
            }
            
            net.minecraft.world.item.Item item = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(itemKey);
            if (item == null) {
                return false;
            }
            
            ItemStack stack = new ItemStack(item);
            
            if (stack.isEmpty()) {
                return false;
            }
            
            // 设置gunId到NBT - 这是TACZ物品的关键标识
            stack.getOrCreateTag().putString("gunId", gunId.toString());
            
            // 写入武器数据到NBT
            WeaponDataNBTUtil.writeWeaponDataToNBT(stack, weaponData);
            
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
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
        
        // 检查是否为TACZ物品 - 使用ModSpecialItemsFetcher
        if (!ModSpecialItemsFetcher.isTacZGun(stack)) {
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