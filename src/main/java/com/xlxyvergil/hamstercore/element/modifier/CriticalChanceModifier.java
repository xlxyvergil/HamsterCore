package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;
import com.xlxyvergil.hamstercore.util.DebugLogger;

/**
 * 暴击率Modifier
 * 专门处理暴击率的计算
 */
public class CriticalChanceModifier {
    
    public static final String CRITICAL_CHANCE = "criticalChance";
    
    /**
     * 计算暴击率
     * 从Basic层和Computed层获取暴击率数据，计算最终值，放入Usage层
     */
    public static void computeCriticalChance(WeaponElementData data) {
        DebugLogger.log("开始计算暴击率...");
        
        double criticalChance = computeSingleCriticalChance(data);
        data.setUsageValue(CRITICAL_CHANCE, criticalChance);
        
        DebugLogger.log("暴击率计算完成: %.3f (%.1f%%)", criticalChance, criticalChance * 100);
    }
    
    /**
     * 计算暴击率的具体值
     */
    private static double computeSingleCriticalChance(WeaponElementData data) {
        double baseValue = 0.0;
        
        // 获取Basic层的暴击率值
        BasicEntry basicEntry = data.getBasicElement(CRITICAL_CHANCE);
        if (basicEntry != null) {
            baseValue = basicEntry.getValue();
        }
        
        // 应用Computed层的修正
        ComputedEntry computedEntry = data.getComputedElement(CRITICAL_CHANCE);
        if (computedEntry != null) {
            baseValue = applyModifier(baseValue, computedEntry);
        }
        
        // 如果Basic和Computed层都没有值，检查是否使用Computed层的独有值
        if (baseValue == 0.0 && computedEntry != null) {
            baseValue = computedEntry.getValue();
        }
        
        // 确保暴击率在合理范围内 (0.0 - 1.0)
        baseValue = Math.max(0.0, Math.min(1.0, baseValue));
        
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
                DebugLogger.log("未知的计算操作: %s", operation);
                return baseValue;
        }
    }
}