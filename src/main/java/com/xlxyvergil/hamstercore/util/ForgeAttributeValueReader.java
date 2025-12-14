package com.xlxyvergil.hamstercore.util;

import com.google.common.collect.Multimap;
import com.xlxyvergil.hamstercore.element.ElementAttribute;
import com.xlxyvergil.hamstercore.element.ElementRegistry;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Forge属性数值读取工具类
 * 负责读取经过Forge属性系统计算后的实际元素修饰符数值
 */
public class ForgeAttributeValueReader {
    
    /**
     * 从物品的属性修饰符中获取指定元素类型的实际数值
     * 采用 GunsmithLib 的方式：手动重建Forge的计算逻辑
     * 这个方法用于分析物品本身提供的属性，不需要玩家实际装备
     * @param stack 物品堆
     * @param elementType 元素类型
     * @return 经过Forge计算后的修饰符值，如果没有则返回0.0
     */
    public static double getElementValueFromItem(ItemStack stack, ElementType elementType) {
        if (stack == null || elementType == null) {
            return 0.0;
        }
        
        try {
            // 获取对应的元素属性
            ElementAttribute elementAttribute = ElementRegistry.getAttribute(elementType);
            if (elementAttribute == null) {
                return 0.0;
            }
            
            // GunsmithLib 方式：固定使用主手槽位获取修饰符
            // 武器通常在主手，这是最合理的默认选择
            var modifiers = stack.getAttributeModifiers(EquipmentSlot.MAINHAND);
            if (modifiers == null || modifiers.isEmpty()) {
                return 0.0;
            }
            
            // 获取特定元素属性的所有修饰符
            var attributeModifiers = modifiers.get(elementAttribute);
            if (attributeModifiers == null || attributeModifiers.isEmpty()) {
                return 0.0;
            }
            
            // GunsmithLib 的完整 Forge 计算逻辑
            double baseValue = elementAttribute.getDefaultValue();
            double additionSum = 0.0;
            double multiplyBaseSum = 0.0;
            double multiplyTotalProduct = 1.0;
            
            // 分类处理不同类型的修饰符
            for (AttributeModifier modifier : attributeModifiers) {
                switch (modifier.getOperation()) {
                    case ADDITION:
                        additionSum += modifier.getAmount();
                        break;
                    case MULTIPLY_BASE:
                        multiplyBaseSum += modifier.getAmount();
                        break;
                    case MULTIPLY_TOTAL:
                        multiplyTotalProduct *= (1.0 + modifier.getAmount());
                        break;
                }
            }
            
            // 按照 Forge 标准计算顺序：基础值 + 加法 + (基础值 × 基础乘法) × 总值乘法
            return baseValue + additionSum + (baseValue * multiplyBaseSum) * multiplyTotalProduct;
            
        } catch (Exception e) {
            System.err.println("Error reading element value from item: " + e.getMessage());
            return 0.0;
        }
    }
    

    
    /**
     * 获取物品上所有特殊元素和派系元素的Forge计算值
     * @param stack 物品堆
     * @return Map<元素类型名称, 计算后的值>，只包含特殊元素和派系元素
     */
    public static Map<String, Double> getAllSpecialAndFactionValues(ItemStack stack) {
        Map<String, Double> result = new HashMap<>();
        
        if (stack == null) {
            return result;
        }
        
        // 定义需要获取的特殊元素和派系元素类型
        String[] specialAndFactionTypes = {
            "critical_chance", "critical_damage", "trigger_chance",  // 特殊元素
            "grineer", "infested", "corpus", "orokin", "sentient", "murmur"  // 派系元素
        };
        
        // 遍历所有特殊元素和派系元素类型
        for (String elementTypeName : specialAndFactionTypes) {
            ElementType elementType = ElementType.byName(elementTypeName);
            if (elementType != null) {
                double value = getElementValueFromItem(stack, elementType);
                if (value > 0) {
                    result.put(elementTypeName, value);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取物品上所有基础元素的Forge计算值
     * @param stack 物品堆
     * @return Map<元素类型名称, 计算后的值>，只包含基础元素
     */
    public static Map<String, Double> getAllBasicElementValues(ItemStack stack) {
        Map<String, Double> result = new HashMap<>();
        
        if (stack == null) {
            return result;
        }
        
        // 遍历所有基础元素类型
        for (ElementType elementType : ElementType.getAllTypes()) {
            if (elementType.isBasic()) {
                double value = getElementValueFromItem(stack, elementType);
                if (value > 0) {
                    result.put(elementType.getName(), value);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取物品上所有复合元素的Forge计算值
     * @param stack 物品堆
     * @return Map<元素类型名称, 计算后的值>，只包含复合元素
     */
    public static Map<String, Double> getAllComplexElementValues(ItemStack stack) {
        Map<String, Double> result = new HashMap<>();
        
        if (stack == null) {
            return result;
        }
        
        // 遍历所有复合元素类型
        for (ElementType elementType : ElementType.getAllTypes()) {
            if (elementType.isComplex()) {
                double value = getElementValueFromItem(stack, elementType);
                if (value > 0) {
                    result.put(elementType.getName(), value);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取物品上所有元素的分类Forge计算值
     * @param stack 物品堆
     * @return 包含所有分类的完整元素数据
     */
    public static ElementCategoryData getAllElementValuesByCategory(ItemStack stack) {
        if (stack == null) {
            return new ElementCategoryData();
        }
        
        Map<String, Double> specialValues = new HashMap<>();
        Map<String, Double> factionValues = new HashMap<>();
        Map<String, Double> basicValues = new HashMap<>();
        Map<String, Double> complexValues = new HashMap<>();
        
        // 遍历所有元素类型进行分类计算
        for (ElementType elementType : ElementType.getAllTypes()) {
            double value = getElementValueFromItem(stack, elementType);
            if (value <= 0) {
                continue;
            }
            
            String elementName = elementType.getName();
            
            // 根据元素类型进行分类
            if (elementType.isSpecial()) {
                if (elementName.equals("critical_chance") || elementName.equals("critical_damage") || elementName.equals("trigger_chance")) {
                    specialValues.put(elementName, value);
                } else {
                    // 其他特殊元素归类为派系元素
                    factionValues.put(elementName, value);
                }
            } else if (elementType.isBasic()) {
                basicValues.put(elementName, value);
            } else if (elementType.isComplex()) {
                complexValues.put(elementName, value);
            }
        }
        
        return new ElementCategoryData(specialValues, factionValues, basicValues, complexValues);
    }
    
    /**
     * 元素分类数据容器
     */
    public static class ElementCategoryData {
        private final Map<String, Double> specialValues;    // 特殊元素
        private final Map<String, Double> factionValues;    // 派系元素
        private final Map<String, Double> basicValues;      // 基础元素
        private final Map<String, Double> complexValues;    // 复合元素
        
        public ElementCategoryData() {
            this(new HashMap<>(), new HashMap<>(), new HashMap<>(), new HashMap<>());
        }
        
        public ElementCategoryData(Map<String, Double> specialValues, Map<String, Double> factionValues, 
                                 Map<String, Double> basicValues, Map<String, Double> complexValues) {
            this.specialValues = specialValues;
            this.factionValues = factionValues;
            this.basicValues = basicValues;
            this.complexValues = complexValues;
        }
        
        // Getter方法
        public Map<String, Double> getSpecialValues() { return specialValues; }
        public Map<String, Double> getFactionValues() { return factionValues; }
        public Map<String, Double> getBasicValues() { return basicValues; }
        public Map<String, Double> getComplexValues() { return complexValues; }
        
        /**
         * 检查是否有任何数据
         */
        public boolean hasAnyData() {
            return !specialValues.isEmpty() || !factionValues.isEmpty() || 
                   !basicValues.isEmpty() || !complexValues.isEmpty();
        }
        
        /**
         * 检查是否有特殊元素数据
         */
        public boolean hasSpecialData() {
            return !specialValues.isEmpty();
        }
        
        /**
         * 检查是否有派系元素数据
         */
        public boolean hasFactionData() {
            return !factionValues.isEmpty();
        }
        
        /**
         * 检查是否有基础元素数据
         */
        public boolean hasBasicData() {
            return !basicValues.isEmpty();
        }
        
        /**
         * 检查是否有复合元素数据
         */
        public boolean hasComplexData() {
            return !complexValues.isEmpty();
        }
        
        /**
         * 获取基础元素和复合元素的合并数据
         */
        public Map<String, Double> getBasicAndComplexValues() {
            Map<String, Double> combined = new HashMap<>(basicValues);
            combined.putAll(complexValues);
            return combined;
        }
    }
}