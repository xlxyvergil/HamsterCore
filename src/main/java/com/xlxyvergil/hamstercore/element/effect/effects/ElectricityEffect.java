package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.ElementType;
import com.xlxyvergil.hamstercore.element.effect.DoTManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.damagesource.DamageSource;

/**
 * 电击元素效果
 * 用于管理电击DoT和眩晕的状态效果
 */
public class ElectricityEffect extends ElementEffect {
    
    // 电击效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    // 眩晕持续时间（tick）：3秒 = 60 ticks
    private static final int STUN_DURATION = 60;
    
    // 电击DoT持续时间（tick）：6秒 = 120 ticks
    private static final int SHOCK_DURATION = 120;
    
    // 影响范围（米）：5米
    private static final double AOE_RANGE = 5.0;
    
    // 基础伤害值（每秒）
    private static final float BASE_DAMAGE_PER_SECOND = 5.0f;
    
    public ElectricityEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFFF00); // 黄色
    }
    
    /**
     * 应用电击效果，实现电击DoT和眩晕效果
     * @param entity 实体
     * @param amplifier 效果等级
     * @param damageSource 原始伤害源
     */
    public void applyEffect(LivingEntity entity, int amplifier, net.minecraft.world.damagesource.DamageSource damageSource) {
        // 对主目标应用眩晕效果3秒
        applyStun(entity);
        
        // 启动电击DoT，每层独立计算
        startShockDoT(entity, amplifier, damageSource);
        
        // 对周围5米内的敌人也施加电击DoT
        applyAoEShock(entity, amplifier, damageSource);
    }
    
    /**
     * 应用眩晕效果
     * @param entity 目标实体
     */
    private void applyStun(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel) {
            // 应用移动减速效果实现眩晕
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                STUN_DURATION,
                255, // 最高等级，完全禁止移动
                false, // ambient
                true, // show particles
                true // show icon
            ));
            
            // 添加发光效果表示电击状态
            entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                MobEffects.GLOWING,
                STUN_DURATION,
                0,
                false,
                true,
                false
            ));
            
            // 生成电击粒子效果
            ((ServerLevel) entity.level()).sendParticles(
                ParticleTypes.ELECTRIC_SPARK,
                entity.getX(),
                entity.getY(),
                entity.getZ(),
                20,
                0.5, 0.5, 0.5,
                0.1
            );
        }
    }
    
    /**
     * 启动电击DoT效果
     * @param target 目标实体
     * @param amplifier 效果等级
     */
    private void startShockDoT(LivingEntity target, int amplifier, net.minecraft.world.damagesource.DamageSource damageSource) {
        // 计算每秒伤害：基础伤害 * 50% * (amplifier + 1)
        float damagePerSecond = BASE_DAMAGE_PER_SECOND * 0.5f * (amplifier + 1);
        
        // 添加DoT效果到目标
        DoTManager.addDoT(target, ElementType.ELECTRICITY, damagePerSecond, SHOCK_DURATION, amplifier, damageSource);
    }
    
    /**
     * 对范围内敌人施加电击效果
     * @param center 中心实体
     * @param amplifier 效果等级
     * @param damageSource 原始伤害源
     */
    private void applyAoEShock(LivingEntity center, int amplifier, net.minecraft.world.damagesource.DamageSource damageSource) {
        if (center.level() instanceof ServerLevel serverLevel) {
            // 获取周围5米内的所有生物（不包括中心实体）
            AABB aabb = new AABB(center.getX() - AOE_RANGE, center.getY() - AOE_RANGE, center.getZ() - AOE_RANGE,
                               center.getX() + AOE_RANGE, center.getY() + AOE_RANGE, center.getZ() + AOE_RANGE);
            
            java.util.List<LivingEntity> nearbyEntities = serverLevel.getEntitiesOfClass(LivingEntity.class, aabb,
                entity -> entity != center && entity.isAlive());
            
            // 对每个周围的敌人施加电击DoT（但不施加眩晕）
            for (LivingEntity entity : nearbyEntities) {
                startShockDoT(entity, amplifier, damageSource);
                
                // 生成电击粒子效果
                serverLevel.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    10,
                    0.3, 0.3, 0.3,
                    0.05
                );
            }
        }
    }
}