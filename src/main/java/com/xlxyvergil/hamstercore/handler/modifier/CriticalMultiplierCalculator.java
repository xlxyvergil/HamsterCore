package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 暴击倍率计算器
 * 计算攻击者的暴击倍率
 */
public class CriticalMultiplierCalculator {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * 计算暴击倍率
     * @param attacker 攻击者
     * @return 暴击倍率
     */
    public static double calculateCriticalMultiplier(net.minecraft.world.entity.LivingEntity attacker) {
        double criticalMultiplier = 1.0; // 默认暴击倍率
        
        // 确保攻击者是玩家
        if (attacker instanceof Player player) {
            // 获取玩家主手物品
            ItemStack weapon = player.getMainHandItem();
            
            // 检查物品是否有元素属性
            if (ElementNBTUtils.hasAnyElements(weapon)) {
                // 从武器NBT中获取元素属性
                List<Map.Entry<ElementType, Double>> elementList = ElementDamageManager.getActiveElements(weapon);
                Map<ElementType, Map.Entry<ElementType, Double>> elements = elementList.stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                java.util.function.Function.identity()
                        ));
                
                // 获取暴击率和暴击伤害
                double criticalChance = 0.0;
                double criticalDamage = 1.0; // 默认暴击伤害倍率
                
                if (elements.containsKey(ElementType.CRITICAL_CHANCE)) {
                    criticalChance = elements.get(ElementType.CRITICAL_CHANCE).getValue();
                }
                
                if (elements.containsKey(ElementType.CRITICAL_DAMAGE)) {
                    criticalDamage = elements.get(ElementType.CRITICAL_DAMAGE).getValue();
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
                double totalCriticalDamage = criticalDamage; // 简化处理，暂不考虑暴击倍率增益
                criticalMultiplier = 1 + criticalLevel * (totalCriticalDamage - 1);
                
                LOGGER.info("Critical hit! Level: " + criticalLevel + ", Damage multiplied by " + criticalMultiplier);
                
                // 向玩家发送暴击信息
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("暴击! 等级: " + criticalLevel + ", 伤害倍率: " + String.format("%.2f", criticalMultiplier)).withStyle(net.minecraft.ChatFormatting.GOLD));
            }
        }
        
        return criticalMultiplier;
    }
}