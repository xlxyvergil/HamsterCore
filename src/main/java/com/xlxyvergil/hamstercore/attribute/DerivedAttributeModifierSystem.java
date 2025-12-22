package com.xlxyvergil.hamstercore.attribute;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.UUID;

/**
 * 衍生属性修饰符系统
 * 负责生成依赖于基础属性计算的衍生修饰符（如基于护盾值计算恢复速率）
 */
public class DerivedAttributeModifierSystem {
    
    // 为每个属性修饰符定义唯一的UUID
    private static final UUID REGEN_RATE_MODIFIER_UUID = UUID.fromString("978da1a2-be2c-453d-b5ff-2186bf4d058d");
    private static final UUID IMMUNITY_TIME_MODIFIER_UUID = UUID.fromString("0d39076e-2865-4097-bd4f-b7953fdbb8c0");
    private static final UUID REGEN_DELAY_MODIFIER_UUID = UUID.fromString("b5f68e57-1551-4964-8ef4-ebb6b56f6ec2");
    private static final UUID DEPLETED_REGEN_DELAY_MODIFIER_UUID = UUID.fromString("0aed4ca9-bd13-480d-9705-e4e7e58f6f65");
    
    /**
     * 应用衍生属性修饰符到实体
     * @param entity 目标实体
     */
    public static void applyDerivedModifiers(LivingEntity entity) {
        // 移除所有现有的衍生属性修饰符
        removeAllDerivedModifiers(entity);
        
        // 基于基础护盾值计算护盾恢复速率
        applyRegenRateModifier(entity);
        
        // 基于基础护盾值计算护盾保险时间
        applyImmunityTimeModifier(entity);
        
        // 基于基础护盾值计算恢复延迟
        applyRegenDelayModifier(entity);
        
        // 基于基础护盾值计算护盾耗尽恢复延迟
        applyDepletedRegenDelayModifier(entity);
    }
    
    /**
     * 基于基础护盾值计算护盾恢复速率修饰符
     * 每秒护盾回复 = 15 + 0.05 × 护盾容量
     * @param entity 目标实体
     */
    private static void applyRegenRateModifier(LivingEntity entity) {
        AttributeInstance shieldAttr = entity.getAttribute(EntityAttributeRegistry.SHIELD.get());
        AttributeInstance regenRateAttr = entity.getAttribute(EntityAttributeRegistry.REGEN_RATE.get());
        
        if (shieldAttr != null && regenRateAttr != null) {
            double shieldValue = shieldAttr.getValue();
            // 计算恢复速率：每秒护盾回复 = 15 + 0.05 × 护盾容量
            double regenRate = 15.0 + 0.05 * shieldValue;
            
            // 添加恢复速率修饰符
            regenRateAttr.addTransientModifier(new AttributeModifier(
                REGEN_RATE_MODIFIER_UUID,
                "Regen Rate Modifier",
                regenRate,
                AttributeModifier.Operation.ADDITION
            ));
        }
    }
    
    /**
     * 基于基础护盾值计算护盾保险时间修饰符
     * 根据护盾最大值计算免疫时间：
     * - maxShield < 53：免疫时间 = maxShield/180 + 1/3 秒
     * - 53 ≤ maxShield < 1150：免疫时间 = (maxShield/350)^0.65 + 1/3 秒
     * - maxShield ≥ 1150：免疫时间固定为2.5秒
     * @param entity 目标实体
     */
    private static void applyImmunityTimeModifier(LivingEntity entity) {
        AttributeInstance shieldAttr = entity.getAttribute(EntityAttributeRegistry.SHIELD.get());
        AttributeInstance immunityTimeAttr = entity.getAttribute(EntityAttributeRegistry.IMMUNITY_TIME.get());
        
        if (shieldAttr != null && immunityTimeAttr != null) {
            double maxShield = shieldAttr.getValue();
            double immunityTime = 0;
            
            // 计算护盾保险机制的免疫时间
            // 护盾保险机制的计算必须基于实体的最大护盾值（maxShield），而非当前护盾值（currentShield）
            if (maxShield < 53) {
                // 低护盾值情况：免疫时间 = 护盾量/180 + 1/3 秒
                immunityTime = (maxShield / 180.0 + 1.0/3.0) * 20;
            } else if (maxShield < 1150) {
                // 中等护盾值情况：免疫时间 = (护盾量/350)^0.65 + 1/3 秒
                immunityTime = (Math.pow(maxShield / 350.0, 0.65) + 1.0/3.0) * 20;
            } else {
                // 高护盾值情况：免疫时间 = 2.5 秒
                immunityTime = 2.5 * 20;
            }
            
            // 添加免疫时间修饰符
            immunityTimeAttr.addTransientModifier(new AttributeModifier(
                IMMUNITY_TIME_MODIFIER_UUID,
                "Immunity Time Modifier",
                immunityTime,
                AttributeModifier.Operation.ADDITION
            ));
        }
    }
    
    /**
     * 基于基础护盾值计算恢复延迟修饰符
     * @param entity 目标实体
     */
    private static void applyRegenDelayModifier(LivingEntity entity) {
        AttributeInstance shieldAttr = entity.getAttribute(EntityAttributeRegistry.SHIELD.get());
        AttributeInstance regenDelayAttr = entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get());
        
        if (shieldAttr != null && regenDelayAttr != null) {
            // 这里可以根据需要定义恢复延迟的计算逻辑
            // 目前我们使用一个简单的示例逻辑
            double regenDelay = 2 * 20; // 默认2秒
            
            // 添加恢复延迟修饰符
            regenDelayAttr.addTransientModifier(new AttributeModifier(
                REGEN_DELAY_MODIFIER_UUID,
                "Regen Delay Modifier",
                regenDelay,
                AttributeModifier.Operation.ADDITION
            ));
        }
    }
    
    /**
     * 基于基础护盾值计算护盾耗尽恢复延迟修饰符
     * @param entity 目标实体
     */
    private static void applyDepletedRegenDelayModifier(LivingEntity entity) {
        AttributeInstance shieldAttr = entity.getAttribute(EntityAttributeRegistry.SHIELD.get());
        AttributeInstance depletedRegenDelayAttr = entity.getAttribute(EntityAttributeRegistry.DEPLETED_REGEN_DELAY.get());
        
        if (shieldAttr != null && depletedRegenDelayAttr != null) {
            // 这里可以根据需要定义护盾耗尽恢复延迟的计算逻辑
            // 目前我们使用一个简单的示例逻辑
            double depletedRegenDelay = 6 * 20; // 默认6秒
            
            // 添加护盾耗尽恢复延迟修饰符
            depletedRegenDelayAttr.addTransientModifier(new AttributeModifier(
                DEPLETED_REGEN_DELAY_MODIFIER_UUID,
                "Depleted Regen Delay Modifier",
                depletedRegenDelay,
                AttributeModifier.Operation.ADDITION
            ));
        }
    }
    
    /**
     * 移除实体上的所有衍生属性修饰符
     * @param entity 目标实体
     */
    private static void removeAllDerivedModifiers(LivingEntity entity) {
        // 移除恢复速率属性的衍生修饰符
        AttributeInstance regenRateAttr = entity.getAttribute(EntityAttributeRegistry.REGEN_RATE.get());
        if (regenRateAttr != null) {
            regenRateAttr.removeModifier(REGEN_RATE_MODIFIER_UUID);
        }
        
        // 移除免疫时间属性的衍生修饰符
        AttributeInstance immunityTimeAttr = entity.getAttribute(EntityAttributeRegistry.IMMUNITY_TIME.get());
        if (immunityTimeAttr != null) {
            immunityTimeAttr.removeModifier(IMMUNITY_TIME_MODIFIER_UUID);
        }
        
        // 移除恢复延迟属性的衍生修饰符
        AttributeInstance regenDelayAttr = entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get());
        if (regenDelayAttr != null) {
            regenDelayAttr.removeModifier(REGEN_DELAY_MODIFIER_UUID);
        }
        
        // 移除护盾耗尽恢复延迟属性的衍生修饰符
        AttributeInstance depletedRegenDelayAttr = entity.getAttribute(EntityAttributeRegistry.DEPLETED_REGEN_DELAY.get());
        if (depletedRegenDelayAttr != null) {
            depletedRegenDelayAttr.removeModifier(DEPLETED_REGEN_DELAY_MODIFIER_UUID);
        }
    }
}