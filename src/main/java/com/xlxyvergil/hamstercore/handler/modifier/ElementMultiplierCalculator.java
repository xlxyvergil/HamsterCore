package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.handler.AffixCacheManager;

import java.util.Map;


/**
 * 元素倍率计算器
 * 计算攻击者的元素总倍率
 */
public class ElementMultiplierCalculator {
    
    
    
    /**
     * 计算元素总倍率（使用缓存数据）
     * @param attacker 攻击者
     * @param cacheData 缓存数据
     * @return 元素总倍率
     */
    public static double calculateElementMultiplier(net.minecraft.world.entity.LivingEntity attacker, AffixCacheManager.AffixCacheData cacheData) {
        double totalElementMultiplier = 0.0; // 默认元素倍率为0.0（无加成）
        
        // 如果缓存数据为空，返回默认值
        if (cacheData == null) {
            return totalElementMultiplier;
        }
        
        // 计算元素总倍率（所有元素倍率之和）
        double elementTotalRatio = 0.0;
        
        // 从缓存数据中获取复合元素值
        Map<String, Double> combinedElements = cacheData.getCombinedElements();
        
        // 缓存中获取基础元素和复合元素
        String[] basicTypes = {"heat", "cold", "electricity", "toxin"};
        String[] complexTypes = {"blast", "corrosive", "gas", "magnetic", "radiation", "viral"};
        
        // 添加基础元素倍率
        for (String type : basicTypes) {
            Double value = combinedElements.get(type);
            if (value != null && value > 0) {
                elementTotalRatio += value;
            }
        }
        
        // 添加复合元素倍率
        for (String type : complexTypes) {
            Double value = combinedElements.get(type);
            if (value != null && value > 0) {
                elementTotalRatio += value;
            }
        }
        
        // 元素总倍率 = 所有元素倍率之和
        totalElementMultiplier = elementTotalRatio;
        
        return totalElementMultiplier;
    }
}