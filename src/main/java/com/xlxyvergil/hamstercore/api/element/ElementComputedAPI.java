package com.xlxyvergil.hamstercore.api.element;

import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.world.item.ItemStack;

/**
 * 元素系统Computed层API
 * 提供对物品NBT中Computed层数据的修改接口
 */
public class ElementComputedAPI {
    
    /**
     * 向物品的Computed层添加元素数据
     * 
     * @param stack 物品堆
     * @param type 元素类型名称
     * @param value 元素数值
     * @param operation 计算操作(add/sub/mul/div)
     */
    public static void addComputedElement(ItemStack stack, String type, double value, String operation) {
        WeaponDataManager.addComputedElement(stack, type, value, operation);
    }
    
    /**
     * 向物品的Extra层添加派系增伤数据
     * 
     * @param stack 物品堆
     * @param faction 派系名称
     * @param value 数值
     * @param operation 计算操作(add/sub)
     */
    public static void addExtraFactionModifier(ItemStack stack, String faction, double value, String operation) {
        WeaponDataManager.addExtraFaction(stack, faction, value, operation);
    }
    
    /**
     * 清除物品的Computed层数据
     * 
     * @param stack 物品堆
     */
    public static void clearComputedData(ItemStack stack) {
        WeaponDataManager.clearComputedData(stack);
    }
}