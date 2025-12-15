package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.element.WeaponData;


/**
 * 元素倍率计算器
 * 计算攻击者的元素总倍率
 */
public class ElementMultiplierCalculator {
    
    /**
     * 计算元素总倍率（使用WeaponData中的usage层数据）
     * @param attacker 攻击者
     * @param data 武器数据（包含usage层数据）
     * @return 元素总倍率
     */
    public static double calculateElementMultiplier(net.minecraft.world.entity.LivingEntity attacker, WeaponData data) {
        double totalElementMultiplier = 1.0; // 默认元素倍率为1.0（无加成）
        
        // 如果数据为空，返回默认值
        if (data == null) {
            return totalElementMultiplier;
        }
        
        // 计算元素总倍率（所有元素倍率之和）
        double elementTotalRatio = 0.0;
        
        // 从WeaponData的usage层数据中获取基础元素和复合元素
        String[] basicTypes = {"heat", "cold", "electricity", "toxin"};
        String[] complexTypes = {"blast", "corrosive", "gas", "magnetic", "radiation", "viral"};
        
        // 添加基础元素倍率
        for (String type : basicTypes) {
            Double value = data.getUsageValue(type);
            if (value != null && value > 0) {
                elementTotalRatio += value;
            }
        }
        
        // 添加复合元素倍率
        for (String type : complexTypes) {
            Double value = data.getUsageValue(type);
            if (value != null && value > 0) {
                elementTotalRatio += value;
            }
        }
        
        // 元素总倍率 = 1.0 + 所有元素倍率之和
        totalElementMultiplier = 1.0 + elementTotalRatio;
        
        return totalElementMultiplier;
    }
}