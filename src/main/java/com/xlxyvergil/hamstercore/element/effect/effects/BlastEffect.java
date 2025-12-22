package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.element.effect.ElementEffectInstance;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 爆炸元素效果
 * 用于管理延迟范围伤害的状态效果
 */
public class BlastEffect extends ElementEffect {
    
    // 爆炸效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public BlastEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFD700); // 金色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每20 ticks（1秒）触发一次效果
        return duration % 20 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现爆炸DoT效果，每秒造成一次伤害
        // 获取ElementEffectInstance以访问原始伤害值
        ElementEffectInstance elementEffectInstance = getElementEffectInstance(entity);
        float baseDamage = elementEffectInstance != null ? elementEffectInstance.getFinalDamage() : 1.0F;
        
        // 计算DoT伤害：基础伤害 * 40% * (1 + 等级/10)
        float dotDamage = baseDamage * 0.40F * (1.0F + amplifier * 0.1F);
        
        // 设置正在处理DoT伤害的标志，防止DoT伤害触发新的元素效果
        ElementTriggerHandler.setProcessingDotDamage(true);
        try {
            entity.hurt(entity.level().damageSources().mobAttack(entity.getLastAttacker()), dotDamage);
        } finally {
            // 确保在伤害处理完成后重置标志
            ElementTriggerHandler.setProcessingDotDamage(false);
        }
    }
}