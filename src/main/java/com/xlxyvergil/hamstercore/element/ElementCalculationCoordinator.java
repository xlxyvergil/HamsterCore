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
        
        // 5. 将计算结果存储到物品的NBT中，包括通用属性
        ElementUsageData.ElementData elementData = new ElementUsageData.ElementData();
        
        // 将其他通用属性存储到物理元素映射中（包括HamsterCore的非复合元素和其他mod属性，现作为通用属性存储）
        elementData.setPhysicalElements(otherAttributes);
        
        // 将复合后的元素存储到组合元素中
        elementData.setCombinedElements(combinedElements);
        
        ElementUsageData.writeElementDataToItem(stack, elementData);
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