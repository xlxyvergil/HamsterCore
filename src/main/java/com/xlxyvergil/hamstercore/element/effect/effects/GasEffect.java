package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
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
        // 每20 ticks（1秒）触发一次效果
        return duration % 20 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现毒气DoT效果，每1秒造成一次伤害
        // 完全按照Apotheosis的方式实现，但使用我们计算后的amplifier值
        entity.hurt(entity.level().damageSources().source(net.minecraft.core.registries.Registries.DAMAGE_TYPE.location(), entity.getLastAttacker()), 1.0F + amplifier);
    }
}