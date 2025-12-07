package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.element.impl.TriggerChanceAttribute;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 武器元素触发处理器
 * 负责计算武器元素伤害占比，并根据触发率计算元素触发概率
 */
public class WeaponTriggerHandler {
    
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();
    
    /**
     * 处理武器元素触发
     * @param stack 武器物品
     * @return 触发的元素类型列表，如果没有触发则返回空列表
     */
    public static List<ElementType> processWeaponTrigger(ItemStack stack) {
        List<ElementType> triggeredElements = new ArrayList<>();
        
        // 获取武器所有元素属性
        Map<ElementType, ElementInstance> elements = ElementHelper.getElementAttributes(stack);
        
        if (elements.isEmpty()) {
            return triggeredElements;
        }
        
        // 获取武器的触发率（可以超过100%）
        double triggerChance = getWeaponTriggerChance(stack, elements);
        
        // 如果武器没有触发率或触发率为0，则不会触发任何元素
        if (triggerChance <= 0) {
            return triggeredElements;
        }
        
        // 计算总伤害
        double totalDamage = calculateTotalDamage(elements);
        
        // 如果总伤害为0，则不会触发任何元素
        if (totalDamage <= 0) {
            return triggeredElements;
        }
        
        // 计算各元素的伤害占比
        Map<ElementType, Double> elementPercentages = calculateElementPercentages(elements, totalDamage);
        
        // 确定触发次数（整数部分是必定触发的次数，小数部分是概率触发）
        int guaranteedTriggers = (int) Math.floor(triggerChance);
        double probabilityTrigger = triggerChance - guaranteedTriggers;
        
        // 先处理必定触发的次数
        for (int i = 0; i < guaranteedTriggers; i++) {
            // 如果只有一个元素，直接触发
            if (elementPercentages.size() == 1) {
                ElementType onlyElement = elementPercentages.keySet().iterator().next();
                triggeredElements.add(onlyElement);
            } else {
                // 多个元素，根据占比选择一个
                ElementType selected = selectElementByPercentage(elementPercentages);
                if (selected != null) {
                    triggeredElements.add(selected);
                }
            }
        }
        
        // 处理概率触发的部分
        if (probabilityTrigger > 0 && RANDOM.nextDouble() <= probabilityTrigger) {
            // 如果只有一个元素，直接触发
            if (elementPercentages.size() == 1) {
                ElementType onlyElement = elementPercentages.keySet().iterator().next();
                triggeredElements.add(onlyElement);
            } else {
                // 多个元素，根据占比选择一个
                ElementType selected = selectElementByPercentage(elementPercentages);
                if (selected != null) {
                    triggeredElements.add(selected);
                }
            }
        }
        
        // 特殊情况：如果触发率超过100%且元素数量小于触发次数，可以让所有元素都触发
        if (triggerChance >= 1.0 && triggeredElements.size() < elementPercentages.size()) {
            // 计算完整触发次数
            int fullTriggerCount = (int) Math.floor(triggerChance);
            
            // 如果完整触发次数大于或等于元素数量，所有元素都触发
            if (fullTriggerCount >= elementPercentages.size()) {
                triggeredElements.clear();
                triggeredElements.addAll(elementPercentages.keySet());
            }
        }
        
        return triggeredElements;
    }
    
    /**
     * 获取武器的触发率
     * @param stack 武器物品
     * @param elements 元素属性
     * @return 触发率 (0.0 - 1.0)
     */
    public static double getWeaponTriggerChance(ItemStack stack, Map<ElementType, ElementInstance> elements) {
        // 检查是否有触发率属性
        if (elements.containsKey(ElementType.TRIGGER_CHANCE)) {
            ElementInstance triggerInstance = elements.get(ElementType.TRIGGER_CHANCE);
            return triggerInstance.value();
        }
        
        // 检查物品NBT中是否有触发率数据
        Map<ElementType, ElementInstance> elementAttributes = ElementHelper.getElementAttributes(stack);
        if (elementAttributes.containsKey(ElementType.TRIGGER_CHANCE)) {
            return elementAttributes.get(ElementType.TRIGGER_CHANCE).value();
        }
        
        // 默认情况下武器没有触发率
        return 0.0;
    }
    
    /**
     * 计算武器总伤害值
     * @param elements 元素属性
     * @return 总伤害值
     */
    public static double calculateTotalDamage(Map<ElementType, ElementInstance> elements) {
        double totalDamage = 0.0;
        
        for (Map.Entry<ElementType, ElementInstance> entry : elements.entrySet()) {
            ElementType type = entry.getKey();
            ElementInstance instance = entry.getValue();
            
            // 排除触发率属性，它不参与伤害计算
            if (type == ElementType.TRIGGER_CHANCE || type == ElementType.CRITICAL_CHANCE || type == ElementType.CRITICAL_DAMAGE) {
                continue;
            }
            
            totalDamage += instance.value();
        }
        
        return totalDamage;
    }
    
    /**
     * 计算各元素在总伤害中的占比
     * @param elements 元素属性
     * @param totalDamage 总伤害值
     * @return 元素类型及其占比的映射
     */
    public static Map<ElementType, Double> calculateElementPercentages(Map<ElementType, ElementInstance> elements, double totalDamage) {
        java.util.Map<ElementType, Double> percentages = new java.util.HashMap<>();
        
        for (Map.Entry<ElementType, ElementInstance> entry : elements.entrySet()) {
            ElementType type = entry.getKey();
            ElementInstance instance = entry.getValue();
            
            // 排除特殊属性
            if (type == ElementType.TRIGGER_CHANCE || type == ElementType.CRITICAL_CHANCE || type == ElementType.CRITICAL_DAMAGE) {
                continue;
            }
            
            double percentage = instance.value() / totalDamage;
            percentages.put(type, percentage);
        }
        
        return percentages;
    }
    
    /**
     * 根据占比随机选择一个元素
     * @param elementPercentages 元素类型及其占比的映射
     * @return 选择的元素类型
     */
    private static ElementType selectElementByPercentage(Map<ElementType, Double> elementPercentages) {
        double randomValue = RANDOM.nextDouble();
        double currentSum = 0.0;
        
        for (Map.Entry<ElementType, Double> entry : elementPercentages.entrySet()) {
            currentSum += entry.getValue();
            if (randomValue <= currentSum) {
                return entry.getKey();
            }
        }
        
        // 如果由于浮点精度问题没有选中，返回最后一个元素
        if (!elementPercentages.isEmpty()) {
            List<ElementType> types = new ArrayList<>(elementPercentages.keySet());
            return types.get(types.size() - 1);
        }
        
        return null;
    }
    
    /**
     * 设置武器的触发率
     * @param stack 武器物品
     * @param triggerChance 触发率 (0.0 - 1.0)
     */
    public static void setWeaponTriggerChance(ItemStack stack, double triggerChance) {
        // 获取当前的元素属性
        Map<ElementType, ElementInstance> elements = ElementHelper.getElementAttributes(stack);
        
        // 创建或更新触发率属性
        TriggerChanceAttribute triggerAttribute = (TriggerChanceAttribute) ElementRegistry.getAttribute(ElementType.TRIGGER_CHANCE);
        if (triggerAttribute != null) {
            ElementInstance triggerInstance = new ElementInstance(triggerAttribute, triggerChance);
            elements.put(ElementType.TRIGGER_CHANCE, triggerInstance);
            ElementHelper.setElementAttributes(stack, elements);
        }
    }
    
    /**
     * 获取武器各元素的触发概率
     * @param stack 武器物品
     * @return 元素类型及其触发概率的映射
     */
    public static Map<ElementType, Double> getWeaponElementTriggerProbabilities(ItemStack stack) {
        Map<ElementType, ElementInstance> elements = ElementHelper.getElementAttributes(stack);
        
        if (elements.isEmpty()) {
            return new java.util.HashMap<>();
        }
        
        // 获取武器的触发率
        double triggerChance = getWeaponTriggerChance(stack, elements);
        
        // 如果武器没有触发率或触发率为0，则所有元素的触发概率为0
        if (triggerChance <= 0) {
            Map<ElementType, Double> probabilities = new java.util.HashMap<>();
            for (ElementType type : elements.keySet()) {
                if (type != ElementType.TRIGGER_CHANCE && type != ElementType.CRITICAL_CHANCE && type != ElementType.CRITICAL_DAMAGE) {
                    probabilities.put(type, 0.0);
                }
            }
            return probabilities;
        }
        
        // 计算总伤害
        double totalDamage = calculateTotalDamage(elements);
        
        // 如果总伤害为0，则所有元素的触发概率为0
        if (totalDamage <= 0) {
            Map<ElementType, Double> probabilities = new java.util.HashMap<>();
            for (ElementType type : elements.keySet()) {
                if (type != ElementType.TRIGGER_CHANCE && type != ElementType.CRITICAL_CHANCE && type != ElementType.CRITICAL_DAMAGE) {
                    probabilities.put(type, 0.0);
                }
            }
            return probabilities;
        }
        
        // 计算各元素的伤害占比
        Map<ElementType, Double> elementPercentages = calculateElementPercentages(elements, totalDamage);
        
        // 元素的最终触发概率 = 武器触发率 × 元素伤害占比
        Map<ElementType, Double> triggerProbabilities = new java.util.HashMap<>();
        for (Map.Entry<ElementType, Double> entry : elementPercentages.entrySet()) {
            triggerProbabilities.put(entry.getKey(), entry.getValue() * triggerChance);
        }
        
        return triggerProbabilities;
    }
}