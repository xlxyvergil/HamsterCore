package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.handler.AffixCacheManager;

import java.util.HashMap;
import java.util.Map;

/**
 * 派系修饰符计算器
 * 计算针对特定派系的伤害修饰符，基于派系克制关系
 */
public class FactionModifierCalculator {
    
    // 派系克制关系表
    private static final Map<String, Map<String, Double>> FACTION_RESISTANCES = new HashMap<>();
    
    static {
        // Grineer: 冲击+50% 腐蚀+50%
        Map<String, Double> grineerResistances = new HashMap<>();
        grineerResistances.put("impact", 0.5);
        grineerResistances.put("corrosive", 0.5);
        FACTION_RESISTANCES.put("grineer", grineerResistances);
        
        // Infested: 切割+50% 火焰+50%
        Map<String, Double> infestedResistances = new HashMap<>();
        infestedResistances.put("slash", 0.5);
        infestedResistances.put("heat", 0.5);
        FACTION_RESISTANCES.put("infested", infestedResistances);
        
        // Corpus: 穿刺+50% 磁力+50%
        Map<String, Double> corpusResistances = new HashMap<>();
        corpusResistances.put("puncture", 0.5);
        corpusResistances.put("magnetic", 0.5);
        FACTION_RESISTANCES.put("corpus", corpusResistances);
        
        // Orokin: 穿刺+50% 病毒+50% 辐射-50%
        Map<String, Double> orokinResistances = new HashMap<>();
        orokinResistances.put("puncture", 0.5);
        orokinResistances.put("viral", 0.5);
        orokinResistances.put("radiation", -0.5); // 负值表示弱点
        FACTION_RESISTANCES.put("orokin", orokinResistances);
        
        // Sentient: 冰冻+50% 辐射+50% 腐蚀-50%
        Map<String, Double> sentientResistances = new HashMap<>();
        sentientResistances.put("cold", 0.5);
        sentientResistances.put("radiation", 0.5);
        sentientResistances.put("corrosive", -0.5); // 负值表示弱点
        FACTION_RESISTANCES.put("sentient", sentientResistances);
        
        // Murmur: 电击+50% 辐射+50% 病毒-50%
        Map<String, Double> murmurResistances = new HashMap<>();
        murmurResistances.put("electricity", 0.5);
        murmurResistances.put("radiation", 0.5);
        murmurResistances.put("viral", -0.5); // 负值表示弱点
        FACTION_RESISTANCES.put("murmur", murmurResistances);
    }
    
    
    
    /**
     * 计算派系伤害修饰符（使用缓存数据）
     * @param weaponData 武器数据（从缓存获取）
     * @param targetFaction 目标派系
     * @param cacheData 缓存的元素数据
     * @return 派系伤害修饰符
     */
    public static double calculateFactionModifier(WeaponData weaponData, String targetFaction, AffixCacheManager.AffixCacheData cacheData) {
        if (weaponData == null || targetFaction == null || cacheData == null) {
            return 0.0;
        }
        
        // 获取目标派系的克制关系
        String factionName = targetFaction.toLowerCase();
        Map<String, Double> resistances = FACTION_RESISTANCES.get(factionName);
        if (resistances == null) {
            return 0.0; // 未知派系，无克制关系
        }
        
        // 计算所有元素对目标派系的伤害加成
        double totalModifier = 0.0;
        
        // 获取派系元素值（从缓存中获取）
        Map<String, Double> factionElements = cacheData.getFactionElements();
        for (Map.Entry<String, Double> entry : factionElements.entrySet()) {
            String elementType = entry.getKey();
            double elementValue = entry.getValue();
            
            // 检查该元素是否对目标派系有克制关系
            Double resistance = resistances.get(elementType);
            if (resistance != null) {
                // 伤害加成 = 元素值 × 克制系数
                double damageBonus = elementValue * resistance;
                totalModifier += damageBonus;
            }
        }
        
        // 获取复合元素值（从缓存中获取）
        Map<String, Double> combinedElements = cacheData.getCombinedElements();
        for (Map.Entry<String, Double> entry : combinedElements.entrySet()) {
            String elementType = entry.getKey();
            double elementValue = entry.getValue();
            
            // 检查该元素是否对目标派系有克制关系
            Double resistance = resistances.get(elementType);
            if (resistance != null) {
                // 伤害加成 = 元素值 × 克制系数
                double damageBonus = elementValue * resistance;
                totalModifier += damageBonus;
            }
        }
        
        // 获取物理元素值（从缓存中获取）
        Map<String, Double> physicalElements = cacheData.getPhysicalElements();
        for (Map.Entry<String, Double> entry : physicalElements.entrySet()) {
            String elementType = entry.getKey();
            double elementValue = entry.getValue();
            
            // 检查该元素是否对目标派系有克制关系
            Double resistance = resistances.get(elementType);
            if (resistance != null) {
                // 伤害加成 = 元素值 × 克制系数
                double damageBonus = elementValue * resistance;
                totalModifier += damageBonus;
            }
        }
        
        return totalModifier;
    }
}