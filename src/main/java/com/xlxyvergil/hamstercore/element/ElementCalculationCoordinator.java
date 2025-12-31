package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// 添加ElementUsageData的导入，用于存储元素属性的NBT数据

/**
 * 元素计算协调器
 * 协调元素计算流程，负责：
 * 1. 调用ElementCalculator从InitialModifiers层获取并计算数据
 * 2. 调用ElementCombinationModifier处理元素复合
 * 3. 将计算结果存储到物品的NBT中，类似Apotheosis的词缀系统
 */
public class ElementCalculationCoordinator {
    public static final ElementCalculationCoordinator INSTANCE = new ElementCalculationCoordinator();
    
    private ElementCalculationCoordinator() {
    }
    
    /**
     * 计算元素值并将其存储到物品的NBT中
     * 支持HamsterCore元素和其他通用属性
     * @param stack 物品栈
     * @param weaponData 武器数据
     */
    public void calculateAndStoreElements(ItemStack stack, WeaponData weaponData) {
        // 1. 调用ElementCalculator从InitialModifiers层计算所有元素值（包括通用属性）
        Map<String, Double> elementValues = ElementCalculator.INSTANCE.calculateElementValuesFromInitialModifiers(weaponData);
        
        // 2. 分离需要复合计算的元素和其他通用属性
        Map<String, Double> elementsForCombination = new HashMap<>();
        Map<String, Double> otherAttributes = new HashMap<>(); // 存储其他通用属性（包括HamsterCore的非复合元素和其他mod属性）
        
        for (Map.Entry<String, Double> entry : elementValues.entrySet()) {
            String attributeName = entry.getKey();
            double value = entry.getValue();
            
            ElementType type = ElementType.byName(attributeName);
            if (type != null && (type.isBasic() || type.isComplex())) {
                // 需要复合计算的元素
                elementsForCombination.put(attributeName, value);
            } else {
                // 其他所有属性（包括HamsterCore的非复合元素和其他mod属性）统一存储
                otherAttributes.put(attributeName, value);
            }
        }
        
        // 3. 使用已分离的需要复合计算的元素
        
        // 4. 调用ElementCombinationModifier处理HamsterCore元素复合
        Map<String, Double> combinedElements = processElementCombinations(weaponData, elementsForCombination);
        
        // 5. 创建属性修饰符条目列表，准备存储到物品NBT中
        List<ElementUsageData.AttributeModifierEntry> modifierEntries = new ArrayList<>();
        
        // 6. 处理所有InitialModifiers，创建完整的属性修饰符条目
        for (InitialModifierEntry entry : weaponData.getInitialModifiers()) {
            String attributeName = entry.getName();
            String elementType = entry.getElementType();
            String source = entry.getSource();
            UUID uuid = entry.getUuid();
            String operation = entry.getOperation();
            
            // 获取计算后的数值
            double amount;
            boolean isFromCombinedElements = false;
            
            if (combinedElements.containsKey(attributeName)) {
                // 如果是复合元素或基础元素，使用复合后的数值
                amount = combinedElements.get(attributeName);
                isFromCombinedElements = true;
            } else if (otherAttributes.containsKey(attributeName)) {
                // 如果是其他属性，使用计算后的数值
                amount = otherAttributes.get(attributeName);
            } else {
                // 如果没有计算结果，使用原始数值
                amount = entry.getAmount();
            }
            
            // 只有经过复合处理的元素才需要确保elementType包含命名空间
            if (isFromCombinedElements) {
                // 检查是否是HamsterCore的元素
                ElementType hamsterElementType = ElementType.byName(attributeName);
                if (hamsterElementType != null && !elementType.contains(":")) {
                    // 如果是HamsterCore的元素且没有命名空间，添加hamstercore:前缀
                    elementType = "hamstercore:" + elementType;
                }
            }
            
            // 创建属性修饰符条目
            ElementUsageData.AttributeModifierEntry modifierEntry = new ElementUsageData.AttributeModifierEntry(
                attributeName,
                elementType,
                amount,
                operation,
                uuid,
                source
            );
            
            modifierEntries.add(modifierEntry);
        }
        
        // 7. 将完整的属性修饰符数据存储到物品的NBT中
        ElementUsageData.writeElementDataToItem(stack, modifierEntries);
    }
    
    /**
     * 处理元素复合
     * @param weaponData 武器数据
     * @param elementValues 元素值映射
     * @return 复合后的元素值映射
     */
    private Map<String, Double> processElementCombinations(WeaponData weaponData, Map<String, Double> elementValues) {
        // 创建一个临时的Map来存储复合结果
        Map<String, Double> tempValues = new HashMap<>(elementValues);
        
        // 调用ElementCombinationModifier处理元素复合，并直接使用返回的结果
        return ElementCombinationModifier.computeElementCombinationsWithValues(weaponData, tempValues);
    }
    

}