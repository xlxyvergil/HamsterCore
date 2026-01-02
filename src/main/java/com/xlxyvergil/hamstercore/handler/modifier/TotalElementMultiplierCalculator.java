package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.util.AttributeHelper;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.HashMap;

/**
 * 总元素倍率计算器
 * 计算攻击者的所有元素总倍率（基础元素+复合元素+物理元素）
 */
public class TotalElementMultiplierCalculator {
    
    /**
     * 总元素倍率计算结果类
     */
    public static class TotalElementResult {
        private final double totalElementMultiplier; // 总元素倍率
        private final Map<String, Double> breakdown; // 各元素的倍率分解
        
        public TotalElementResult(double totalElementMultiplier, Map<String, Double> breakdown) {
            this.totalElementMultiplier = totalElementMultiplier;
            this.breakdown = new HashMap<>(breakdown);
        }
        
        public double getTotalElementMultiplier() {
            return totalElementMultiplier;
        }
        
        public Map<String, Double> getBreakdown() {
            return breakdown;
        }
    }
    
    /**
     * 计算总元素倍率（直接从实体获取属性值）- 返回详细结果，既用于显示也用于计算
     * @param attacker 攻击者
     * @return 总元素倍率计算结果
     */
    public static TotalElementResult calculateTotalElementMultiplier(LivingEntity attacker) {
        double totalElementMultiplier = 0.0; // 默认总元素倍率为0.0（无加成）
        Map<String, Double> breakdown = new HashMap<>();
        
        // 计算所有元素总倍率（所有元素倍率之和）
        double totalRatio = 0.0;
        
        // 从实体获取基础元素的值
        double heatValue = AttributeHelper.getHeat(attacker);
        double coldValue = AttributeHelper.getCold(attacker);
        double electricityValue = AttributeHelper.getElectricity(attacker);
        double toxinValue = AttributeHelper.getToxin(attacker);
        
        // 从实体获取复合元素的值
        double blastValue = AttributeHelper.getBlast(attacker);
        double corrosiveValue = AttributeHelper.getCorrosive(attacker);
        double gasValue = AttributeHelper.getGas(attacker);
        double magneticValue = AttributeHelper.getMagnetic(attacker);
        double radiationValue = AttributeHelper.getRadiation(attacker);
        double viralValue = AttributeHelper.getViral(attacker);
        
        // 从实体获取物理元素的值
        double slashValue = AttributeHelper.getSlash(attacker);
        double punctureValue = AttributeHelper.getPuncture(attacker);
        double impactValue = AttributeHelper.getImpact(attacker);
        
        // 添加基础元素倍率
        if (heatValue > 0) {
            totalRatio += heatValue;
            breakdown.put("heat", heatValue);
        }
        if (coldValue > 0) {
            totalRatio += coldValue;
            breakdown.put("cold", coldValue);
        }
        if (electricityValue > 0) {
            totalRatio += electricityValue;
            breakdown.put("electricity", electricityValue);
        }
        if (toxinValue > 0) {
            totalRatio += toxinValue;
            breakdown.put("toxin", toxinValue);
        }
        
        // 添加复合元素倍率
        if (blastValue > 0) {
            totalRatio += blastValue;
            breakdown.put("blast", blastValue);
        }
        if (corrosiveValue > 0) {
            totalRatio += corrosiveValue;
            breakdown.put("corrosive", corrosiveValue);
        }
        if (gasValue > 0) {
            totalRatio += gasValue;
            breakdown.put("gas", gasValue);
        }
        if (magneticValue > 0) {
            totalRatio += magneticValue;
            breakdown.put("magnetic", magneticValue);
        }
        if (radiationValue > 0) {
            totalRatio += radiationValue;
            breakdown.put("radiation", radiationValue);
        }
        if (viralValue > 0) {
            totalRatio += viralValue;
            breakdown.put("viral", viralValue);
        }
        
        // 添加物理元素倍率
        if (slashValue > 0) {
            totalRatio += slashValue;
            breakdown.put("slash", slashValue);
        }
        if (punctureValue > 0) {
            totalRatio += punctureValue;
            breakdown.put("puncture", punctureValue);
        }
        if (impactValue > 0) {
            totalRatio += impactValue;
            breakdown.put("impact", impactValue);
        }
        
        // 总元素倍率 = 所有元素倍率之和，确保至少为1.0
        totalElementMultiplier = Math.max(1.0, totalRatio);
        
        return new TotalElementResult(totalElementMultiplier, breakdown);
    }
}