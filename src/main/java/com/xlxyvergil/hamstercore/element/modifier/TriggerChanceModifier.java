package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;
import com.xlxyvergil.hamstercore.util.DebugLogger;

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
        DebugLogger.log("开始计算触发率...");
        
        double triggerChance = computeSingleTriggerChance(data);
        data.setUsageValue(TRIGGER_CHANCE, triggerChance);
        
        DebugLogger.log("触发率计算完成: %.3f (%.1f%%)", triggerChance, triggerChance * 100);
    }
    
    /**
     * 计算触发率的具体值
     */
    private static double computeSingleTriggerChance(WeaponElementData data) {
        double baseValue = 0.0;
        
        DebugLogger.log("正在计算触发率");
        
        // 获取Basic层的触发率值
        BasicEntry basicEntry = data.getBasicElement(TRIGGER_CHANCE);
        if (basicEntry != null) {
            baseValue = basicEntry.getValue();
            DebugLogger.log("从Basic层获取到触发率值: %.3f", baseValue);
        } else {
            DebugLogger.log("在Basic层未找到触发率数据");
        }
        
        // 应用Computed层的修正
        ComputedEntry computedEntry = data.getComputedElement(TRIGGER_CHANCE);
        if (computedEntry != null) {
            baseValue = applyModifier(baseValue, computedEntry);
            DebugLogger.log("应用Computed层修正后触发率值: %.3f", baseValue);
        }
        
        // 如果Basic和Computed层都没有值，检查是否使用Computed层的独有值
        if (baseValue == 0.0 && computedEntry != null) {
            baseValue = computedEntry.getValue();
            DebugLogger.log("使用Computed层独有触发率值: %.3f", baseValue);
        }
        
        // 确保触发率在合理范围内 (0.0 - 1.0)
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