package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.handler.ElementDamageManager;
import com.xlxyvergil.hamstercore.element.ElementType;
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
            
            // 从ElementDamageManager缓存中获取激活的元素列表
            List<Map.Entry<ElementType, Double>> activeElements = ElementDamageManager.getActiveElements(weapon);
            
            // 计算元素总倍率（所有元素倍率之和）
            double elementTotalRatio = 0.0;
            
            // 遍历激活的元素，只计算基础元素和复合元素的倍率
            // Usage层只包含基础元素和复合元素，不包含特殊属性和派系增伤，所以可以直接累加
            for (Map.Entry<ElementType, Double> entry : activeElements) {
                ElementType elementType = entry.getKey();
                double value = entry.getValue();
                
                // 计算基础元素和复合元素的倍率
                if (elementType != null && (elementType.isBasic() || elementType.isComplex())) {
                    elementTotalRatio += value;
                }
            }
            
            // 元素总倍率 = 1.0 + 所有元素倍率之和
            totalElementMultiplier = 1.0 + elementTotalRatio;
        }
        
        return totalElementMultiplier;
    }
}