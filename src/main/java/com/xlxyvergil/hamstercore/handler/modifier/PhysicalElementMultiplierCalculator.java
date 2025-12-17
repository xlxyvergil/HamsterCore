package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.handler.AffixCacheManager;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

/**
 * 物理元素倍率计算器
 * 计算攻击者的物理元素总倍率
 */
public class PhysicalElementMultiplierCalculator {
    
    
    
    /**
     * 计算物理元素总倍率（使用缓存数据）
     * @param attacker 攻击者
     * @param cacheData 缓存数据
     * @return 物理元素总倍率
     */
    public static double calculatePhysicalElementMultiplier(LivingEntity attacker, AffixCacheManager.AffixCacheData cacheData) {
        double totalPhysicalMultiplier = 0.0; // 默认物理元素倍率为0.0（无加成）
        
        // 如果缓存数据为空，返回默认值
        if (cacheData == null) {
            return totalPhysicalMultiplier;
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
            }
        }
        
        // 物理元素总倍率 = 所有物理元素倍率之和
        totalPhysicalMultiplier = physicalTotalRatio;
        
        return totalPhysicalMultiplier;
    }
}