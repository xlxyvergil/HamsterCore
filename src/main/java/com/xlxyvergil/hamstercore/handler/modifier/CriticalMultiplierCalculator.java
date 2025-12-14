package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import com.xlxyvergil.hamstercore.util.ElementModifierValueUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 暴击倍率计算器
 * 计算攻击者的暴击倍率
 */
public class CriticalMultiplierCalculator {
    
    
    /**
     * 计算暴击倍率
     * @param attacker 攻击者
     * @param weapon 武器物品堆
     * @return 暴击倍率
     */
    public static double calculateCriticalMultiplier(net.minecraft.world.entity.LivingEntity attacker, ItemStack weapon) {
        double criticalMultiplier = 1.0; // 默认暴击倍率
        
        // 检查物品是否有元素属性
        if (ElementNBTUtils.hasAnyElements(weapon)) {
            // 直接从修饰符系统获取暴击率和暴击伤害
            double criticalChance = ElementModifierValueUtil.getElementValueFromAttributes(weapon, ElementType.CRITICAL_CHANCE);
            double criticalDamage = ElementModifierValueUtil.getElementValueFromAttributes(weapon, ElementType.CRITICAL_DAMAGE);
            
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
            double totalCriticalDamage = criticalDamage; // 简化处理，暂不考虑暴击倍率增益
            criticalMultiplier = 1 + criticalLevel * (totalCriticalDamage - 1);
            
            
            // 如果攻击者是玩家，向玩家发送暴击信息
            if (attacker instanceof Player player) {
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("暴击! 等级: " + criticalLevel + ", 伤害倍率: " + String.format("%.2f", criticalMultiplier)).withStyle(net.minecraft.ChatFormatting.GOLD));
            }
        }
        
        return criticalMultiplier;
    }
}