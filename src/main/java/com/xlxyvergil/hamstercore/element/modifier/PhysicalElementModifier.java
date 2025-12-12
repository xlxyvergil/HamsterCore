package com.xlxyvergil.hamstercore.element.modifier;

import com.xlxyvergil.hamstercore.element.WeaponElementData;
import com.xlxyvergil.hamstercore.element.BasicEntry;
import com.xlxyvergil.hamstercore.element.ComputedEntry;

import java.util.List;

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
        
        // 物理元素类型
        String[] physicalTypes = {"slash", "puncture", "impact"};
        
        
        for (String type : physicalTypes) {
            double value = computeSinglePhysicalElement(data, type);
            data.setUsageValue(type, value);
            
        }
        
    }
    
    /**
     * 计算单个物理元素的值
     */
    private static double computeSinglePhysicalElement(WeaponElementData data, String type) {
        double baseValue = 0.0;
        
        
        // 获取Basic层的值
        List<BasicEntry> basicEntries = data.getBasicElement(type);
        if (!basicEntries.isEmpty()) {
            // 累加所有Basic层的值
            baseValue = basicEntries.stream()
                .mapToDouble(BasicEntry::getValue)
                .sum();
        }
        
        // 应用Computed层的修正
        List<ComputedEntry> computedEntries = data.getComputedElement(type);
        if (!computedEntries.isEmpty()) {
            // 应用所有Computed层的修正
            for (ComputedEntry computedEntry : computedEntries) {
                baseValue = applyModifier(baseValue, computedEntry);
            }
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
                return baseValue;
        }
    }
}