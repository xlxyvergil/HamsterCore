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
    
    /**
     * 派系克制计算结果类
     */
    public static class FactionResult {
        private final double factionModifier; // HM 总克制系数
        private final Map<String, Double> breakdown; // 各元素的克制系数分解
        
        public FactionResult(double factionModifier, Map<String, Double> breakdown) {
            this.factionModifier = factionModifier;
            this.breakdown = new HashMap<>(breakdown);
        }
        
        public double getFactionModifier() {
            return factionModifier;
        }
        
        public Map<String, Double> getBreakdown() {
            return breakdown;
        }
    }
    
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
     * 计算针对特定派系的HM值（派系克制系数）- 返回详细结果，既用于显示也用于计算
     * HM = 派系元素数据值 + 克制系数
     * @param weaponData 武器数据（从缓存获取）
     * @param targetFaction 目标派系
     * @param cacheData 缓存的元素数据
     * @return 派系克制计算结果
     */
    public static FactionResult calculateFactionModifier(WeaponData weaponData, String targetFaction, AffixCacheManager.AffixCacheData cacheData) {
        if (weaponData == null || targetFaction == null || cacheData == null) {
            return new FactionResult(0.0, new HashMap<>());
        }
        
        // 获取目标派系的克制关系
        String factionName = targetFaction.toLowerCase();
        Map<String, Double> resistances = FACTION_RESISTANCES.get(factionName);
        if (resistances == null) {
            return new FactionResult(0.0, new HashMap<>()); // 未知派系，无克制关系
        }
        
        // 计算HM值：基于元素类型和派系克制关系
        double hm = 0.0;
        Map<String, Double> breakdown = new HashMap<>();
        
        // 检查武器是否对目标派系有特殊元素数据
        String factionElementName = factionName; // 派系元素名称与派系名称相同
        
        // 获取派系元素值（例如：orokin元素数据）
        Map<String, Double> factionElements = cacheData.getFactionElements();
        Double factionElementValue = factionElements.get(factionElementName);
        
        if (factionElementValue != null) {
            breakdown.put("faction_element", factionElementValue);
            
            // 获取武器所有有克制关系的元素，并累加克制系数
            double totalResistance = 0.0;
            Map<String, Double> resistanceBreakdown = new HashMap<>();
            
            // 检查派系元素
            for (String elementType : factionElements.keySet()) {
                Double resistance = resistances.get(elementType);
                if (resistance != null) {
                    totalResistance += resistance;
                    resistanceBreakdown.put("faction_" + elementType, resistance);
                }
            }
            
            // 检查复合元素
            Map<String, Double> combinedElements = cacheData.getCombinedElements();
            for (String elementType : combinedElements.keySet()) {
                Double resistance = resistances.get(elementType);
                if (resistance != null) {
                    totalResistance += resistance;
                    resistanceBreakdown.put("combined_" + elementType, resistance);
                }
            }
            
            // 检查物理元素
            Map<String, Double> physicalElements = cacheData.getPhysicalElements();
            for (String elementType : physicalElements.keySet()) {
                Double resistance = resistances.get(elementType);
                if (resistance != null) {
                    totalResistance += resistance;
                    resistanceBreakdown.put("physical_" + elementType, resistance);
                }
            }
            
            breakdown.putAll(resistanceBreakdown);
            
            // HM = 派系元素数据值 + 总克制系数
            hm = factionElementValue + totalResistance;
        }
        
        return new FactionResult(hm, breakdown);
    }
}