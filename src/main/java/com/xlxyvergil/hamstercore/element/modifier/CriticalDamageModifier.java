package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;
import com.xlxyvergil.hamstercore.util.DebugLogger;

/**
 * 暴击伤害Modifier
 * 专门处理暴击伤害的计算
 */
public class CriticalDamageModifier {
    
    public static final String CRITICAL_DAMAGE = "critical_damage";
    
    /**
     * 计算暴击伤害
     * 从Basic层和Computed层获取暴击伤害数据，计算最终值，放入Usage层
     */
    public static void computeCriticalDamage(WeaponElementData data) {
        DebugLogger.log("开始计算暴击伤害...");
        
        double criticalDamage = computeSingleCriticalDamage(data);
        data.setUsageValue(CRITICAL_DAMAGE, criticalDamage);
        
        DebugLogger.log("暴击伤害计算完成: %.3f", criticalDamage);
    }
    
    /**
     * 计算暴击伤害的具体值
     */
    private static double computeSingleCriticalDamage(WeaponElementData data) {
        double baseValue = 0.0;
        
        DebugLogger.log("正在计算暴击伤害");
        
        // 获取Basic层的暴击伤害值
        BasicEntry basicEntry = data.getBasicElement(CRITICAL_DAMAGE);
        if (basicEntry != null) {
            baseValue = basicEntry.getValue();
            DebugLogger.log("从Basic层获取到暴击伤害值: %.3f", baseValue);
        } else {
            DebugLogger.log("在Basic层未找到暴击伤害数据");
        }
        
        // 应用Computed层的修正
        ComputedEntry computedEntry = data.getComputedElement(CRITICAL_DAMAGE);
        if (computedEntry != null) {
            baseValue = applyModifier(baseValue, computedEntry);
            DebugLogger.log("应用Computed层修正后暴击伤害值: %.3f", baseValue);
        }
        
        // 如果Basic和Computed层都没有值，检查是否使用Computed层的独有值
        if (baseValue == 0.0 && computedEntry != null) {
            baseValue = computedEntry.getValue();
            DebugLogger.log("使用Computed层独有暴击伤害值: %.3f", baseValue);
        }
        
        // 确保暴击伤害为正数
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
                DebugLogger.log("未知的计算操作: %s", operation);
                return baseValue;
        }
    }
}