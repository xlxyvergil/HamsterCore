package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectInstance;
import com.xlxyvergil.hamstercore.element.effect.effects.ColdEffect;
import com.xlxyvergil.hamstercore.util.AttributeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Random;

/**
 * 暴击倍率计算器
 * 计算攻击者的暴击倍率
 */
public class CriticalMultiplierCalculator {
    
    /**
     * 暴击计算结果类
     */
    public static class CriticalResult {
        private final double multiplier;
        private final int level;
        private final double damage;
        
        public CriticalResult(double multiplier, int level, double damage) {
            this.multiplier = multiplier;
            this.level = level;
            this.damage = damage;
        }
        
        public double getMultiplier() {
            return multiplier;
        }
        
        public int getLevel() {
            return level;
        }
        
        public double getDamage() {
            return damage;
        }
    }
    
    /**
     * 计算暴击倍率（直接从实体获取属性值）- 返回详细结果，既用于显示也用于计算
     * @param attacker 攻击者
     * @param target 被攻击者
     * @param weapon 武器物品堆
     * @param specialAndFactionValues 特殊元素
     * @return 暴击计算结果
     */
    public static CriticalResult calculateCriticalMultiplier(LivingEntity attacker, LivingEntity target, ItemStack weapon, Map<String, Double> specialAndFactionValues) {
        double criticalMultiplier = 1.0; // 默认暴击倍率
        
        // 直接从实体获取暴击相关属性值
        double criticalChance = AttributeHelper.getCriticalChance(attacker);
        double criticalDamage = AttributeHelper.getCriticalDamage(attacker);
        
        // 检查被攻击者是否具有穿刺效果，如果有则增加暴击几率
        ElementEffectInstance punctureEffect = ElementEffectManager.getEffect(target, ElementType.PUNCTURE);
        if (punctureEffect != null) {
            // 每级获得5%暴击几率，最大层数时总共增加25%
            int amplifier = punctureEffect.getAmplifier();
            double punctureCriticalBonus = amplifier * 0.05;
            criticalChance += punctureCriticalBonus;
        }
        
        // 检查被攻击者是否具有冰冻效果，如果有则增加暴击伤害
        ElementEffectInstance coldEffect = ElementEffectManager.getEffect(target, ElementType.COLD);
        if (coldEffect != null) {
            // 每级获得20%暴击伤害，最大层数时总共增加120%
            int amplifier = coldEffect.getAmplifier();
            double coldCriticalDamageBonus = amplifier * 0.20;
            criticalDamage += coldCriticalDamageBonus;
        }
        
        // 使用Random判断暴击等级
        Random random = new Random();
        // 暴击等级判断逻辑：
        // 每个暴击等级需要100%的暴击率，超出保底的部分由随机数决定是否达到更高等级
        // 例如：150%暴击率 = 保底1级 + 50%概率达到2级
        
        double chancePercent = criticalChance * 100;
        int guaranteedCriticalLevel = (int) Math.floor((chancePercent + 100) / 100) - 1; // 保底暴击等级
        int maxCriticalLevel = (int) Math.floor((chancePercent + 100) / 100); // 最大可能暴击等级
        double extraChance = chancePercent - (guaranteedCriticalLevel * 100); // 超出保底等级的部分
        
        // 确保暴击等级至少为0
        if (guaranteedCriticalLevel < 0) {
            guaranteedCriticalLevel = 0;
        }
        
        if (maxCriticalLevel < 0) {
            maxCriticalLevel = 0;
        }
        
        // 先判断随机数是否达到更高等级
        int criticalLevel;
        if (random.nextDouble() * 100 < extraChance) {
            // 达到更高暴击等级
            criticalLevel = maxCriticalLevel;
        } else {
            // 保底暴击等级
            criticalLevel = guaranteedCriticalLevel;
        }
        
        // 暴击倍率计算公式：
        // 暴击倍率 = 1 + 暴击等级 × (暴击伤害 - 1)
        // 其中：
        // - 暴击伤害：从实体获取的已经过基础暴击伤害计算的完整值
        // - 暴击等级：根据暴击率计算得出
        
        // 从实体获取的暴击伤害值（已经是经过基础暴击伤害计算的完整值）
        double totalCriticalDamage = criticalDamage;
        
        // 计算最终暴击倍率（基于暴击等级的增幅），确保至少为1.0
        criticalMultiplier = Math.max(1.0, 1 + criticalLevel * (totalCriticalDamage - 1));
        
        return new CriticalResult(criticalMultiplier, criticalLevel, criticalDamage);
    }
}