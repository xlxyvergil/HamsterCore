package com.xlxyvergil.hamstercore.element.effect.effects;

import com.xlxyvergil.hamstercore.element.effect.ElementEffect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;

/**
 * 冰冻元素效果
 * 用于管理减速和暴击伤害加成的状态效果
 */
public class ColdEffect extends ElementEffect {
    
    // 冰冻效果的最大等级
    public static final int MAX_LEVEL = 6;
    
    public ColdEffect() {
        super(MobEffectCategory.HARMFUL, 0x00FFFF); // 青色
    }
    
    /**
     * 应用冰冻效果，实现减速效果
     * @param entity 实体
     * @param amplifier 效果等级
     */
    public void applyEffect(LivingEntity entity, int amplifier) {
        // 实现减速效果，使用原版的缓慢效果
        // amplifier从0开始，所以等级1-6对应amplifier 0-5
        entity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
            MobEffects.MOVEMENT_SLOWDOWN, 
            120, // 6秒 = 120 ticks
            amplifier, 
            false, 
            true, 
            true));
    }
}