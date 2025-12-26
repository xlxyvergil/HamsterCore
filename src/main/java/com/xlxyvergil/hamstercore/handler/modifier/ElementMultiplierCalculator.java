package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.util.AttributeHelper;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.HashMap;


/**
 * 元素倍率计算器
 * 计算攻击者的元素总倍率
 */
public class ElementMultiplierCalculator {
    
    /**
     * 元素倍率计算结果类
     */
    public static class ElementResult {
        private final double elementMultiplier; // 元素总倍率
        private final Map<String, Double> breakdown; // 各元素的倍率分解
        
        public ElementResult(double elementMultiplier, Map<String, Double> breakdown) {
            this.elementMultiplier = elementMultiplier;
            this.breakdown = new HashMap<>(breakdown);
        }
        
        public double getElementMultiplier() {
            return elementMultiplier;
        }
        
        public Map<String, Double> getBreakdown() {
            return breakdown;
        }
    }
    
    /**
     * 计算元素总倍率（直接从实体获取属性值）- 返回详细结果，既用于显示也用于计算
     * @param attacker 攻击者
     * @return 元素倍率计算结果
     */
    public static ElementResult calculateElementMultiplier(LivingEntity attacker) {
        double totalElementMultiplier = 0.0; // 默认元素倍率为0.0（无加成）
        Map<String, Double> breakdown = new HashMap<>();
        
        // 计算元素总倍率（所有元素倍率之和）
        double elementTotalRatio = 0.0;
        
        // 从实体获取基础元素和复合元素的值
        // 基础元素
        double heatValue = AttributeHelper.getHeat(attacker);
        double coldValue = AttributeHelper.getCold(attacker);
        double electricityValue = AttributeHelper.getElectricity(attacker);
        double toxinValue = AttributeHelper.getToxin(attacker);
        
        // 复合元素
        double blastValue = AttributeHelper.getBlast(attacker);
        double corrosiveValue = AttributeHelper.getCorrosive(attacker);
        double gasValue = AttributeHelper.getGas(attacker);
        double magneticValue = AttributeHelper.getMagnetic(attacker);
        double radiationValue = AttributeHelper.getRadiation(attacker);
        double viralValue = AttributeHelper.getViral(attacker);
        
        // 添加基础元素倍率
        if (heatValue > 0) {
            elementTotalRatio += heatValue;
            breakdown.put("heat", heatValue);
        }
        if (coldValue > 0) {
            elementTotalRatio += coldValue;
            breakdown.put("cold", coldValue);
        }
        if (electricityValue > 0) {
            elementTotalRatio += electricityValue;
            breakdown.put("electricity", electricityValue);
        }
        if (toxinValue > 0) {
            elementTotalRatio += toxinValue;
            breakdown.put("toxin", toxinValue);
        }
        
        // 添加复合元素倍率
        if (blastValue > 0) {
            elementTotalRatio += blastValue;
            breakdown.put("blast", blastValue);
        }
        if (corrosiveValue > 0) {
            elementTotalRatio += corrosiveValue;
            breakdown.put("corrosive", corrosiveValue);
        }
        if (gasValue > 0) {
            elementTotalRatio += gasValue;
            breakdown.put("gas", gasValue);
        }
        if (magneticValue > 0) {
            elementTotalRatio += magneticValue;
            breakdown.put("magnetic", magneticValue);
        }
        if (radiationValue > 0) {
            elementTotalRatio += radiationValue;
            breakdown.put("radiation", radiationValue);
        }
        if (viralValue > 0) {
            elementTotalRatio += viralValue;
            breakdown.put("viral", viralValue);
        }
        
        // 元素总倍率 = 所有元素倍率之和，确保至少为1.0
        totalElementMultiplier = Math.max(1.0, elementTotalRatio);
        
        return new ElementResult(totalElementMultiplier, breakdown);
    }
}