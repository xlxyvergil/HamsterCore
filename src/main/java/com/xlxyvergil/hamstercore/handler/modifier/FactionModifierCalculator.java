package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.util.AttributeHelper;

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
     * @param attacker 攻击者实体
     * @param targetFaction 目标派系
     * @return 派系克制计算结果
     */
    public static FactionResult calculateFactionModifier(net.minecraft.world.entity.LivingEntity attacker, String targetFaction) {
        if (attacker == null || targetFaction == null) {
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
        
        // 从攻击者实体获取派系元素值
        double grineerValue = AttributeHelper.getGrineer(attacker);
        double infestedValue = AttributeHelper.getInfested(attacker);
        double corpusValue = AttributeHelper.getCorpus(attacker);
        double orokinValue = AttributeHelper.getOrokin(attacker);
        double sentientValue = AttributeHelper.getSentient(attacker);
        double murmurValue = AttributeHelper.getMurmur(attacker);
        
        // 计算派系元素总值
        double totalFactionValue = grineerValue + infestedValue + corpusValue + orokinValue + sentientValue + murmurValue;
        
        // 获取武器所有有克制关系的元素，并累加克制系数
        double totalResistance = 0.0;
        Map<String, Double> resistanceBreakdown = new HashMap<>();
        
        // 检查派系元素
        if (grineerValue > 0) {
            Double resistance = resistances.get("grineer");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("grineer", resistance);
            }
        }
        if (infestedValue > 0) {
            Double resistance = resistances.get("infested");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("infested", resistance);
            }
        }
        if (corpusValue > 0) {
            Double resistance = resistances.get("corpus");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("corpus", resistance);
            }
        }
        if (orokinValue > 0) {
            Double resistance = resistances.get("orokin");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("orokin", resistance);
            }
        }
        if (sentientValue > 0) {
            Double resistance = resistances.get("sentient");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("sentient", resistance);
            }
        }
        if (murmurValue > 0) {
            Double resistance = resistances.get("murmur");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("murmur", resistance);
            }
        }
        
        // 检查复合元素
        double blastValue = AttributeHelper.getBlast(attacker);
        double corrosiveValue = AttributeHelper.getCorrosive(attacker);
        double gasValue = AttributeHelper.getGas(attacker);
        double magneticValue = AttributeHelper.getMagnetic(attacker);
        double radiationValue = AttributeHelper.getRadiation(attacker);
        double viralValue = AttributeHelper.getViral(attacker);
        
        if (blastValue > 0) {
            Double resistance = resistances.get("blast");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("blast", resistance);
            }
        }
        if (corrosiveValue > 0) {
            Double resistance = resistances.get("corrosive");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("corrosive", resistance);
            }
        }
        if (gasValue > 0) {
            Double resistance = resistances.get("gas");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("gas", resistance);
            }
        }
        if (magneticValue > 0) {
            Double resistance = resistances.get("magnetic");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("magnetic", resistance);
            }
        }
        if (radiationValue > 0) {
            Double resistance = resistances.get("radiation");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("radiation", resistance);
            }
        }
        if (viralValue > 0) {
            Double resistance = resistances.get("viral");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("viral", resistance);
            }
        }
        
        // 检查物理元素
        double impactValue = AttributeHelper.getImpact(attacker);
        double punctureValue = AttributeHelper.getPuncture(attacker);
        double slashValue = AttributeHelper.getSlash(attacker);
        
        if (impactValue > 0) {
            Double resistance = resistances.get("impact");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("impact", resistance);
            }
        }
        if (punctureValue > 0) {
            Double resistance = resistances.get("puncture");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("puncture", resistance);
            }
        }
        if (slashValue > 0) {
            Double resistance = resistances.get("slash");
            if (resistance != null) {
                totalResistance += resistance;
                resistanceBreakdown.put("slash", resistance);
            }
        }
        
        breakdown.putAll(resistanceBreakdown);
        
        // HM = 派系元素数据值 + 总克制系数
        hm = totalFactionValue + totalResistance;
        
        return new FactionResult(hm, breakdown);
    }
}