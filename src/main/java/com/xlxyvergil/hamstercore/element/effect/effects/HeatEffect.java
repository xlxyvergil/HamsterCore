package com.xlxyvergil.hamstercore.element.effect.effects;

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
    
    public HeatEffect() {
        super(MobEffectCategory.HARMFUL, 0xFF8C00); // 橙色
    }
    
    /**
     * 应用火焰效果，实现护甲减少和火焰DoT效果
     * @param entity 实体
     * @param amplifier 效果等级
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现护甲减少和火焰DoT效果
        // 护甲减少可以通过AttributeModifier实现
        // 火焰DoT可以通过周期性伤害实现
    }
}