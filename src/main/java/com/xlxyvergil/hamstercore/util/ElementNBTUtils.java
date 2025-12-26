package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.ElementUsageData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * 元素NBT工具类
 * 用于读取和处理武器上存储在NBT中的元素数据
 */
public class ElementNBTUtils {
    
    /**
     * 检查物品是否包含元素数据
     * @param stack 物品栈
     * @return 如果包含元素数据返回true，否则返回false
     */
    public static boolean hasElementData(ItemStack stack) {
        return ElementUsageData.hasElementData(stack);
    }
    
    /**
     * 从物品中读取所有元素数据
     * @param stack 物品栈
     * @return 元素数据对象，如果物品不包含元素数据则返回空的ElementData对象
     */
    public static ElementUsageData.ElementData readElementData(ItemStack stack) {
        return ElementUsageData.readElementDataFromItem(stack);
    }
    
    /**
     * 从物品中读取暴击相关统计数据
     * @param stack 物品栈
     * @return 暴击相关统计数据映射
     */
    public static Map<String, Double> readCriticalStats(ItemStack stack) {
        if (!hasElementData(stack)) {
            return new HashMap<>();
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        return new HashMap<>(elementData.getCriticalStats());
    }
    
    /**
     * 从物品中读取物理元素数据
     * @param stack 物品栈
     * @return 物理元素数据映射
     */
    public static Map<String, Double> readPhysicalElements(ItemStack stack) {
        if (!hasElementData(stack)) {
            return new HashMap<>();
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        return new HashMap<>(elementData.getPhysicalElements());
    }
    
    /**
     * 从物品中读取派系元素数据
     * @param stack 物品栈
     * @return 派系元素数据映射
     */
    public static Map<String, Double> readFactionElements(ItemStack stack) {
        if (!hasElementData(stack)) {
            return new HashMap<>();
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        return new HashMap<>(elementData.getFactionElements());
    }
    
    /**
     * 从物品中读取复合元素数据
     * @param stack 物品栈
     * @return 复合元素数据映射
     */
    public static Map<String, Double> readCombinedElements(ItemStack stack) {
        if (!hasElementData(stack)) {
            return new HashMap<>();
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        return new HashMap<>(elementData.getCombinedElements());
    }
    
    /**
     * 从物品中读取指定元素类型的值
     * @param stack 物品栈
     * @param elementType 元素类型名称
     * @return 元素值，如果不存在则返回0.0
     */
    public static double readElementValue(ItemStack stack, String elementType) {
        if (!hasElementData(stack)) {
            return 0.0;
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        
        // 检查暴击相关统计
        Double value = elementData.getCriticalStats().get(elementType);
        if (value != null) {
            return value;
        }
        
        // 检查物理元素
        value = elementData.getPhysicalElements().get(elementType);
        if (value != null) {
            return value;
        }
        
        // 检查派系元素
        value = elementData.getFactionElements().get(elementType);
        if (value != null) {
            return value;
        }
        
        // 检查复合元素
        value = elementData.getCombinedElements().get(elementType);
        if (value != null) {
            return value;
        }
        
        return 0.0;
    }
    
    /**
     * 从物品中读取暴击率值
     * @param stack 物品栈
     * @return 暴击率值，如果不存在则返回0.0
     */
    public static double readCriticalChance(ItemStack stack) {
        return readElementValue(stack, "critical_chance");
    }
    
    /**
     * 从物品中读取暴击伤害值
     * @param stack 物品栈
     * @return 暴击伤害值，如果不存在则返回0.0
     */
    public static double readCriticalDamage(ItemStack stack) {
        return readElementValue(stack, "critical_damage");
    }
    
    /**
     * 从物品中读取触发率值
     * @param stack 物品栈
     * @return 触发率值，如果不存在则返回0.0
     */
    public static double readTriggerChance(ItemStack stack) {
        return readElementValue(stack, "trigger_chance");
    }
    
    /**
     * 从物品中读取物理元素值
     * @param stack 物品栈
     * @param elementType 物理元素类型名称（impact, puncture, slash）
     * @return 物理元素值，如果不存在则返回0.0
     */
    public static double readPhysicalElementValue(ItemStack stack, String elementType) {
        if (!hasElementData(stack)) {
            return 0.0;
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        Double value = elementData.getPhysicalElements().get(elementType);
        return value != null ? value : 0.0;
    }
    
    /**
     * 从物品中读取基础元素值
     * @param stack 物品栈
     * @param elementType 基础元素类型名称（cold, electricity, heat, toxin）
     * @return 基础元素值，如果不存在则返回0.0
     */
    public static double readBasicElementValue(ItemStack stack, String elementType) {
        if (!hasElementData(stack)) {
            return 0.0;
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        Double value = elementData.getCombinedElements().get(elementType);
        return value != null ? value : 0.0;
    }
    
    /**
     * 从物品中读取复合元素值
     * @param stack 物品栈
     * @param elementType 复合元素类型名称（blast, corrosive, gas, magnetic, radiation, viral）
     * @return 复合元素值，如果不存在则返回0.0
     */
    public static double readCombinedElementValue(ItemStack stack, String elementType) {
        if (!hasElementData(stack)) {
            return 0.0;
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        Double value = elementData.getCombinedElements().get(elementType);
        return value != null ? value : 0.0;
    }
    
    /**
     * 从物品中读取派系元素值
     * @param stack 物品栈
     * @param elementType 派系元素类型名称（grineer, infested, corpus, orokin, sentient, murmum）
     * @return 派系元素值，如果不存在则返回0.0
     */
    public static double readFactionElementValue(ItemStack stack, String elementType) {
        if (!hasElementData(stack)) {
            return 0.0;
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        Double value = elementData.getFactionElements().get(elementType);
        return value != null ? value : 0.0;
    }
    
    /**
     * 获取物品中元素数据的总元素数量
     * @param stack 物品栈
     * @return 总元素数量
     */
    public static int getTotalElementCount(ItemStack stack) {
        if (!hasElementData(stack)) {
            return 0;
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        return elementData.getCriticalStats().size() + 
               elementData.getPhysicalElements().size() + 
               elementData.getFactionElements().size() + 
               elementData.getCombinedElements().size();
    }
    
    /**
     * 检查物品是否有非零的元素数据
     * @param stack 物品栈
     * @return 如果有任何非零元素数据返回true，否则返回false
     */
    public static boolean hasNonZeroElements(ItemStack stack) {
        if (!hasElementData(stack)) {
            return false;
        }
        
        ElementUsageData.ElementData elementData = readElementData(stack);
        
        // 检查暴击相关统计
        for (Double value : elementData.getCriticalStats().values()) {
            if (value != 0.0) return true;
        }
        
        // 检查物理元素
        for (Double value : elementData.getPhysicalElements().values()) {
            if (value != 0.0) return true;
        }
        
        // 检查派系元素
        for (Double value : elementData.getFactionElements().values()) {
            if (value != 0.0) return true;
        }
        
        // 检查复合元素
        for (Double value : elementData.getCombinedElements().values()) {
            if (value != 0.0) return true;
        }
        
        return false;
    }
}