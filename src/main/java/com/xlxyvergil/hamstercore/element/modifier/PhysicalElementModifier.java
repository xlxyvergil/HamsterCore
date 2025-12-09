package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;
import com.xlxyvergil.hamstercore.util.DebugLogger;

/**
 * 物理元素Modifier
 * 专门处理物理元素的计算
 */
public class PhysicalElementModifier {
    
    /**
     * 计算物理元素
     * 从Basic层和Computed层获取物理元素数据，计算最终值，放入Usage层
     */
    public static void computePhysicalElements(WeaponElementData data) {
        DebugLogger.log("开始计算物理元素...");
        
        // 物理元素类型
        String[] physicalTypes = {"slash", "puncture", "impact"};
        
        for (String type : physicalTypes) {
            double value = computeSinglePhysicalElement(data, type);
            data.setUsageValue(type, value);
            
            DebugLogger.log("物理元素 %s: %.3f", type, value);
        }
        
        DebugLogger.log("物理元素计算完成");
    }
    
    /**
     * 计算单个物理元素的值
     */
    private static double computeSinglePhysicalElement(WeaponElementData data, String type) {
        double baseValue = 0.0;
        
        // 获取Basic层的值
        BasicEntry basicEntry = data.getBasicElement(type);
        if (basicEntry != null) {
            baseValue = basicEntry.getValue();
        }
        
        // 应用Computed层的修正
        ComputedEntry computedEntry = data.getComputedElement(type);
        if (computedEntry != null) {
            baseValue = applyModifier(baseValue, computedEntry);
        }
        
        // 如果Basic和Computed层都没有值，检查是否使用Computed层的独有值
        if (baseValue == 0.0 && computedEntry != null) {
            baseValue = computedEntry.getValue();
        }
        
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