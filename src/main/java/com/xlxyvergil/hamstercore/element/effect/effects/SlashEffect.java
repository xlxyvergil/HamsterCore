package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 切割元素效果
 * 用于管理出血DoT的状态效果
 */
public class SlashEffect extends ElementEffect {
    
    // 切割效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    public SlashEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF0000); // 红色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每40 ticks（2秒）触发一次效果（参考Apotheosis的BleedingEffect实现）
        return duration % 40 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 实现出血DoT效果，每2秒造成一次伤害
        // 完全按照Apotheosis的方式实现，但使用我们计算后的amplifier值
        entity.hurt(entity.level().damageSources().source(net.minecraft.core.registries.Registries.DAMAGE_TYPE.location(), entity.getLastAttacker()), 1.0F + amplifier);
    }
}