package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.util.ElementNBTUtils;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.WeaponDataManager;
import com.xlxyvergil.hamstercore.element.WeaponData;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;

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
                // 直接从武器NBT的Usage层获取元素数据以计算元素总倍率
                WeaponData data = WeaponDataManager.loadElementData(weapon);
                if (data != null) {
                    // 计算元素总倍率（所有元素倍率之和，除了暴击相关属性和触发率）
                    double elementTotalRatio = 0.0;
                    
                    // 获取Usage层所有元素数据
                    Map<String, Double> usageElements = data.getUsageElements();
                    for (Map.Entry<String, Double> entry : usageElements.entrySet()) {
                        String elementTypeStr = entry.getKey();
                        double value = entry.getValue();
                        
                        // 将字符串转换为ElementType枚举
                        ElementType elementType = ElementType.byName(elementTypeStr);
                        
                        // 排除暴击相关属性和触发率，只计算元素伤害倍率
                        if (elementType != null && 
                            elementType != ElementType.CRITICAL_CHANCE && 
                            elementType != ElementType.CRITICAL_DAMAGE &&
                            elementType != ElementType.TRIGGER_CHANCE) {
                            elementTotalRatio += value;
                        }
                    }
                    
                    // 元素总倍率 = 所有元素倍率之和（默认值保证了至少为1）
                    totalElementMultiplier = 1.0 + elementTotalRatio;
                }
            }
        }
        
        return totalElementMultiplier;
    }
}