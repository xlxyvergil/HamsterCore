package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.entity.LivingEntity;
import com.xlxyvergil.hamstercore.element.ElementType;
import net.minecraft.world.effect.MobEffectInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * 元素效果管理器
 * 管理元素效果的施加、更新和移除，与项目现有的ElementType系统集成
 */
public class ElementEffectManager {
    
    // 存储实体身上的元素效果
    private static final Map<LivingEntity, Map<ElementType, ElementEffectInstance>> entityEffects = new HashMap<>();
    
    /**
     * 为实体添加元素效果，根据规则处理等级叠加
     * @param entity 实体
     * @param elementType 元素类型
     * @param effect 效果
     * @param maxAmplifier 最大效果等级
     * @param duration 效果持续时间
     */
    public static void applyEffect(LivingEntity entity, ElementType elementType, ElementEffect effect, int maxAmplifier, int duration) {
        // 检查实体是否已经有这个效果
        MobEffectInstance existingEffect = entity.getEffect(effect);
        
        int newAmplifier = 0;
        if (existingEffect != null) {
            // 如果已有效果，等级+1，但不超过最大等级
            newAmplifier = Math.min(existingEffect.getAmplifier() + 1, maxAmplifier);
        }
        
        // 移除现有效果
        if (existingEffect != null) {
            entity.removeEffect(effect);
        }
        
        // 应用新效果
        ElementEffectInstance effectInstance = new ElementEffectInstance(effect, duration, newAmplifier);
        entity.addEffect(effectInstance);
        
        // 更新内部跟踪映射
        entityEffects.computeIfAbsent(entity, k -> new HashMap<>()).put(elementType, effectInstance);
    }
    
    /**
     * 为实体添加元素效果
     * @param entity 实体
     * @param elementType 元素类型
     * @param effectInstance 效果实例
     */
    public static void addEffect(LivingEntity entity, ElementType elementType, ElementEffectInstance effectInstance) {
        entityEffects.computeIfAbsent(entity, k -> new HashMap<>())
                    .put(elementType, effectInstance);
        // 应用效果到实体
        entity.addEffect(effectInstance);
    }
    
    /**
     * 获取实体身上的指定元素效果
     * @param entity 实体
     * @param elementType 元素类型
     * @return 效果实例，如果不存在则返回null
     */
    public static ElementEffectInstance getEffect(LivingEntity entity, ElementType elementType) {
        Map<ElementType, ElementEffectInstance> effects = entityEffects.get(entity);
        if (effects != null) {
            return effects.get(elementType);
        }
        return null;
    }
    
    /**
     * 移除实体身上的指定元素效果
     * @param entity 实体
     * @param elementType 元素类型
     */
    public static void removeEffect(LivingEntity entity, ElementType elementType) {
        Map<ElementType, ElementEffectInstance> effects = entityEffects.get(entity);
        if (effects != null) {
            ElementEffectInstance instance = effects.remove(elementType);
            if (instance != null) {
                // 从实体移除效果
                entity.removeEffect(instance.getEffect());
            }
            
            // 如果该实体没有任何效果了，清理map
            if (effects.isEmpty()) {
                entityEffects.remove(entity);
            }
        }
    }
    
    /**
     * 更新实体身上的所有元素效果
     * @param entity 实体
     */
    public static void updateEffects(LivingEntity entity) {
        Map<ElementType, ElementEffectInstance> effects = entityEffects.get(entity);
        if (effects != null) {
            // 更新逻辑可以在这里实现
        }
    }
    
    /**
     * 清理实体身上的所有元素效果
     * @param entity 实体
     */
    public static void clearEffects(LivingEntity entity) {
        Map<ElementType, ElementEffectInstance> effects = entityEffects.get(entity);
        if (effects != null) {
            // 移除所有效果
            for (ElementEffectInstance instance : effects.values()) {
                entity.removeEffect(instance.getEffect());
            }
            effects.clear();
            entityEffects.remove(entity);
        }
    }
}