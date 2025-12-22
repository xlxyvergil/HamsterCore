package com.xlxyvergil.hamstercore.element.effect;

import com.xlxyvergil.hamstercore.attribute.EntityAttributeRegistry;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.damagesource.DamageSource;

import java.util.*;

/**
 * 火焰效果管理器
 * 管理火焰效果的护甲削减部分，使用覆盖机制
 */
public class HeatManager {
    
    // 存储实体身上的火焰效果及其护甲修饰符UUID
    private static final Map<LivingEntity, HeatEffectData> entityHeats = new HashMap<>();
    
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
            -0.5, // 50%减少
            AttributeModifier.Operation.MULTIPLY_BASE
        );
        
        // 添加修饰符到实体的护甲属性上
        if (entity.getAttribute(EntityAttributeRegistry.ARMOR.get()) != null) {
            entity.getAttribute(EntityAttributeRegistry.ARMOR.get()).addPermanentModifier(armorModifier);
        }
        
        // 存储效果数据
        entityHeats.put(entity, new HeatEffectData(modifierUUID, duration));
        
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
}