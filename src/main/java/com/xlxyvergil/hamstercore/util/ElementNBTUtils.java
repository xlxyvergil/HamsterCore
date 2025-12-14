package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import net.minecraft.world.item.ItemStack;

import java.util.*;

/**
 * 元素NBT工具类
 * 提供对物品上元素数据的读取和检查功能
 */
public class ElementNBTUtils {
    
    /**
     * 检查物品是否包含任何元素数据
     * @param stack 物品堆
     * @return 是否包含元素数据
     */
    public static boolean hasAnyElements(ItemStack stack) {
        return WeaponDataManager.readElementData(stack) != null;
    }
    
    /**
     * 获取物品上所有Usage层的元素类型
     * @param stack 物品堆
     * @return 元素类型集合
     */
    public static Set<String> getAllUsageElementTypes(ItemStack stack) {
        WeaponData data = WeaponDataManager.readElementData(stack);
        if (data != null) {
            return data.getUsageElements().keySet();
        }
        return Collections.emptySet();
    }
    
    /**
     * 获取物品上指定Usage层元素的值
     * @param stack 物品堆
     * @param elementTypeName 元素类型名称
     * @return 元素值列表
     */
    public static List<Double> getUsageElementValue(ItemStack stack, String elementTypeName) {
        WeaponData data = WeaponDataManager.readElementData(stack);
        if (data != null) {
            Double value = data.getUsageValue(elementTypeName);
            if (value != null) {
                List<Double> result = new ArrayList<>();
                result.add(value);
                return result;
            }
        }
        return new ArrayList<>();
    }
}