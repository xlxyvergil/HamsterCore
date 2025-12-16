package com.xlxyvergil.hamstercore.element;

import com.xlxyvergil.hamstercore.element.modifier.ElementCombinationModifier;
import com.xlxyvergil.hamstercore.handler.AffixCacheManager;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 元素计算协调器
 * 协调元素计算流程，负责：
 * 1. 调用ElementCalculator从InitialModifiers层获取并计算数据
 * 2. 调用ElementCombinationModifier处理元素复合
 * 3. 将计算结果缓存到AffixCacheManager中
 */
public class ElementCalculationCoordinator {
    public static final ElementCalculationCoordinator INSTANCE = new ElementCalculationCoordinator();
    
    private ElementCalculationCoordinator() {
    }
    
    /**
     * 计算元素值并缓存结果
     * @param stack 物品栈
     * @param weaponData 武器数据
     */
    public void calculateAndCacheElements(ItemStack stack, WeaponData weaponData) {
        // 1. 调用ElementCalculator从InitialModifiers层计算元素值
        Map<String, Double> elementValues = calculateElementValuesFromInitialModifiers(weaponData);
        
        // 2. 调用ElementCombinationModifier处理元素复合
        Map<String, Double> combinedElements = processElementCombinations(weaponData, elementValues);
        
        // 3. 计算特殊元素值（暴击率、暴击伤害、触发率等）
        Map<String, Double> specialStats = calculateSpecialStats(weaponData);
        
        // 4. 分离物理元素和派系元素
        Map<String, Double> physicalElements = new HashMap<>();
        Map<String, Double> factionElements = new HashMap<>();
        
        for (Map.Entry<String, Double> entry : combinedElements.entrySet()) {
            String elementType = entry.getKey();
            double value = entry.getValue();
            
            ElementType type = ElementType.byName(elementType);
            if (type != null) {
                if (type.isBasic() || type.isComplex()) {
                    physicalElements.put(elementType, value);
                } else if (type.isSpecial()) {
                    factionElements.put(elementType, value);
                }
            }
        }
        
        // 5. 将计算结果缓存到AffixCacheManager中
        AffixCacheManager.AffixCacheData cacheData = AffixCacheManager.getOrCreateCache(stack);
        cacheData.setCriticalStats(specialStats);
        cacheData.setPhysicalElements(physicalElements);
        cacheData.setFactionElements(factionElements);
        cacheData.setCombinedElements(combinedElements);
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
    
    /**
     * 计算特殊元素值（暴击率、暴击伤害、触发率等）
     * @param weaponData 武器数据
     * @return 特殊元素值映射
     */
    private Map<String, Double> calculateSpecialStats(WeaponData weaponData) {
        Map<String, Double> specialStats = new HashMap<>();
        
        List<InitialModifierEntry> initialModifiers = weaponData.getInitialModifiers();
        
        for (InitialModifierEntry entry : initialModifiers) {
            String elementType = entry.getElementType();
            double amount = entry.getAmount();
            
            // 检查是否为特殊元素类型
            switch (elementType) {
                case "critical_rate":
                case "critical_damage":
                case "trigger_rate":
                    specialStats.put(elementType, specialStats.getOrDefault(elementType, 0.0) + amount);
                    break;
            }
        }
        
        return specialStats;
    }
}