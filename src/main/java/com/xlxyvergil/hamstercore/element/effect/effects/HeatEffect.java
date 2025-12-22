package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.HeatManager;
import com.xlxyvergil.hamstercore.element.effect.ElementEffect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

/**
 * 火焰元素效果
 * 用于管理护甲减少和火焰DoT的状态效果
 */
public class HeatEffect extends ElementEffect {
    
    // 火焰效果的最大等级
    public static final int MAX_LEVEL = 10;
    
    // 火焰效果持续时间（tick）：6秒 = 120 ticks
    private static final int FLAME_DURATION = 120;
    
    public HeatEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF8C00); // 橙色
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        // 每20 ticks（1秒）触发一次效果
        return duration % 20 == 0;
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        // 每20 ticks（1秒）造成一次火焰DoT伤害
        // 完全按照Apotheosis的方式实现，但使用我们计算后的amplifier值
        entity.hurt(entity.level().damageSources().source(net.minecraft.core.registries.Registries.DAMAGE_TYPE.location(), entity.getLastAttacker()), 1.0F + amplifier);
    }
    
    @Override
    public void addAttributeModifiers(LivingEntity entity, net.minecraft.world.entity.ai.attributes.AttributeMap attributeMap, int amplifier) {
        super.addAttributeModifiers(entity, attributeMap, amplifier);
        // 应用护甲减少50%的效果，持续6秒（覆盖机制）
        HeatManager.addHeatArmorReduction(entity, FLAME_DURATION);
    }
}