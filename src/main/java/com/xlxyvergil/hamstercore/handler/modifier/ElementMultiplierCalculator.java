package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * 元素倍率计算器
 * 计算攻击者的元素总倍率
 */
public class ElementMultiplierCalculator {
    
    /**
     * 计算元素总倍率
     * @param attacker 攻击者
     * @return 元素总倍率
     */
    public static double calculateElementMultiplier(net.minecraft.world.entity.LivingEntity attacker) {
        double totalElementMultiplier = 1.0; // 默认元素倍率为1.0（无加成）
        
        // 确保攻击者是玩家
        if (attacker instanceof Player player) {
            // 获取玩家主手物品
            ItemStack weapon = player.getMainHandItem();
            
            // 检查物品是否有元素属性
            if (ElementNBTUtils.hasAnyElements(weapon)) {
                // 从武器NBT中获取元素属性以计算元素总倍率
                List<Map.Entry<ElementType, Double>> elementList = ElementDamageManager.getActiveElements(weapon);
                Map<ElementType, Map.Entry<ElementType, Double>> elements = elementList.stream()
                        .collect(java.util.stream.Collectors.toMap(
                                Map.Entry::getKey,
                                java.util.function.Function.identity()
                        ));
                
                // 计算元素总倍率（所有元素倍率之和，除了暴击相关属性和触发率）
                double elementTotalRatio = 0.0;
                for (Map.Entry<ElementType, Map.Entry<ElementType, Double>> entry : elements.entrySet()) {
                    ElementType elementType = entry.getKey();
                    Map.Entry<ElementType, Double> elementEntry = entry.getValue();
                    
                    // 排除暴击相关属性和触发率，只计算元素伤害倍率
                    if (elementType != ElementType.CRITICAL_CHANCE && 
                        elementType != ElementType.CRITICAL_DAMAGE &&
                        elementType != ElementType.TRIGGER_CHANCE) {
                        elementTotalRatio += elementEntry.getValue();
                    }
                }
                
                // 元素总倍率 = 所有元素倍率之和（默认值保证了至少为1）
                totalElementMultiplier = elementTotalRatio;
            }
        }
        
        return totalElementMultiplier;
    }
}