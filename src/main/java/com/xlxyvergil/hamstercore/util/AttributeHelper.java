package com.xlxyvergil.hamstercore.util;

import com.xlxyvergil.hamstercore.attribute.EntityAttributeRegistry;
import net.minecraft.world.entity.LivingEntity;

/**
 * 属性助手类
 * 用于获取实体身上的各种属性值
 */
public class AttributeHelper {
    
    /**
     * 获取实体的基础护甲值
     * @param entity 实体
     * @return 基础护甲值
     */
    public static double getBaseArmor(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.BASE_ARMOR.get()).getValue();
    }
    
    /**
     * 获取实体的护甲值
     * @param entity 实体
     * @return 护甲值
     */
    public static double getArmor(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.ARMOR.get()).getValue();
    }
    
    /**
     * 获取实体的最大护盾值
     * @param entity 实体
     * @return 最大护盾值
     */
    public static double getMaxShield(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.MAX_SHIELD.get()).getValue();
    }
    
    /**
     * 获取实体的护盾恢复速率
     * @param entity 实体
     * @return 护盾恢复速率（每秒恢复值）
     */
    public static double getRegenRate(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.REGEN_RATE.get()).getValue();
    }
    
    /**
     * 获取实体的护盾恢复延迟
     * @param entity 实体
     * @return 护盾恢复延迟（tick数）
     */
    public static double getRegenDelay(LivingEntity entity) {
        return entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get()).getValue();
    }
}