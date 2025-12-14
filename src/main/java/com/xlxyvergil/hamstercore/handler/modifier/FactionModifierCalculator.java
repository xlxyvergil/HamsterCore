package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityFactionCapabilityProvider;
import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.WeaponData;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.faction.Faction;
import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import com.xlxyvergil.hamstercore.util.ElementModifierValueUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;

/**
 * 派系修饰符计算器
 * 计算针对特定派系的伤害修饰符
 */
public class FactionModifierCalculator {
    
    /**
     * 计算派系伤害修饰符
     * @param weapon 武器物品堆
     * @param targetFaction 目标派系
     * @return 派系伤害修饰符（从修饰符系统获取计算后的值）
     */
    public static double calculateFactionModifier(ItemStack weapon, String targetFaction) {
        // 从修饰符系统获取派系增伤值
        return ElementModifierValueUtil.getElementValueFromAttributes(weapon, ElementType.byName(targetFaction.toLowerCase()));
    }
    
    /**
     * 计算派系伤害修饰符（使用预计算的特殊元素和派系元素值）
     * @param specialAndFactionValues 特殊元素和派系元素值（从Forge属性系统预计算）
     * @param targetFaction 目标派系
     * @return 派系伤害修饰符
     */
    public static double calculateFactionModifier(Map<String, Double> specialAndFactionValues, String targetFaction) {
        // 从预计算的值中获取派系增伤值
        if (specialAndFactionValues != null) {
            String factionName = targetFaction.toLowerCase();
            Double modifier = specialAndFactionValues.get(factionName);
            if (modifier != null) {
                return modifier;
            }
        }
        return 0.0;
    }
}