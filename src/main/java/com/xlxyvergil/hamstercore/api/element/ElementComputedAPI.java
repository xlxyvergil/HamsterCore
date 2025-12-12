package com.xlxyvergil.hamstercore.api.element;

import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.world.item.ItemStack;

/**
 * 元素系统Computed层API
 * 提供对物品NBT中Computed层数据的修改接口
 */
public class ElementComputedAPI {
    
    /**
     * 向物品的Computed层添加带具体来源标识符的元素数据
     * 
     * @param stack 物品堆
     * @param type 元素类型名称
     * @param value 元素数值
     * @param operation 计算操作(add/sub/mul/div)
     * @param specificSource 具体来源标识符
     */
    public static void addComputedElementWithSpecificSource(ItemStack stack, String type, double value, String operation, String specificSource) {
        WeaponDataManager.addComputedElementWithSpecificSource(stack, type, value, operation, specificSource);
    }
    
    /**
     * 从物品的Computed层移除指定具体来源标识符的元素数据
     * 
     * @param stack 物品堆
     * @param type 元素类型名称
     * @param specificSource 具体来源标识符
     */
    public static void removeComputedElementBySpecificSource(ItemStack stack, String type, String specificSource) {
        WeaponDataManager.removeComputedElementBySpecificSource(stack, type, specificSource);
    }
    
    /**
     * 向物品的Extra层添加带具体来源标识符的派系增伤数据
     * 
     * @param stack 物品堆
     * @param faction 派系名称
     * @param value 数值
     * @param operation 计算操作(add/sub)
     * @param specificSource 具体来源标识符
     */
    public static void addExtraFactionModifierWithSpecificSource(ItemStack stack, String faction, double value, String operation, String specificSource) {
        WeaponDataManager.addExtraFactionWithSpecificSource(stack, faction, value, operation, specificSource);
    }
    
    /**
     * 从物品的Extra层移除指定具体来源标识符的派系增伤数据
     * 
     * @param stack 物品堆
     * @param faction 派系名称
     * @param specificSource 具体来源标识符
     */
    public static void removeExtraFactionModifierBySpecificSource(ItemStack stack, String faction, String specificSource) {
        WeaponDataManager.removeExtraFactionBySpecificSource(stack, faction, specificSource);
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