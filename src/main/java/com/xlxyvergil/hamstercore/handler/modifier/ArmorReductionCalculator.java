package com.xlxyvergil.hamstercore.handler.modifier;

import net.minecraft.world.entity.LivingEntity;

/**
 * 护甲减免计算器
 * 参考TACZ的DamageModifier设计模式
 */
public class ArmorReductionCalculator {
    
    /**
     * 护甲减免计算结果类
     */
    public static class ArmorReductionResult {
        private final double armorReduction; // (1-AM) 护甲减免系数
        private final double armorValue; // 原始护甲值
        private final double armorModifier; // AM值
        
        public ArmorReductionResult(double armorReduction, double armorValue, double armorModifier) {
            this.armorReduction = armorReduction;
            this.armorValue = armorValue;
            this.armorModifier = armorModifier;
        }
        
        public double getArmorReduction() {
            return armorReduction;
        }
        
        public double getArmorValue() {
            return armorValue;
        }
        
        public double getArmorModifier() {
            return armorModifier;
        }
    }
    
    /**
     * 计算护甲减免系数 (1-AM) - 返回详细结果，既用于显示也用于计算
     * @param target 目标实体
     * @param targetArmor 目标护甲值
     * @return 护甲减免计算结果
     */
    public static ArmorReductionResult calculateArmorReduction(LivingEntity target, Double targetArmor) {
        // 计算AM = 0.9 × √(AR/2700)
        double AM = 0.9 * Math.sqrt(targetArmor / 2700.0);
        double reduction = 1.0 - AM;
        
        return new ArmorReductionResult(reduction, targetArmor, AM);
    }
}