package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;

import java.util.List;

/**
 * 触发率Modifier
 * 专门处理触发率的计算
 */
public class TriggerChanceModifier {
    
    public static final String TRIGGER_CHANCE = "trigger_chance";
    
    /**
     * 计算触发率
     * 从Basic层和Computed层获取触发率数据，计算最终值，放入Usage层
     */
    public static void computeTriggerChance(WeaponElementData data) {
        
        double triggerChance = computeSingleTriggerChance(data);
        data.setUsageValue(TRIGGER_CHANCE, triggerChance);
        
    }
    
    /**
     * 计算触发率的具体值
     */
    private static double computeSingleTriggerChance(WeaponElementData data) {
        double baseValue = 0.0;
        
        
        // 获取Basic层的触发率值
        List<BasicEntry> basicEntries = data.getBasicElement(TRIGGER_CHANCE);
        if (!basicEntries.isEmpty()) {
            // 累加所有Basic层的触发率值
            baseValue = basicEntries.stream()
                .mapToDouble(BasicEntry::getValue)
                .sum();
        }
        
        // 应用Computed层的修正
        List<ComputedEntry> computedEntries = data.getComputedElement(TRIGGER_CHANCE);
        if (!computedEntries.isEmpty()) {
            // 应用所有Computed层的修正
            for (ComputedEntry computedEntry : computedEntries) {
                baseValue = applyModifier(baseValue, computedEntry);
            }
        }
        
        // 确保触发率不低于0
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