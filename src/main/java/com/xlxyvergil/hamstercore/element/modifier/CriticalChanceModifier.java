package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;

import java.util.List;

/**
 * 暴击率Modifier
 * 专门处理暴击率的计算
 */
public class CriticalChanceModifier {
    
    public static final String CRITICAL_CHANCE = "critical_chance";
    
    /**
     * 计算暴击率
     * 从Basic层和Computed层获取暴击率数据，计算最终值，放入Usage层
     */
    public static void computeCriticalChance(WeaponElementData data) {
        
        double criticalChance = computeSingleCriticalChance(data);
        data.setUsageValue(CRITICAL_CHANCE, criticalChance);
        
    }
    
    /**
     * 计算暴击率的具体值
     */
    private static double computeSingleCriticalChance(WeaponElementData data) {
        double baseValue = 0.0;
        
        
        // 获取Basic层的暴击率值
        List<BasicEntry> basicEntries = data.getBasicElement(CRITICAL_CHANCE);
        if (!basicEntries.isEmpty()) {
            // 累加所有Basic层的暴击率值
            baseValue = basicEntries.stream()
                .mapToDouble(BasicEntry::getValue)
                .sum();
        } else {
        }
        
        // 应用Computed层的修正
        List<ComputedEntry> computedEntries = data.getComputedElement(CRITICAL_CHANCE);
        if (!computedEntries.isEmpty()) {
            // 应用所有Computed层的修正
            for (ComputedEntry computedEntry : computedEntries) {
                baseValue = applyModifier(baseValue, computedEntry);
            }
        }
        
        // 确保暴击率不低于0
        baseValue = Math.max(0.0, baseValue);
        
        return baseValue;
    }
    
    /**
     * 应用计算修正
     */
    private static double applyModifier(double baseValue, ComputedEntry computedEntry) {
        String operation = computedEntry.getOperation();
        double value = computedEntry.getValue();
        
        switch (operation) {
            case "add":
                return baseValue + value;
            case "sub":
                return baseValue - value;
            case "mul":
                return baseValue * value;
            case "div":
                return value != 0 ? baseValue / value : baseValue;
            default:
                return baseValue;
        }
    }
}