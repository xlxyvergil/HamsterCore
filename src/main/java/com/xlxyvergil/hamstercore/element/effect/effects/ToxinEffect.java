package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import com.xlxyvergil.hamstercore.handler.ElementTriggerHandler;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 毒素元素效果
 * 用于管理可绕过护盾的毒素DoT的状态效果
 */
public class ToxinEffect extends ElementEffect {
    
    // 毒素效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public ToxinEffect() {
        super(MobEffectCategory.HARMFUL, 0x00FF00); // 绿色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每40 ticks（2秒）触发一次效果
        return duration % 40 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现毒素DoT效果，每2秒造成一次伤害
        // 使用ElementEffectDataHelper获取存储的伤害值
        float baseDamage = this.getEffectDamage(entity);
        if (baseDamage <= 0.0F) {
            baseDamage = 1.0F;
        }

        // 计算DoT伤害：基础伤害 * (20% + 等级*10%)
        float dotDamage = baseDamage * (0.20F + amplifier * 0.1F);

        // 设置正在处理DoT伤害的标志，防止DoT伤害触发新的元素效果
        // 同时ElementDamageManager会检查这个标志，跳过暴击计算，避免双重暴击
        ElementTriggerHandler.setProcessingDotDamage(true);
        try {
            // 使用伤害源进行伤害，让伤害系统统一处理，但跳过暴击计算
            entity.hurt(entity.level().damageSources().mobAttack(entity.getLastAttacker()), dotDamage);
        } finally {
            // 确保在伤害处理完成后重置标志
            ElementTriggerHandler.setProcessingDotDamage(false);
        }
    }
}