package com.xlxyvergil.hamstercore.handler.modifier;

import net.minecraft.world.entity.LivingEntity;

/**
 * 护甲减免计算器
 * 参考TACZ的DamageModifier设计模式
 */
public class ArmorReductionCalculator {
    
    /**
     * 计算护甲减免系数 (1-AM)
     * @param target 目标实体
     * @param targetArmor 目标护甲值
     * @return 护甲减免系数
     */
    public static double calculateArmorReduction(LivingEntity target, Double targetArmor) {
        // 计算AM = 0.9 × √(AR/2700)
        double AM = 0.9 * Math.sqrt(targetArmor / 2700.0);
        
        return 1.0 - AM;
    }
}