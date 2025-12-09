package com.xlxyvergil.hamstercore.handler.modifier;

import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 护甲减免计算器
 * 参考TACZ的DamageModifier设计模式
 */
public class ArmorReductionCalculator {
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     * 计算护甲减免系数 (1-AM)
     * @param target 目标实体
     * @return 护甲减免系数
     */
    public static double calculateArmorReduction(LivingEntity target) {
        // 获取我们自定义的护甲值
        double customArmor = target.getCapability(EntityArmorCapabilityProvider.CAPABILITY)
            .map(armorCap -> {
                LOGGER.debug("Getting armor from capability for entity: " + target.getType().getDescriptionId());
                double armor = armorCap.getArmor();
                LOGGER.debug("Got armor value: " + armor);
                return armor;
            })
            .orElse(0.0);
        
        // 计算AM = 0.9 × √(AR/2700)
        double AM = 0.9 * Math.sqrt(customArmor / 2700.0);
        
        return 1.0 - AM;
    }
}