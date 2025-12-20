package com.xlxyvergil.hamstercore.calculator.shield;

/**
 * 护盾系数计算器
 */
public class ShieldCoefficientCalculator {
    
    /**
     * 计算护盾系数
     * 护盾系数 f(x) = 1 + 0.02 × (x - 基础等级)^1.76
     * @param level 实体等级
     * @param baseLevel 基础等级
     * @return 护盾系数
     */
    public static float calculateShieldCoefficient(int level, int baseLevel) {
        return 1.0f + 0.02f * (float) Math.pow(level - baseLevel, 1.76);
    }
}