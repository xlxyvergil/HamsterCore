package com.xlxyvergil.hamstercore.handler.modifier;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

/**
 * 派系修饰符计算器
 * 计算针对特定派系的伤害修饰符
 */
public class FactionModifierCalculator {
    

    
    /**
     * 计算派系伤害修饰符（使用预计算的特殊元素和派系元素值）
     * @param weapon 武器物品堆（不使用，保持参数兼容性）
     * @param targetFaction 目标派系
     * @param specialAndFactionValues 特殊元素和派系元素值（从Forge属性系统预计算）
     * @return 派系伤害修饰符
     */
    public static double calculateFactionModifier(ItemStack weapon, String targetFaction, Map<String, Double> specialAndFactionValues) {
        // 直接使用预计算的派系修饰符值
        if (specialAndFactionValues != null) {
            String factionName = targetFaction.toLowerCase();
            Double modifier = specialAndFactionValues.get(factionName);
            if (modifier != null) {
                return modifier;
            }
        }
        // 如果没有预计算的值，返回默认值0.0
        return 0.0;
    }
}