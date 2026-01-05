package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.element.ElementUsageData;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
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
     * 从物品中读取物理元素数据
     * @param stack 物品栈
     * @return 物理元素数据映射
     */
    public static Map<String, Double> readPhysicalElements(ItemStack stack) {
        Map<String, Double> elements = new HashMap<>();
        Map<String, Double> allElements = readAllElementValues(stack);
        
        // 物理元素类型
        String[] physicalTypes = {"impact", "puncture", "slash"};
        for (String type : physicalTypes) {
            // 检查完整匹配或后缀匹配
            for (Map.Entry<String, Double> entry : allElements.entrySet()) {
                if (entry.getKey().equals(type) || entry.getKey().endsWith(":" + type)) {
                    elements.put(type, entry.getValue());
                    break;
                }
            }
        }
        
        return elements;
    }
    
    /**
     * 从物品中读取复合元素数据
     * @param stack 物品栈
     * @return 复合元素数据映射
     */
    public static Map<String, Double> readCombinedElements(ItemStack stack) {
        Map<String, Double> elements = new HashMap<>();
        Map<String, Double> allElements = readAllElementValues(stack);
        
        // 复合元素类型
        String[] combinedTypes = {"blast", "corrosive", "gas", "magnetic", "radiation", "viral"};
        for (String type : combinedTypes) {
            // 检查完整匹配或后缀匹配
            for (Map.Entry<String, Double> entry : allElements.entrySet()) {
                if (entry.getKey().equals(type) || entry.getKey().endsWith(":" + type)) {
                    elements.put(type, entry.getValue());
                    break;
                }
            }
        }
        
        // 基础元素类型
        String[] basicTypes = {"cold", "electricity", "heat", "toxin"};
        for (String type : basicTypes) {
            // 检查完整匹配或后缀匹配
            for (Map.Entry<String, Double> entry : allElements.entrySet()) {
                if (entry.getKey().equals(type) || entry.getKey().endsWith(":" + type)) {
                    elements.put(type, entry.getValue());
                    break;
                }
            }
        }
        
        return elements;
    }
    
    /**
     * 从物品中读取派系元素数据
     * @param stack 物品栈
     * @return 派系元素数据映射
     */
    public static Map<String, Double> readFactionElements(ItemStack stack) {
        Map<String, Double> elements = new HashMap<>();
        Map<String, Double> allElements = readAllElementValues(stack);
        
        // 派系元素类型
        String[] factionTypes = {"grineer", "infested", "corpus", "orokin", "sentient", "murmum"};
        for (String type : factionTypes) {
            // 检查完整匹配或后缀匹配
            for (Map.Entry<String, Double> entry : allElements.entrySet()) {
                if (entry.getKey().equals(type) || entry.getKey().endsWith(":" + type)) {
                    elements.put(type, entry.getValue());
                    break;
                }
            }
        }
        
        return elements;
    }
    
    /**
     * 从物品中读取暴击相关统计数据
     * @param stack 物品栈
     * @return 暴击相关统计数据映射
     */
    public static Map<String, Double> readCriticalStats(ItemStack stack) {
        Map<String, Double> stats = new HashMap<>();
        Map<String, Double> allElements = readAllElementValues(stack);
        
        // 暴击相关统计
        String[] criticalTypes = {"crit_chance", "crit_damage", "trigger_chance"};
        for (String type : criticalTypes) {
            // 检查完整匹配或后缀匹配
            for (Map.Entry<String, Double> entry : allElements.entrySet()) {
                if (entry.getKey().equals(type) || entry.getKey().endsWith(":" + type)) {
                    stats.put(type, entry.getValue());
                    break;
                }
            }
        }
        
        return stats;
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
        
        List<ElementUsageData.AttributeModifierEntry> entries = ElementUsageData.readElementDataFromItem(stack);
        for (ElementUsageData.AttributeModifierEntry entry : entries) {
            // 检查elementType是否完全匹配
            if (entry.getElementType().equals(elementType)) {
                return entry.getAmount();
            }
            // 检查elementType的后缀是否匹配（支持直接使用属性名称匹配）
            if (entry.getElementType().endsWith(":" + elementType)) {
                return entry.getAmount();
            }
        }
        
        return 0.0;
    }
    
    /**
     * 从物品中读取暴击率值
     * @param stack 物品栈
     * @return 暴击率值，如果不存在则返回0.0
     */
    public static double readCriticalChance(ItemStack stack) {
        return readElementValue(stack, "crit_chance");
    }
    
    /**
     * 从物品中读取暴击伤害值
     * @param stack 物品栈
     * @return 暴击伤害值，如果不存在则返回0.0
     */
    public static double readCriticalDamage(ItemStack stack) {
        return readElementValue(stack, "crit_damage");
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
        return readElementValue(stack, elementType);
    }
    
    /**
     * 从物品中读取基础元素值
     * @param stack 物品栈
     * @param elementType 基础元素类型名称（cold, electricity, heat, toxin）
     * @return 基础元素值，如果不存在则返回0.0
     */
    public static double readBasicElementValue(ItemStack stack, String elementType) {
        return readElementValue(stack, elementType);
    }
    
    /**
     * 从物品中读取复合元素值
     * @param stack 物品栈
     * @param elementType 复合元素类型名称（blast, corrosive, gas, magnetic, radiation, viral）
     * @return 复合元素值，如果不存在则返回0.0
     */
    public static double readCombinedElementValue(ItemStack stack, String elementType) {
        return readElementValue(stack, elementType);
    }
    
    /**
     * 从物品中读取派系元素值
     * @param stack 物品栈
     * @param elementType 派系元素类型名称（grineer, infested, corpus, orokin, sentient, murmum）
     * @return 派系元素值，如果不存在则返回0.0
     */
    public static double readFactionElementValue(ItemStack stack, String elementType) {
        return readElementValue(stack, elementType);
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
        
        List<ElementUsageData.AttributeModifierEntry> entries = ElementUsageData.readElementDataFromItem(stack);
        return entries.size();
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
        
        List<ElementUsageData.AttributeModifierEntry> entries = ElementUsageData.readElementDataFromItem(stack);
        for (ElementUsageData.AttributeModifierEntry entry : entries) {
            if (entry.getAmount() != 0.0) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 从物品中读取所有元素数据
     * @param stack 物品栈
     * @return 元素数据映射
     */
    public static Map<String, Double> readAllElementValues(ItemStack stack) {
        Map<String, Double> values = new HashMap<>();
        
        if (!hasElementData(stack)) {
            return values;
        }
        
        List<ElementUsageData.AttributeModifierEntry> entries = ElementUsageData.readElementDataFromItem(stack);
        for (ElementUsageData.AttributeModifierEntry entry : entries) {
            values.put(entry.getElementType(), entry.getAmount());
        }
        
        return values;
    }
}