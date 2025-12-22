package com.xlxyvergil.hamstercore.element.effect;

import net.minecraft.world.entity.LivingEntity;
import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.effect.effects.MagneticEffect;

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
     * 为实体添加元素效果，根据规则处理等级叠加（带伤害参数）
     * @param entity 实体
     * @param elementType 元素类型
     * @param effect 效果
     * @param maxAmplifier 最大效果等级
     * @param duration 效果持续时间
     * @param finalDamage 最终伤害值
     * @param damageSource 伤害源
     */
    public static void applyEffect(LivingEntity entity, ElementType elementType, ElementEffect effect, int maxAmplifier, int duration, float finalDamage, net.minecraft.world.damagesource.DamageSource damageSource) {
        // 检查实体是否已经有这个效果（使用内部跟踪映射）
        Map<ElementType, ElementEffectInstance> entityEffectMap = entityEffects.computeIfAbsent(entity, k -> new HashMap<>());
        ElementEffectInstance existingEffect = entityEffectMap.get(elementType);
        
        int newAmplifier;
        if (existingEffect != null) {
            // 如果已有效果且未达到最大等级，等级+1
            if (existingEffect.getAmplifier() < maxAmplifier) {
                newAmplifier = existingEffect.getAmplifier() + 1;
            } else {
                // 已达到最大等级，保持等级不变
                newAmplifier = existingEffect.getAmplifier();
            }
        } else {
            // 无现有效果，从1级开始（注意：Minecraft的Amplifier从0开始，所以1级对应Amplifier=0）
            newAmplifier = 0;
        }
        
        // 移除现有效果
        if (existingEffect != null) {
            entity.removeEffect(effect);
        }
        
        // 应用新效果，使用新的持续时间
        ElementEffectInstance effectInstance = new ElementEffectInstance(effect, duration, newAmplifier);
        entity.addEffect(effectInstance);
        
        // 更新内部跟踪映射
        entityEffectMap.put(elementType, effectInstance);
        
        // 根据效果类型调用对应的特殊效果管理器
        try {
            if (effect instanceof com.xlxyvergil.hamstercore.element.effect.effects.HeatEffect heatEffect) {
                // 处理火焰效果的护甲削减
                com.xlxyvergil.hamstercore.element.effect.HeatManager.addHeatArmorReduction(entity, duration);
                // 如果有伤害源，启动火焰DoT效果
                if (damageSource != null) {
                    heatEffect.applyEffect(entity, newAmplifier, finalDamage, damageSource);
                }
            } else if (effect instanceof com.xlxyvergil.hamstercore.element.effect.effects.BlastEffect blastEffect) {
                // 处理爆炸效果
                if (damageSource != null) {
                    com.xlxyvergil.hamstercore.element.effect.BlastManager.addBlast(entity, finalDamage, newAmplifier + 1, damageSource);
                }
            } else if (effect instanceof com.xlxyvergil.hamstercore.element.effect.effects.CorrosiveEffect corrosiveEffect) {
                // 处理腐蚀效果
                com.xlxyvergil.hamstercore.element.effect.CorrosiveManager.addCorrosive(entity, newAmplifier + 1);
            } else if (effect instanceof com.xlxyvergil.hamstercore.element.effect.effects.GasEffect gasEffect) {
                // 处理毒气效果
                if (damageSource != null) {
                    com.xlxyvergil.hamstercore.element.effect.GasManager.addGasCloud(entity, finalDamage, newAmplifier + 1, damageSource);
                }
            }
        } catch (Exception e) {
            // 记录错误但不影响主流程
            System.err.println("Error applying additional element effect: " + e.getMessage());
            e.printStackTrace();
        }
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
                // 如果效果类有自定义的清理逻辑，调用它
                ElementEffect effect = (ElementEffect) instance.getEffect();
                if (effect instanceof MagneticEffect magneticEffect) {
                    magneticEffect.removeEffect(entity);
                }
                // 从实体移除效果
                entity.removeEffect(effect);
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
                // 如果效果类有自定义的清理逻辑，调用它
                ElementEffect effect = (ElementEffect) instance.getEffect();
                if (effect instanceof MagneticEffect magneticEffect) {
                    magneticEffect.removeEffect(entity);
                }
                entity.removeEffect(effect);
            }
            effects.clear();
            entityEffects.remove(entity);
        }
    }
}