package com.xlxyvergil.hamstercore.element.effect.effects;

import java.util.UUID;

import com.xlxyvergil.hamstercore.attribute.EntityAttributeRegistry;
import com.xlxyvergil.hamstercore.content.capability.EntityCapabilityAttacher;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapability;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.util.AttributeHelper;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * 磁力元素效果
 * 用于管理护盾伤害、护盾再生失效和破盾后电击伤害的状态效果
 */
public class MagneticEffect extends ElementEffect {
    
    // 磁力效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    // 护盾再生延迟属性修饰符UUID
    private static final UUID REGEN_DELAY_MODIFIER_UUID = 
        UUID.nameUUIDFromBytes("magnetic_regen_delay".getBytes());
    private static final UUID DEPLETED_REGEN_DELAY_MODIFIER_UUID = 
        UUID.nameUUIDFromBytes("magnetic_depleted_regen_delay".getBytes());
    
    public MagneticEffect() {
        super(MobEffectCategory.HARMFUL, 0x0000FF); // 蓝色
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        // 应用护盾再生延迟修饰符
        applyShieldRegenDelayModifiers(entity, amplifier);
        
        // 安全地刷新Capability数据，保持当前护盾值和血量不变
        refreshCapabilityData(entity);
    }
    
    @Override
    public void removeAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        // 移除护盾再生延迟修饰符
        removeShieldRegenDelayModifiers(entity);
        
        // 安全地刷新Capability数据，保持当前护盾值和血量不变
        refreshCapabilityData(entity);
    }
    
    /**
     * 应用护盾再生延迟修饰符
     * @param entity 实体
     * @param amplifier 效果等级
     */
    private void applyShieldRegenDelayModifiers(LivingEntity entity, int amplifier) {
        // 磁力效果期间，护盾再生被大幅延长
        // 基础延迟延长：每级+10秒延迟（200 ticks）
        double regenDelayMultiplier = 1.0 + (amplifier * 10.0); // 1级:+10秒，10级:+100秒
        double depletedDelayMultiplier = 1.0 + (amplifier * 15.0); // 耗尽时延迟更长
        
        // 添加再生延迟修饰符
        if (entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get()) != null) {
            AttributeModifier regenDelayModifier = new AttributeModifier(
                REGEN_DELAY_MODIFIER_UUID,
                "Magnetic Regen Delay",
                regenDelayMultiplier,
                AttributeModifier.Operation.ADDITION
            );
            // 先检查并移除已存在的修饰符
            AttributeModifier existingModifier = entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get()).getModifier(REGEN_DELAY_MODIFIER_UUID);
            if (existingModifier != null) {
                entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get()).removeModifier(existingModifier);
            }
            entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get()).addPermanentModifier(regenDelayModifier);
        }
        
        // 添加耗尽再生延迟修饰符
        if (entity.getAttribute(EntityAttributeRegistry.DEPLETED_REGEN_DELAY.get()) != null) {
            AttributeModifier depletedDelayModifier = new AttributeModifier(
                DEPLETED_REGEN_DELAY_MODIFIER_UUID,
                "Magnetic Depleted Regen Delay",
                depletedDelayMultiplier,
                AttributeModifier.Operation.ADDITION
            );
            // 先检查并移除已存在的修饰符
            AttributeModifier existingDepletedModifier = entity.getAttribute(EntityAttributeRegistry.DEPLETED_REGEN_DELAY.get()).getModifier(DEPLETED_REGEN_DELAY_MODIFIER_UUID);
            if (existingDepletedModifier != null) {
                entity.getAttribute(EntityAttributeRegistry.DEPLETED_REGEN_DELAY.get()).removeModifier(existingDepletedModifier);
            }
            entity.getAttribute(EntityAttributeRegistry.DEPLETED_REGEN_DELAY.get()).addPermanentModifier(depletedDelayModifier);
        }
    }
    
    /**
     * 移除护盾再生延迟修饰符
     * @param entity 实体
     */
    private void removeShieldRegenDelayModifiers(LivingEntity entity) {
        // 移除护盾再生延迟修饰符
        if (entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get()) != null) {
            entity.getAttribute(EntityAttributeRegistry.REGEN_DELAY.get()).removeModifier(REGEN_DELAY_MODIFIER_UUID);
        }
        
        // 移除护盾耗尽再生延迟修饰符
        if (entity.getAttribute(EntityAttributeRegistry.DEPLETED_REGEN_DELAY.get()) != null) {
            entity.getAttribute(EntityAttributeRegistry.DEPLETED_REGEN_DELAY.get()).removeModifier(DEPLETED_REGEN_DELAY_MODIFIER_UUID);
        }
    }
    
    /**
     * 计算护盾伤害增幅倍率
     * @param amplifier 效果等级 (0-9，对应1-10级)
     * @return 伤害增幅倍率
     */
    public static double calculateShieldDamageMultiplier(int amplifier) {
        return 1.0 + 1.0 + (amplifier * 0.25);
    }
    
    /**
     * 计算破盾后电击伤害
     * @param entity 实体
     * @param amplifier 效果等级
     * @return 电击伤害值
     */
    public static float calculateShieldBreakElectricDamage(LivingEntity entity, int amplifier) {
        EntityShieldCapability shieldCap = entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).orElse(null);
        if (shieldCap == null) {
            return 0.0f;
        }
        
        // 基于敌方护盾总值3% * 效果等级的电击伤害
        float maxShield = shieldCap.getMaxShield();
        float damagePercentage = 0.03f; // 3%
        
        return maxShield * damagePercentage * (amplifier + 1);
    }
    
    /**
     * 检查实体是否有磁力效果（使用MobEffect参数）
     * @param entity 实体
     * @param magneticEffect 磁力效果实例（MobEffect类型）
     * @return 磁力效果等级，如果没有则返回-1
     */
    public static int getMagneticEffectLevel(LivingEntity entity, MobEffect magneticEffect) {
        if (entity.hasEffect(magneticEffect)) {
            return entity.getEffect(magneticEffect).getAmplifier();
        }
        return -1;
    }
    
    /**
     * 检查实体是否有磁力效果（使用MagneticEffect参数）
     * @param entity 实体
     * @param magneticEffect 磁力效果实例
     * @return 磁力效果等级，如果没有则返回-1
     */
    public static int getMagneticEffectLevel(LivingEntity entity, MagneticEffect magneticEffect) {
        if (entity.hasEffect(magneticEffect)) {
            return entity.getEffect(magneticEffect).getAmplifier();
        }
        return -1;
    }
    
    /**
     * 安全地更新实体的Capability数据，保持当前护盾值和血量不变
     * @param entity 实体
     */
    public static void refreshCapabilityData(LivingEntity entity) {
        // 仅更新Attribute相关的Capability，保持当前值不变
        entity.getCapability(EntityArmorCapabilityProvider.CAPABILITY).ifPresent(armorCap -> {
            armorCap.setArmor(AttributeHelper.getArmor(entity));
        });
        
        // 先获取当前护盾值
        float currentShieldValue = entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY)
            .map(shieldCap -> shieldCap.getCurrentShield())
            .orElse(0.0f);
        
        // 然后更新护盾相关参数，同时保持当前护盾值
        entity.getCapability(EntityShieldCapabilityProvider.CAPABILITY).ifPresent(shieldCap -> {
            // 只更新不涉及当前护盾值的参数
            shieldCap.setMaxShield((float) AttributeHelper.getShield(entity));
            shieldCap.setRegenRate((float) AttributeHelper.getRegenRate(entity));
            shieldCap.setRegenDelay((int) AttributeHelper.getRegenDelay(entity));
            shieldCap.setRegenDelayDepleted((int) AttributeHelper.getDepletedRegenDelay(entity));
            shieldCap.setImmunityTime((int) AttributeHelper.getImmunityTime(entity));
            
            // 恢复之前的当前护盾值，防止实体回血
            shieldCap.setCurrentShield(currentShieldValue);
        });
        
        // 同步更新到客户端
        EntityCapabilityAttacher.syncEntityCapabilitiesToClients(entity);
    }
}