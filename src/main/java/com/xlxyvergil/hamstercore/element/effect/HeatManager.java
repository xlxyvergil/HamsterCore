package com.xlxyvergil.hamstercore.element.effect;

import com.xlxyvergil.hamstercore.attribute.EntityAttributeRegistry;
import com.xlxyvergil.hamstercore.content.capability.EntityCapabilityAttacher;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityArmorCapabilityProvider;
import com.xlxyvergil.hamstercore.content.capability.entity.EntityShieldCapabilityProvider;
import com.xlxyvergil.hamstercore.util.AttributeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

import java.util.*;

/**
 * 火焰效果管理器
 * 管理火焰效果的护甲削减部分，使用覆盖机制
 */
public class HeatManager {
    
    // 存储实体身上的火焰效果及其护甲修饰符UUID
    private static final Map<LivingEntity, HeatEffectData> entityHeats = Collections.synchronizedMap(new HashMap<>());
    
    /**
     * 火焰效果数据类
     */
    public static class HeatEffectData {
        private final UUID modifierUUID;
        private int ticksRemaining;
        
        public HeatEffectData(UUID modifierUUID, int duration) {
            this.modifierUUID = modifierUUID;
            this.ticksRemaining = duration;
        }
        
        public void decrementTicks() {
            this.ticksRemaining--;
        }
        
        public boolean isExpired() {
            return ticksRemaining <= 0;
        }
        
        public UUID getModifierUUID() {
            return modifierUUID;
        }
        
        public int getTicksRemaining() {
            return ticksRemaining;
        }
    }
    
    /**
     * 为实体添加火焰护甲削减效果
     * @param entity 实体
     * @param duration 持续时间（tick）
     * @return 护甲修饰符UUID
     */
    public static UUID addHeatArmorReduction(LivingEntity entity, int duration) {
        // 清理现有的火焰效果（覆盖机制）
        removeHeatArmorReduction(entity);
        
        // 生成新的UUID
        UUID modifierUUID = UUID.randomUUID();
        
        // 创建护甲减少修饰符：减少50%护甲
        AttributeModifier armorModifier = new AttributeModifier(
            modifierUUID,
            "Heat Armor Reduction",
            0.5, // 50%减少
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        
        // 添加修饰符到实体的护甲属性上
        if (entity.getAttribute(EntityAttributeRegistry.ARMOR.get()) != null) {
            // 先检查并移除已存在的修饰符
            AttributeModifier existingModifier = entity.getAttribute(EntityAttributeRegistry.ARMOR.get()).getModifier(modifierUUID);
            if (existingModifier != null) {
                entity.getAttribute(EntityAttributeRegistry.ARMOR.get()).removeModifier(existingModifier);
            }
            entity.getAttribute(EntityAttributeRegistry.ARMOR.get()).addPermanentModifier(armorModifier);
        }
        
        // 存储效果数据
        entityHeats.put(entity, new HeatEffectData(modifierUUID, duration));
        
        // 安全地刷新Capability数据，保持当前护盾值和血量不变
        refreshCapabilityData(entity);
        
        return modifierUUID;
    }
    
    /**
     * 更新实体身上的火焰效果
     * @param entity 实体
     */
    public static void updateHeatEffects(LivingEntity entity) {
        HeatEffectData data = entityHeats.get(entity);
        if (data != null) {
            data.decrementTicks();
            
            // 如果效果结束，清理它
            if (data.isExpired()) {
                removeHeatArmorReduction(entity);
            }
        }
    }
    
    /**
     * 移除实体身上的火焰护甲削减效果
     * @param entity 实体
     */
    public static void removeHeatArmorReduction(LivingEntity entity) {
        HeatEffectData data = entityHeats.remove(entity);
        if (data != null) {
            // 移除属性修饰符
            if (entity.getAttribute(EntityAttributeRegistry.ARMOR.get()) != null) {
                entity.getAttribute(EntityAttributeRegistry.ARMOR.get()).removeModifier(data.getModifierUUID());
            }
        }
        
        // 安全地刷新Capability数据，保持当前护盾值和血量不变
        refreshCapabilityData(entity);
    }
    
    /**
     * 清理实体身上的所有火焰效果
     * @param entity 实体
     */
    public static void clearHeatEffects(LivingEntity entity) {
        removeHeatArmorReduction(entity);
    }
    
    /**
     * 检查实体是否有火焰护甲削减效果
     * @param entity 实体
     * @return 是否有效果
     */
    public static boolean hasHeatEffect(LivingEntity entity) {
        return entityHeats.containsKey(entity);
    }
    
    /**
     * 获取火焰效果剩余时间
     * @param entity 实体
     * @return 剩余tick数，如果没有效果则返回0
     */
    public static int getHeatEffectTicksRemaining(LivingEntity entity) {
        HeatEffectData data = entityHeats.get(entity);
        return data != null ? data.getTicksRemaining() : 0;
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