package com.xlxyvergil.hamstercore.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 物品词缀适用性判断工具类
 * 用于判断物品是否适用于词缀和附魔
 * 
 * 判断条件：
 * 1. 符合WeaponJudgeUtil里的判断的道具
 * 2. 或者命名空间为slashblade:slashblade的道具
 * 3. 或者命名空间为tacz:modern_kinetic_gun的道具
 */
public class ItemAffixApplicableUtil {
    
    /**
     * 检查物品是否适用于词缀和附魔
     * 
     * @param item 要检查的物品
     * @return 如果适用于词缀和附魔返回true
     */
    public static boolean isItemApplicableForAffix(Item item) {
        // 检查物品注册名
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        if (registryName == null) {
            return false;
        }
        
        String itemName = registryName.toString();
        
        // 条件1: 命名空间为slashblade:slashblade的道具
        if ("slashblade:slashblade".equals(itemName)) {
            return true;
        }
        
        // 条件2: 命名空间为tacz:modern_kinetic_gun的道具
        if ("tacz:modern_kinetic_gun".equals(itemName)) {
            return true;
        }
        
        // 条件3: 符合WeaponJudgeUtil里的判断的道具
        if (WeaponJudgeUtil.isWeaponOrTool(item)) {
            return true;
        }
        
        return false;
    }
    
    /**
     * 检查物品栈是否适用于词缀和附魔
     * 
     * @param stack 要检查的物品栈
     * @return 如果适用于词缀和附魔返回true
     */
    public static boolean isItemApplicableForAffix(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        
        return isItemApplicableForAffix(stack.getItem());
    }
}