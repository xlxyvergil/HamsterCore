package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
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
     * 计算暴击倍率（使用预计算的特殊元素）
     * @param attacker 攻击者
     * @param weapon 武器物品堆
     * @param specialAndFactionValues 特殊元素
     * @return 暴击倍率
     */
    public static double calculateCriticalMultiplier(net.minecraft.world.entity.LivingEntity attacker, ItemStack weapon, Map<String, Double> specialAndFactionValues) {
        double criticalMultiplier = 1.0; // 默认暴击倍率
        
        // 直接使用预计算的暴击率和暴击伤害值，不再检查元素属性
        double criticalChance = 0.0;
        double criticalDamage = 0.0;
        
        if (specialAndFactionValues != null) {
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
        
        // 暴击倍率（暴击伤害） =1 + 暴击等级 × (武器总暴击倍率 − 1)
        // 武器总暴击倍率 = 武器基础暴击倍率 × (1 +暴击倍率增益)
        
        // 获取武器基础暴击倍率
        double baseCriticalMultiplier = getBaseCriticalMultiplier(weapon);
        
        // 计算暴击倍率增益（假设critical_damage值即为增益值）
        double criticalDamageBonus = criticalDamage;
        
        // 计算武器总暴击倍率
        double totalCriticalDamage = baseCriticalMultiplier * (1 + criticalDamageBonus);
        
        // 计算最终暴击倍率
        criticalMultiplier = 1 + criticalLevel * (totalCriticalDamage - 1);
        
        
        // 如果攻击者是玩家，向玩家发送暴击信息
        if (attacker instanceof Player player) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("暴击! 等级: " + criticalLevel + ", 伤害倍率: " + String.format("%.2f", criticalMultiplier)).withStyle(net.minecraft.ChatFormatting.GOLD));
        }
        
        return criticalMultiplier;
    }
    
    /**
     * 获取武器基础暴击倍率
     * @param weapon 武器物品堆
     * @return 武器基础暴击倍率，默认为0.5（0.5倍伤害）
     */
    private static double getBaseCriticalMultiplier(ItemStack weapon) {
        if (weapon.isEmpty()) {
            return 0.5; // 默认基础暴击倍率为0.5
        }
        
        // 尝试从WeaponData获取基础暴击倍率
        WeaponData weaponData = WeaponDataManager.getWeaponData(weapon);
        if (weaponData != null) {
            // 检查是否有基础暴击倍率配置
            Double baseCritMultiplier = weaponData.getUsageValue("base_critical_multiplier");
            if (baseCritMultiplier != null) {
                return baseCritMultiplier;
            }
        }
        
        // 默认基础暴击倍率为0.5
        return 0.5;
    }
}