package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectInstance;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;

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
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每40 ticks（2秒）触发一次DoT伤害
        return duration % 40 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现电击DoT效果，每2秒造成一次伤害
        // 获取ElementEffectInstance以访问原始伤害值
        ElementEffectInstance elementEffectInstance = getElementEffectInstance(entity);
        float baseDamage = elementEffectInstance != null ? elementEffectInstance.getFinalDamage() : 1.0F;
        DamageSource originalDamageSource = elementEffectInstance != null ? elementEffectInstance.getDamageSource() : entity.damageSources().generic();
        
        // 计算DoT伤害：基础伤害 * 20% * (1 + 等级/10)
        float dotDamage = baseDamage * 0.20F * (1.0F + amplifier * 0.1F);
        
        // 设置正在处理DoT伤害的标志，防止DoT伤害触发新的元素效果
        // 同时ElementDamageManager会检查这个标志，跳过暴击计算，避免双重暴击
        ElementTriggerHandler.setProcessingDotDamage(true);
        try {
            // 使用原始伤害源，但通过标志防止触发新元素效果
            // ElementDamageManager会检查ProcessingDotDamage标志，跳过暴击计算
            entity.hurt(originalDamageSource, dotDamage);
        } finally {
            // 确保在伤害处理完成后重置标志
            ElementTriggerHandler.setProcessingDotDamage(false);
        }
        
        // 应用眩晕效果（移动减速）和发光效果
        applyStun(entity);
    }
    

    
    /**
     * 应用眩晕效果
     * @param entity 目标实体
     */
    private void applyStun(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel) {
            // 应用移动减速效果实现眩晕
            entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                STUN_DURATION,
                255, // 最高等级，完全禁止移动
                false, // ambient
                true, // show particles
                true // show icon
            ));
            
            // 添加发光效果表示电击状态
            entity.addEffect(new MobEffectInstance(
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
    

}