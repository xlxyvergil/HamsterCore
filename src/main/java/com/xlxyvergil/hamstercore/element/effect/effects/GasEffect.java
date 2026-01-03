package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectRegistry.Effects;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 毒气元素效果
 * 用于管理AoE毒气DoT的状态效果
 */
public class GasEffect extends ElementEffect {
    
    // 毒气效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    // 毒气效果持续时间（tick）：6秒 = 120 ticks
    private static final int GAS_DURATION = 120;
    
    public GasEffect() {
        super(MobEffectCategory.HARMFUL, 0x9370DB); // 紫色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每40 ticks（2秒）触发一次DoT伤害
        return duration % 40 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现毒气DoT效果，每2秒造成一次伤害
        // 使用ElementEffectDataHelper获取存储的伤害值
        float baseDamage = this.getEffectDamage(entity);
        if (baseDamage <= 0.0F) {
            baseDamage = 1.0F;
        }
        DamageSource originalDamageSource = entity.damageSources().generic();

        // 计算DoT伤害：基础伤害 * 30% * (1 + 等级/10) - 提高伤害
        float dotDamage = baseDamage * 0.30F * (1.0F + amplifier * 0.1F);

        // 设置正在处理DoT伤害的标志，防止DoT伤害触发新的元素效果
        ElementTriggerHandler.setProcessingDotDamage(true);
        try {
            // 使用原始伤害源，但通过标志防止触发新元素效果
            entity.hurt(originalDamageSource, dotDamage);
        } finally {
            // 确保在伤害处理完成后重置标志
            ElementTriggerHandler.setProcessingDotDamage(false);
        }
    }
    

}