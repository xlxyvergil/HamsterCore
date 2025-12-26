package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 添加ElementUsageData的导入，用于存储元素属性的NBT数据
import com.xlxyvergil.hamstercore.element.ElementUsageData;

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
     * 类似Apotheosis的词缀系统，将元素数据直接写入物品NBT
     * @param stack 物品栈
     * @param weaponData 武器数据
     */
    public void calculateAndStoreElements(ItemStack stack, WeaponData weaponData) {
        // 1. 调用ElementCalculator从InitialModifiers层计算所有元素值
        Map<String, Double> elementValues = calculateElementValuesFromInitialModifiers(weaponData);
        
        // 2. 分离物理元素、基础元素、复合元素、派系元素和特殊元素
        Map<String, Double> physicalElements = new HashMap<>();
        Map<String, Double> basicElements = new HashMap<>();
        Map<String, Double> complexElements = new HashMap<>();
        Map<String, Double> factionElements = new HashMap<>();
        Map<String, Double> specialStats = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : elementValues.entrySet()) {
            String elementType = entry.getKey();
            double value = entry.getValue();
            
            ElementType type = ElementType.byName(elementType);
            if (type != null) {
                if (type.isPhysical()) {
                    physicalElements.put(elementType, value);
                } else if (type.isBasic()) {
                    basicElements.put(elementType, value);
                } else if (type.isComplex()) {
                    complexElements.put(elementType, value);
                } else if (type.isSpecial()) {
                    factionElements.put(elementType, value);
                } else if (type.isCriticalChance() || type.isCriticalDamage() || type.isTriggerChance()) {
                    specialStats.put(elementType, value);
                }
            }
        }
        
        // 3. 合并基础元素和复合元素，用于处理元素复合
        Map<String, Double> elementsForCombination = new HashMap<>();
        elementsForCombination.putAll(basicElements);
        elementsForCombination.putAll(complexElements);
        
        // 4. 调用ElementCombinationModifier处理元素复合
        Map<String, Double> combinedElements = processElementCombinations(weaponData, elementsForCombination);
        
        // 5. 将计算结果存储到物品的NBT中，类似Apotheosis的词缀系统
        ElementUsageData.ElementData elementData = new ElementUsageData.ElementData();
        elementData.setCriticalStats(specialStats);
        elementData.setPhysicalElements(physicalElements);
        elementData.setFactionElements(factionElements);
        elementData.setCombinedElements(combinedElements);
        
        ElementUsageData.writeElementDataToItem(stack, elementData);
    }
    
    /**
     * 从InitialModifiers层计算元素值
     * @param weaponData 武器数据
     * @return 元素值映射
     */
    private Map<String, Double> calculateElementValuesFromInitialModifiers(WeaponData weaponData) {
        Map<String, Double> elementValues = new HashMap<>();
        
        List<InitialModifierEntry> initialModifiers = weaponData.getInitialModifiers();
        
        for (InitialModifierEntry entry : initialModifiers) {
            String elementType = entry.getElementType();
            double amount = entry.getAmount();
            
            // 累加同类型元素的值
            elementValues.put(elementType, elementValues.getOrDefault(elementType, 0.0) + amount);
        }
        
        return elementValues;
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