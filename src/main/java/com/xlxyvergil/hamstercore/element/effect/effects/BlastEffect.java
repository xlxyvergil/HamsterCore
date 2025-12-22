package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
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
        // 每40 ticks（2秒）触发一次效果
        return duration % 40 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现爆炸DoT效果，每2秒造成一次伤害
        // 完全按照Apotheosis的方式实现，但使用我们计算后的amplifier值
        entity.hurt(entity.level().damageSources().source(net.minecraft.core.registries.Registries.DAMAGE_TYPE.location(), entity.getLastAttacker()), 1.0F + amplifier);
    }
}