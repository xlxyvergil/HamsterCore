package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.handler.AffixCacheManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Random;

/**
 * 暴击倍率计算器
 * 计算攻击者的暴击倍率
 */
public class CriticalMultiplierCalculator {
    
    

    
    
    
    
    
    /**
     * 计算暴击倍率（使用缓存数据）
     * @param attacker 攻击者
     * @param weapon 武器物品堆
     * @param specialAndFactionValues 特殊元素
     * @param cacheData 缓存数据
     * @return 暴击倍率
     */
    public static double calculateCriticalMultiplier(net.minecraft.world.entity.LivingEntity attacker, ItemStack weapon, Map<String, Double> specialAndFactionValues, AffixCacheManager.AffixCacheData cacheData) {
        double criticalMultiplier = 1.0; // 默认暴击倍率
        
        // 首先尝试从缓存中获取暴击数据，如果没有则使用传统方法
        double criticalChance = 0.0;
        double criticalDamage = 0.0;
        
        if (cacheData != null && cacheData.getCriticalStats() != null) {
            Map<String, Double> criticalStats = cacheData.getCriticalStats();
            criticalChance = criticalStats.getOrDefault("critical_chance", 0.0);
            criticalDamage = criticalStats.getOrDefault("critical_damage", 0.0);
        } else if (specialAndFactionValues != null) {
            // 回退到传统方法
            criticalChance = specialAndFactionValues.getOrDefault("critical_chance", 0.0);
            criticalDamage = specialAndFactionValues.getOrDefault("critical_damage", 0.0);
        }
        
        // 使用Random判断是否暴击
        Random random = new Random();
        // 暴击等级判断：n% < 暴击几率 ≤ (n% + 100%)
        // 概率达到的暴击等级：(n% + 100%) ÷ 100
        // 保底达到的暴击等级：(n% + 100%) ÷ 100 - 1
        
        double chancePercent = criticalChance * 100;
        int guaranteedCriticalLevel = (int) Math.floor((chancePercent + 100) / 100) - 1; // 保底暴击等级
        int maxCriticalLevel = (int) Math.floor((chancePercent + 100) / 100); // 最大可能暴击等级
        
        // 确保暴击等级至少为0
        if (guaranteedCriticalLevel < 0) {
            guaranteedCriticalLevel = 0;
        }
        
        if (maxCriticalLevel < 0) {
            maxCriticalLevel = 0;
        }
        
        // 判断是否能达到更高的暴击等级
        int criticalLevel = guaranteedCriticalLevel;
        double extraChance = chancePercent - (guaranteedCriticalLevel * 100); // 超出保底等级的部分
        
        if (random.nextDouble() * 100 < extraChance) {
            criticalLevel = maxCriticalLevel;
        }
        
        // 暴击倍率计算公式：
        // 暴击倍率 = 1 + 暴击等级 × (暴击伤害 - 1)
        // 其中：
        // - 暴击伤害：从缓存中获取的已经过基础暴击伤害计算的完整值
        // - 暴击等级：根据暴击率计算得出
        
        // 从缓存中获取的暴击伤害值（已经是经过基础暴击伤害计算的完整值）
        double totalCriticalDamage = criticalDamage;
        
        // 计算最终暴击倍率（基于暴击等级的增幅）
        criticalMultiplier = 1 + criticalLevel * (totalCriticalDamage - 1);
        
        // 如果攻击者是玩家，向玩家发送暴击信息
        if (attacker instanceof Player player) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("暴击! 等级: " + criticalLevel + ", 伤害倍率: " + String.format("%.2f", criticalMultiplier)).withStyle(net.minecraft.ChatFormatting.GOLD));
        }
        
        return criticalMultiplier;
    }
}