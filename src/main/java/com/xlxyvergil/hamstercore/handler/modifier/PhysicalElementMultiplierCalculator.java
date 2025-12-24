package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.handler.AffixCacheManager;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;
import java.util.HashMap;

/**
 * 物理元素倍率计算器
 * 计算攻击者的物理元素总倍率
 */
public class PhysicalElementMultiplierCalculator {
    
    /**
     * 物理元素倍率计算结果类
     */
    public static class PhysicalElementResult {
        private final double physicalElementMultiplier; // 物理元素总倍率
        private final Map<String, Double> breakdown; // 各物理元素的倍率分解
        
        public PhysicalElementResult(double physicalElementMultiplier, Map<String, Double> breakdown) {
            this.physicalElementMultiplier = physicalElementMultiplier;
            this.breakdown = new HashMap<>(breakdown);
        }
        
        public double getPhysicalElementMultiplier() {
            return physicalElementMultiplier;
        }
        
        public Map<String, Double> getBreakdown() {
            return breakdown;
        }
    }
    
    
    
    /**
     * 计算物理元素总倍率（使用缓存数据）- 返回详细结果，既用于显示也用于计算
     * @param attacker 攻击者
     * @param cacheData 缓存数据
     * @return 物理元素倍率计算结果
     */
    public static PhysicalElementResult calculatePhysicalElementMultiplier(LivingEntity attacker, AffixCacheManager.AffixCacheData cacheData) {
        double totalPhysicalMultiplier = 0.0; // 默认物理元素倍率为0.0（无加成）
        Map<String, Double> breakdown = new HashMap<>();
        
        // 如果缓存数据为空，返回默认值
        if (cacheData == null) {
            return new PhysicalElementResult(totalPhysicalMultiplier, breakdown);
        }
        
        // 计算物理元素总倍率（所有物理元素倍率之和）
        double physicalTotalRatio = 0.0;
        
        // 从缓存数据中获取物理元素
        Map<String, Double> physicalElements = cacheData.getPhysicalElements();
        String[] physicalTypes = {"slash", "puncture", "impact"};
        
        // 计算物理元素总倍率
        for (String type : physicalTypes) {
            Double value = physicalElements.get(type);
            if (value != null && value > 0) {
                physicalTotalRatio += value;
                breakdown.put(type, value);
            }
        }
        
        // 物理元素总倍率 = 所有物理元素倍率之和，确保至少为1.0
        totalPhysicalMultiplier = Math.max(1.0, physicalTotalRatio);
        
        return new PhysicalElementResult(totalPhysicalMultiplier, breakdown);
    }
}