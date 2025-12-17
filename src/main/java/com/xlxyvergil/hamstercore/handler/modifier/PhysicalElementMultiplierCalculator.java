package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.element.WeaponData;
import net.minecraft.world.entity.LivingEntity;

/**
 * 物理元素倍率计算器
 * 计算攻击者的物理元素总倍率
 */
public class PhysicalElementMultiplierCalculator {
    
    /**
     * 计算物理元素总倍率（使用WeaponData中的usage层数据）
     * @param attacker 攻击者
     * @param data 武器数据（包含usage层数据）
     * @return 物理元素总倍率
     */
    public static double calculatePhysicalElementMultiplier(LivingEntity attacker, WeaponData data) {
        double totalPhysicalMultiplier = 0.0; // 默认物理元素倍率为0.0（无加成）
        
        // 如果数据为空，返回默认值
        if (data == null) {
            return totalPhysicalMultiplier;
        }
        
        // 计算物理元素总倍率（所有物理元素倍率之和）
        double physicalTotalRatio = 0.0;
        
        // 从WeaponData的usage层数据中获取物理元素
        String[] physicalTypes = {"slash", "puncture", "impact"};
        
        // 计算物理元素总倍率
        for (String type : physicalTypes) {
            Double value = data.getUsageValue(type);
            if (value != null && value > 0) {
                physicalTotalRatio += value;
            }
        }
        
        // 物理元素总倍率 = 所有物理元素倍率之和
        totalPhysicalMultiplier = physicalTotalRatio;
        
        return totalPhysicalMultiplier;
    }
}